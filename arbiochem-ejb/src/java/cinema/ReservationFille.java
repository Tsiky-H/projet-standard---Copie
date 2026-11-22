package cinema;

import bean.ClassFille;
import java.sql.Connection;

/** Modèle de données pour CINE_RESERVATION_FILLE. */
public class ReservationFille extends ClassFille {
    private String id;
    private String idReservation;
    private String idSeance;
    private String idPlace;
    private double prixUnitaire;
    private double montantRemise;
    private int actif = 1;

    public ReservationFille() throws Exception {
        setNomTable("CINE_RESERVATION_FILLE");
        setLiaisonMere("idReservation");
        setNomClasseMere("cinema.Reservation");
    }

    @Override
    public String getTuppleID() { return id; }

    @Override
    public String getAttributIDName() { return "id"; }

    @Override
    public String getLiaisonMere() { return "idReservation"; }

    @Override
    public String getNomClasseMere() { return "cinema.Reservation"; }

    @Override
    public void construirePK(Connection c) throws Exception {
        preparePk("CRSF", "getSeqCineReservationFille");
        setId(makePK(c));
    }

    public void controler(Connection c) throws Exception {
        if (idReservation == null || idReservation.trim().isEmpty()) {
            throw new Exception("La réservation de la ligne est obligatoire.");
        }
        if (idSeance == null || idSeance.trim().isEmpty()) {
            throw new Exception("La séance de la ligne est obligatoire.");
        }
        if (idPlace == null || idPlace.trim().isEmpty()) {
            throw new Exception("La place est obligatoire.");
        }
        if (prixUnitaire < 0) {
            throw new Exception("Le prix unitaire ne peut pas être négatif.");
        }
        if (montantRemise < 0 || montantRemise > prixUnitaire) {
            throw new Exception("Le montant de remise doit être compris entre zéro et le prix unitaire.");
        }
        if (actif != 0 && actif != 1) {
            throw new Exception("L'indicateur de disponibilité de la place doit valoir 0 ou 1.");
        }
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getIdReservation() { return idReservation; }
    public void setIdReservation(String idReservation) { this.idReservation = idReservation; }
    public String getIdSeance() { return idSeance; }
    public void setIdSeance(String idSeance) { this.idSeance = idSeance; }
    public String getIdPlace() { return idPlace; }
    public void setIdPlace(String idPlace) { this.idPlace = idPlace; }
    public double getPrixUnitaire() { return prixUnitaire; }
    public void setPrixUnitaire(double prixUnitaire) { this.prixUnitaire = prixUnitaire; }
    public double getMontantRemise() { return montantRemise; }
    public void setMontantRemise(double montantRemise) { this.montantRemise = montantRemise; }
    public int getActif() { return actif; }
    public void setActif(int actif) { this.actif = actif; }
}
