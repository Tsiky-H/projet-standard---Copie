import caisse.MvtCaisse;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import utilitaire.UtilDB;
import utilitaire.Utilitaire;
import vente.Vente;
import vente.VenteDetails;
import vente.VenteDetailsArbiochem;

public class TestVenteCinema {
    private static final String ID_CLIENT = "CLIA0934";
    private static final String ID_UTILISATEUR = "1060";
    private static final String ID_MAGASIN = "MAG_CINEMA";
    private static final String ID_CAISSE = "CAI_CINEMA";
    private static final String ID_MODE_ESPECES = "0002";
    private static final double PRIX_TEST = 1000.0;

    public static void main(String[] args) throws Exception {
        Connection c = new UtilDB().GetConn();
        try (PreparedStatement ps = c.prepareStatement(
                "SELECT USER, SYS_CONTEXT('USERENV','DB_NAME'), " +
                        "(SELECT COUNT(*) FROM AS_INGREDIENTS WHERE ID = 'CINE_BILLET') " +
                        "FROM DUAL");
                ResultSet rs = ps.executeQuery()) {
            rs.next();
            System.out.println("Schéma Oracle : " + rs.getString(1));
            System.out.println("Base Oracle : " + rs.getString(2));
            System.out.println("Billet visible : " + rs.getInt(3));
        }
        c.setAutoCommit(false);

        try {
            Date aujourdHui = Utilitaire.dateDuJourSql();

            Vente vente = new Vente();
            vente.setMode("insert");
            vente.setIdClient(ID_CLIENT);
            vente.setIdMagasin(ID_MAGASIN);
            vente.setDesignation("TEST CINEMA - billet");
            vente.setDaty(aujourdHui);
            vente.setDatyPrevu(aujourdHui);
            vente.setEstPrevu(0);
            vente.setIdModePaiement(ID_MODE_ESPECES);

            VenteDetailsArbiochem ligne = new VenteDetailsArbiochem();
            ligne.setMode("insert");
            ligne.setIdProduit("CINE_BILLET");
            ligne.setDesignation("Billet de cinema - test");
            ligne.setQte(1);
            ligne.setPu(PRIX_TEST);
            ligne.setPuVente(PRIX_TEST);
            ligne.setTva(0);
            ligne.setMontant(PRIX_TEST);
            ligne.setIdDevise("AR");
            ligne.setTauxDeChange(1);
            ligne.setCompte("706");
            ligne.setRemiseArbiochem(0);
            ligne.setRistourneArbiochem(0);

            vente.setFille(new VenteDetails[] { ligne });

            vente.createObject(ID_UTILISATEUR, c);
            String detailSql = "SELECT ID, IDVENTE, IDPRODUIT, MONTANT, PU, PUVente, QTE, TVA, " +
                    "REMISE, IDREMISE, RISTOURNE " +
                    "FROM VENTE_DETAILS WHERE IDVENTE = ?";
            try (PreparedStatement ps = c.prepareStatement(detailSql)) {
                ps.setString(1, vente.getId());
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        throw new Exception("La ligne VENTE_DETAILS créée est introuvable.");
                    }
                    System.out.println(
                            "VENTE_DETAILS : ID=" + rs.getString("ID")
                                    + ", IDPRODUIT=" + rs.getString("IDPRODUIT")
                                    + ", MONTANT=" + rs.getDouble("MONTANT")
                                    + ", PU=" + rs.getDouble("PU")
                                    + ", PUVente=" + rs.getDouble("PUVENTE")
                                    + ", QTE=" + rs.getDouble("QTE")
                                    + ", TVA=" + rs.getDouble("TVA")
                                    + ", REMISE=" + rs.getDouble("REMISE")
                                    + ", IDREMISE=" + rs.getString("IDREMISE")
                                    + ", RISTOURNE=" + rs.getDouble("RISTOURNE"));
                    if (rs.next()) {
                        throw new Exception("Le test devait créer une seule ligne de vente.");
                    }
                }
            }
            String calculSql = "SELECT ID, IDPRODUIT, QTE, PU, COMPTE, TVA, MONTANTHT, MONTANTTVA, MONTANTTTC " +
                    "FROM VENTEDETCALC WHERE IDVENTE = ?";
            try (PreparedStatement ps = c.prepareStatement(calculSql)) {
                ps.setString(1, vente.getId());
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        System.out.println(
                                "VENTEDETCALC : produit=" + rs.getString("IDPRODUIT")
                                        + ", QTE=" + rs.getDouble("QTE")
                                        + ", PU=" + rs.getDouble("PU")
                                        + ", COMPTE=" + rs.getString("COMPTE")
                                        + ", TVA=" + rs.getDouble("TVA")
                                        + ", HT=" + rs.getDouble("MONTANTHT")
                                        + ", TVA calculee=" + rs.getDouble("MONTANTTVA")
                                        + ", TTC=" + rs.getDouble("MONTANTTTC"));
                    }
                }
            }
            String sql = "SELECT IDVENTE, COMPTE, MONTANTHT, MONTANTTVA, MONTANTTTC " +
                    "FROM VENTE_COMPTA_GEN WHERE IDVENTE = ?";

            try (PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setString(1, vente.getId());
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        System.out.println(
                                "Ligne comptable : compte=" + rs.getString("COMPTE")
                                        + ", HT=" + rs.getDouble("MONTANTHT")
                                        + ", TVA=" + rs.getDouble("MONTANTTVA")
                                        + ", TTC=" + rs.getDouble("MONTANTTTC"));
                    }
                }
            }
            vente.validerObject(ID_UTILISATEUR, c);
            String idDetail = lireIdDetail(c, vente.getId());

            MvtCaisse mouvement = new MvtCaisse();
            mouvement.setMode("insert");
            mouvement.setDesignation("Encaissement test billet cinema");
            mouvement.setIdOrigine(vente.getId());
            mouvement.setIdVenteDetail(idDetail);
            mouvement.setIdCaisse(ID_CAISSE);
            mouvement.setIdTiers(ID_CLIENT);
            mouvement.setDaty(aujourdHui);
            mouvement.setCredit(PRIX_TEST);
            mouvement.setIdDevise("AR");
            mouvement.setTaux(1);
            mouvement.setIdModePaiement(ID_MODE_ESPECES);

            mouvement.createObject(ID_UTILISATEUR, c);
            mouvement.validerObject(ID_UTILISATEUR, c);

            verifier(c, "VENTE", "ID", vente.getId(), 1);
            verifier(c, "VENTE_DETAILS", "IDVENTE", vente.getId(), 1);
            verifier(c, "MOUVEMENTCAISSE", "IDVENTEDETAIL", idDetail, 1);

            verifier(c, "MVTSTOCK", "IDVENTE", vente.getId(), 0);
            verifier(c, "STOCK_ENGAGE", "IDVENTE", vente.getId(), 0);
            verifier(c, "LIVRAISON", "IDVENTE", vente.getId(), 0);
            verifier(c, "AS_BONDELIVRAISON_CLIENT", "IDVENTE", vente.getId(), 0);
            verifier(c, "MVTSTOCKFILLE", "IDVENTEDETAIL", idDetail, 0);
            verifier(c, "STOCK_ENGAGE_FILLE", "IDVENTEDETAIL", idDetail, 0);
            verifier(c, "AS_BONDELIVRAISON_CLIENT_FILLE", "IDVENTEDETAIL", idDetail, 0);

            c.commit();
            System.out.println("TEST OK — vente " + vente.getId());
        } catch (Exception e) {
            c.rollback();
            throw e;
        } finally {
            c.close();
        }
    }

    private static String lireIdDetail(Connection c, String idVente) throws Exception {
        String sql = "SELECT ID FROM VENTE_DETAILS WHERE IDVENTE = ?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, idVente);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new Exception("La ligne VENTE_DETAILS est introuvable.");
                }
                String id = rs.getString(1);
                if (rs.next()) {
                    throw new Exception("Le test devait créer une seule ligne de vente.");
                }
                return id;
            }
        }
    }

    private static void verifier(
            Connection c, String table, String colonne, String valeur, int attendu) throws Exception {
        String sql = "SELECT COUNT(*) FROM " + table + " WHERE " + colonne + " = ?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, valeur);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                int obtenu = rs.getInt(1);
                if (obtenu != attendu) {
                    throw new Exception(
                            table + " : attendu " + attendu + ", obtenu " + obtenu);
                }
                System.out.println(table + " : " + obtenu);
            }
        }
    }
}
