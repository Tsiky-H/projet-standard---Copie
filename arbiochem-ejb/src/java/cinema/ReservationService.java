package cinema;

import bean.CGenUtil;
import caisse.MvtCaisse;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import utilitaire.UtilDB;
import user.UserEJB;
import vente.Vente;
import vente.VenteDetails;
import vente.VenteDetailsArbiochem;

/** Opérations transactionnelles de réservation, d'achat direct et d'encaissement cinéma. */
public final class ReservationService {
    private static final String MAGASIN_CINEMA = "MAG_CINEMA";
    private static final String CAISSE_CINEMA = "CAI_CINEMA";
    private static final String PRODUIT_BILLET = "CINE_BILLET";
    private static final String COMPTE_VENTE = "706";
    private static final String DEVISE = "AR";

    private ReservationService() { }

    public static final class Resultat {
        private final String idReservation;
        private final String idVente;
        Resultat(String idReservation, String idVente) {
            this.idReservation = idReservation;
            this.idVente = idVente;
        }
        public String getIdReservation() { return idReservation; }
        public String getIdVente() { return idVente; }
    }

    public static PlaceSeanceDispo[] listerPlaces(String idSeance) throws Exception {
        if (vide(idSeance)) throw new IllegalArgumentException("La séance est obligatoire.");
        try (Connection c = new UtilDB().GetConn()) {
            expirerReservations(c);
            PlaceSeanceDispo critere = new PlaceSeanceDispo();
            Object[] trouvees = CGenUtil.rechercher(critere, null, null, c,
                    " AND IDSEANCE = '" + sql(idSeance.trim()) + "' ORDER BY IDCATEGORIELIB, NUMERO");
            PlaceSeanceDispo[] resultat = new PlaceSeanceDispo[trouvees.length];
            System.arraycopy(trouvees, 0, resultat, 0, trouvees.length);
            return resultat;
        }
    }

    /** Crée une réservation, ou réalise immédiatement un achat direct. */
    public static Resultat creer(UserEJB utilisateur, String idClient, String idSeance,
            Timestamp expirationDemandee, Collection<String> places, boolean achatDirect,
            String idModePaiement) throws Exception {
        String user = utilisateurId(utilisateur);
        if (vide(idClient) || vide(idSeance)) throw new IllegalArgumentException("Client et séance obligatoires.");
        List<String> idsPlaces = normaliserPlaces(places);
        if (achatDirect && vide(idModePaiement)) throw new IllegalArgumentException("Le mode de paiement est obligatoire pour l'achat direct.");

        try (Connection c = new UtilDB().GetConn()) {
            c.setAutoCommit(false);
            try {
                expirerReservations(c);
                SeanceInfo seance = verrouillerSeance(c, idSeance.trim());
                Timestamp maintenant = new Timestamp(System.currentTimeMillis());
                if (achatDirect) {
                    if (!seance.fin.after(maintenant)) throw new IllegalArgumentException("La séance est terminée.");
                } else if (!seance.debut.after(maintenant)) {
                    throw new IllegalArgumentException("La séance a déjà commencé.");
                }
                verifierClient(c, idClient.trim());

                List<PlaceInfo> lignes = verrouillerEtChargerPlaces(c, idSeance.trim(), seance.idSalle, idsPlaces);
                appliquerRemise(c, lignes);
                Timestamp expiration = null;
                if (!achatDirect) {
                    expiration = expirationDemandee == null ? seance.debut : expirationDemandee;
                    if (!expiration.after(maintenant) || expiration.after(seance.debut)) {
                        throw new IllegalArgumentException("L'expiration doit être future et au plus tard au début de la séance.");
                    }
                }

                Reservation reservation = new Reservation();
                reservation.setMode("insert");
                reservation.setIdClient(idClient.trim());
                reservation.setIdSeance(idSeance.trim());
                reservation.setDateReservation(maintenant);
                reservation.setDateExpiration(expiration);
                reservation.setEtat(1);
                ReservationFille[] filles = new ReservationFille[lignes.size()];
                for (int i = 0; i < lignes.size(); i++) {
                    PlaceInfo p = lignes.get(i);
                    ReservationFille fille = new ReservationFille();
                    fille.setMode("insert");
                    fille.setIdSeance(idSeance.trim());
                    fille.setIdPlace(p.idPlace);
                    fille.setPrixUnitaire(p.prix.doubleValue());
                    fille.setMontantRemise(p.remise.doubleValue());
                    fille.setActif(1);
                    filles[i] = fille;
                }
                reservation.setFille(filles);
                reservation.createObject(user, c);

                String idVente = null;
                if (achatDirect) {
                    idVente = creerVenteEtEncaisser(c, user, reservation, lignes, idModePaiement.trim());
                    reservation.setEtat(11);
                    reservation.updateToTableWithHisto(user, c);
                }
                c.commit();
                return new Resultat(reservation.getId(), idVente);
            } catch (Exception e) {
                c.rollback();
                throw e;
            }
        }
    }

    /** Validation et paiement total d'une réservation encore en attente. */
    public static Resultat payer(UserEJB utilisateur, String idReservation, String idModePaiement) throws Exception {
        String user = utilisateurId(utilisateur);
        if (vide(idReservation) || vide(idModePaiement)) throw new IllegalArgumentException("Réservation et mode de paiement obligatoires.");
        try (Connection c = new UtilDB().GetConn()) {
            c.setAutoCommit(false);
            try {
                expirerReservations(c);
                Reservation reservation = chargerReservationVerrouillee(c, idReservation.trim());
                if (reservation.getEtat() != 1) throw new IllegalArgumentException("Cette réservation n'est plus en attente.");
                Timestamp maintenant = new Timestamp(System.currentTimeMillis());
                if (reservation.getDateExpiration() == null || !reservation.getDateExpiration().after(maintenant)) {
                    annulerEnBase(user, c, reservation);
                    c.commit();
                    throw new IllegalArgumentException("La réservation a expiré.");
                }
                SeanceInfo seance = verrouillerSeance(c, reservation.getIdSeance());
                if (!seance.debut.after(maintenant)) throw new IllegalArgumentException("La séance a déjà commencé.");
                List<PlaceInfo> places = chargerPlacesReservation(c, reservation.getId());
                if (places.isEmpty()) throw new IllegalArgumentException("La réservation ne contient aucune place active.");
                appliquerRemiseDepuisMontants(places);
                String idVente = creerVenteEtEncaisser(c, user, reservation, places, idModePaiement.trim());
                reservation.setEtat(11);
                reservation.updateToTableWithHisto(user, c);
                c.commit();
                return new Resultat(reservation.getId(), idVente);
            } catch (Exception e) {
                if (!c.getAutoCommit()) c.rollback();
                throw e;
            }
        }
    }

    public static void annuler(UserEJB utilisateur, String idReservation) throws Exception {
        String user = utilisateurId(utilisateur);
        if (vide(idReservation)) throw new IllegalArgumentException("L'identifiant de réservation est obligatoire.");
        try (Connection c = new UtilDB().GetConn()) {
            c.setAutoCommit(false);
            try {
                expirerReservations(c);
                Reservation reservation = chargerReservationVerrouillee(c, idReservation.trim());
                if (reservation.getEtat() != 1 || reservation.getDateExpiration() == null) {
                    throw new IllegalArgumentException("Seule une réservation en attente peut être annulée.");
                }
                Timestamp maintenant = new Timestamp(System.currentTimeMillis());
                SeanceInfo seance = verrouillerSeance(c, reservation.getIdSeance());
                if (!reservation.getDateExpiration().after(maintenant)) throw new IllegalArgumentException("La réservation a expiré.");
                if (!seance.debut.after(maintenant)) throw new IllegalArgumentException("La séance a déjà commencé.");
                annulerEnBase(user, c, reservation);
                c.commit();
            } catch (Exception e) {
                c.rollback();
                throw e;
            }
        }
    }

    /** Appelé par le job SQL et avant les opérations de réservation/vente. */
    public static void expirerReservations(Connection c) throws Exception {
        try (java.sql.Statement st = c.createStatement()) {
            st.execute("BEGIN CINE_LIBERER_EXPIREES; END;");
        }
    }

    private static String creerVenteEtEncaisser(Connection c, String user, Reservation reservation,
            List<PlaceInfo> places, String idModePaiement) throws Exception {
        BigDecimal brut = BigDecimal.ZERO.setScale(2);
        BigDecimal remise = BigDecimal.ZERO.setScale(2);
        for (PlaceInfo p : places) {
            brut = brut.add(p.prix);
            remise = remise.add(p.remise);
        }
        BigDecimal net = brut.subtract(remise).setScale(2, RoundingMode.HALF_UP);
        if (net.signum() <= 0) throw new IllegalArgumentException("Le montant à payer doit être positif.");

        Vente vente = new Vente();
        vente.setMode("insert");
        vente.setIdClient(reservation.getIdClient());
        vente.setIdMagasin(MAGASIN_CINEMA);
        vente.setIdOrigine(reservation.getId());
        vente.setDesignation("Billets cinéma - réservation " + reservation.getId());
        vente.setDaty(new Date(System.currentTimeMillis()));
        vente.setDatyPrevu(vente.getDaty());
        vente.setEstPrevu(0);
        vente.setIdModePaiement(idModePaiement);

        // Vente.createObject refuse deux lignes portant le même produit. La facture
        // porte donc une ligne agrégée; le détail par siège et la remise restent dans CINE_RESERVATION_FILLE.
        VenteDetailsArbiochem ligne = new VenteDetailsArbiochem();
        ligne.setMode("insert");
        ligne.setIdProduit(PRODUIT_BILLET);
        ligne.setDesignation("Billets cinéma - " + places.size() + " place(s), réservation " + reservation.getId());
        ligne.setQte(1);
        ligne.setPu(brut.doubleValue());
        ligne.setPuVente(brut.doubleValue());
        ligne.setTva(0);
        ligne.setMontant(net.doubleValue());
        ligne.setIdDevise(DEVISE);
        ligne.setTauxDeChange(1);
        ligne.setCompte(COMPTE_VENTE);
        ligne.setRemiseArbiochem(remise.doubleValue());
        ligne.setRistourneArbiochem(0);
        vente.setFille(new VenteDetails[] { ligne });
        vente.createObject(user, c);
        vente.validerObject(user, c);

        String idDetail;
        try (PreparedStatement ps = c.prepareStatement("SELECT ID FROM VENTE_DETAILS WHERE IDVENTE = ?")) {
            ps.setString(1, vente.getId());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new Exception("La ligne de facture du billet est introuvable.");
                idDetail = rs.getString(1);
                if (rs.next()) throw new Exception("La facture cinéma devait contenir une ligne agrégée.");
            }
        }

        MvtCaisse mouvement = new MvtCaisse();
        mouvement.setMode("insert");
        mouvement.setDesignation("Encaissement billets cinéma - " + reservation.getId());
        mouvement.setIdOrigine(vente.getId());
        mouvement.setIdVenteDetail(idDetail);
        mouvement.setIdCaisse(CAISSE_CINEMA);
        mouvement.setIdTiers(reservation.getIdClient());
        mouvement.setDaty(new Date(System.currentTimeMillis()));
        mouvement.setDebit(0);
        mouvement.setCredit(net.doubleValue());
        mouvement.setIdDevise(DEVISE);
        mouvement.setTaux(1);
        mouvement.setIdModePaiement(idModePaiement);
        mouvement.createObject(user, c);
        mouvement.validerObject(user, c);
        return vente.getId();
    }

    private static void appliquerRemise(Connection c, List<PlaceInfo> places) throws Exception {
        String sql = "SELECT ID, CAT_RESERVE, NB_PLACES, CAT_REMISE, VAL FROM CINE_REMISE ORDER BY ID";
        RemiseCandidate meilleure = null;
        try (PreparedStatement ps = c.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String catReserve = rs.getString("CAT_RESERVE");
                String catRemise = rs.getString("CAT_REMISE");
                int requis = rs.getInt("NB_PLACES");
                BigDecimal taux = rs.getBigDecimal("VAL");
                int nombreReserve = 0;
                PlaceInfo cible = null;
                for (PlaceInfo p : places) {
                    if (catReserve.equals(p.idCategorie)) nombreReserve++;
                    if (catRemise.equals(p.idCategorie) && (cible == null || p.prix.compareTo(cible.prix) > 0)) cible = p;
                }
                if (nombreReserve < requis || cible == null) continue;
                BigDecimal montant = cible.prix.multiply(taux).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
                if (meilleure == null || montant.compareTo(meilleure.montant) > 0) {
                    meilleure = new RemiseCandidate(cible, montant);
                }
            }
        }
        if (meilleure != null) meilleure.place.remise = meilleure.montant;
    }

    private static void appliquerRemiseDepuisMontants(List<PlaceInfo> places) {
        // Le prix et la remise historisés à la création sont conservés pour le paiement.
    }

    private static List<PlaceInfo> chargerPlacesReservation(Connection c, String idReservation) throws Exception {
        String sql = "SELECT f.IDPLACE, f.PRIXUNITAIRE, f.MONTANTREMISE, p.IDCATEGORIE, p.NUMERO "
                + "FROM CINE_RESERVATION_FILLE f JOIN CINE_PLACE p ON p.ID = f.IDPLACE "
                + "WHERE f.IDRESERVATION = ? AND f.ACTIF = 1 ORDER BY f.IDPLACE";
        List<PlaceInfo> resultat = new ArrayList<PlaceInfo>();
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, idReservation);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    PlaceInfo p = new PlaceInfo();
                    p.idPlace = rs.getString("IDPLACE");
                    p.idCategorie = rs.getString("IDCATEGORIE");
                    p.numero = rs.getString("NUMERO");
                    p.prix = rs.getBigDecimal("PRIXUNITAIRE").setScale(2, RoundingMode.HALF_UP);
                    p.remise = rs.getBigDecimal("MONTANTREMISE").setScale(2, RoundingMode.HALF_UP);
                    resultat.add(p);
                }
            }
        }
        return resultat;
    }

    private static List<PlaceInfo> verrouillerEtChargerPlaces(Connection c, String idSeance,
            String idSalle, List<String> ids) throws Exception {
        List<PlaceInfo> resultat = new ArrayList<PlaceInfo>();
        String lock = "SELECT ID FROM CINE_PLACE WHERE ID = ? FOR UPDATE";
        String sql = "SELECT p.ID, p.IDCATEGORIE, p.NUMERO, cp.PRIX FROM CINE_PLACE p "
                + "JOIN CINE_CATEGORIE_PLACE cp ON cp.ID = p.IDCATEGORIE "
                + "WHERE p.ID = ? AND p.IDSALLE = ?";
        for (String id : ids) {
            try (PreparedStatement ps = c.prepareStatement(lock)) {
                ps.setString(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) throw new IllegalArgumentException("Une place sélectionnée n'existe plus.");
                }
            }
            PlaceInfo p = new PlaceInfo();
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setString(1, id);
                ps.setString(2, idSalle);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) throw new IllegalArgumentException("Une place ne fait pas partie de la salle de la séance.");
                    p.idPlace = rs.getString("ID");
                    p.idCategorie = rs.getString("IDCATEGORIE");
                    p.numero = rs.getString("NUMERO");
                    p.prix = rs.getBigDecimal("PRIX").setScale(2, RoundingMode.HALF_UP);
                    p.remise = BigDecimal.ZERO.setScale(2);
                }
            }
            try (PreparedStatement ps = c.prepareStatement(
                    "SELECT 1 FROM CINE_RESERVATION_FILLE WHERE IDSEANCE = ? AND IDPLACE = ? AND ACTIF = 1 AND ROWNUM = 1")) {
                ps.setString(1, idSeance);
                ps.setString(2, id);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) throw new IllegalArgumentException("La place " + p.numero + " vient d'être réservée.");
                }
            }
            resultat.add(p);
        }
        return resultat;
    }

    private static SeanceInfo verrouillerSeance(Connection c, String id) throws Exception {
        String sql = "SELECT s.IDSALLE, s.DATEHEUREDEBUT, s.DATEHEUREFIN, s.ETAT "
                + "FROM CINE_SEANCE s WHERE s.ID = ? FOR UPDATE";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new IllegalArgumentException("La séance est introuvable.");
                if (rs.getInt("ETAT") == 0) throw new IllegalArgumentException("La séance est annulée.");
                SeanceInfo info = new SeanceInfo();
                info.idSalle = rs.getString("IDSALLE");
                info.debut = rs.getTimestamp("DATEHEUREDEBUT");
                info.fin = rs.getTimestamp("DATEHEUREFIN");
                return info;
            }
        }
    }

    private static void verifierClient(Connection c, String id) throws Exception {
        try (PreparedStatement ps = c.prepareStatement("SELECT ID FROM CLIENT WHERE ID = ?")) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new IllegalArgumentException("Le client sélectionné est introuvable.");
            }
        }
    }

    private static Reservation chargerReservationVerrouillee(Connection c, String id) throws Exception {
        Reservation reservation = new Reservation();
        try (PreparedStatement ps = c.prepareStatement(
                "SELECT ID, IDCLIENT, IDSEANCE, DATERESERVATION, DATEEXPIRATION, ETAT FROM CINE_RESERVATION WHERE ID = ? FOR UPDATE")) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new IllegalArgumentException("La réservation demandée est introuvable.");
                reservation.setId(rs.getString("ID"));
                reservation.setIdClient(rs.getString("IDCLIENT"));
                reservation.setIdSeance(rs.getString("IDSEANCE"));
                reservation.setDateReservation(rs.getTimestamp("DATERESERVATION"));
                reservation.setDateExpiration(rs.getTimestamp("DATEEXPIRATION"));
                reservation.setEtat(rs.getInt("ETAT"));
            }
        }
        return reservation;
    }

    private static void annulerEnBase(String user, Connection c, Reservation reservation) throws Exception {
        reservation.setEtat(0);
        reservation.updateToTableWithHisto(user, c);
        // TRG_CINE_RESERVATION_LIBERER conserve les lignes et libère immédiatement les places.
    }

    private static List<String> normaliserPlaces(Collection<String> places) {
        if (places == null || places.isEmpty()) throw new IllegalArgumentException("Sélectionnez au moins une place.");
        Set<String> uniques = new HashSet<String>();
        for (String place : places) {
            if (vide(place) || !uniques.add(place.trim())) throw new IllegalArgumentException("La sélection contient une place vide ou répétée.");
        }
        List<String> resultat = new ArrayList<String>(uniques);
        Collections.sort(resultat, new Comparator<String>() {
            public int compare(String a, String b) { return a.compareTo(b); }
        });
        return resultat;
    }

    private static String utilisateurId(UserEJB u) {
        if (u == null || u.getUser() == null) throw new IllegalStateException("La session utilisateur a expiré.");
        return String.valueOf(u.getUser().getRefuser());
    }
    private static boolean vide(String s) { return s == null || s.trim().isEmpty(); }
    private static String sql(String s) { return s.replace("'", "''"); }

    private static final class SeanceInfo { String idSalle; Timestamp debut; Timestamp fin; }
    private static final class PlaceInfo {
        String idPlace; String idCategorie; String numero;
        BigDecimal prix = BigDecimal.ZERO.setScale(2);
        BigDecimal remise = BigDecimal.ZERO.setScale(2);
    }
    private static final class RemiseCandidate {
        final PlaceInfo place; final BigDecimal montant;
        RemiseCandidate(PlaceInfo place, BigDecimal montant) { this.place = place; this.montant = montant; }
    }
}
