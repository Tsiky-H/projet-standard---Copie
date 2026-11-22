package cinema;

import cinema.Reservation;

/** Modèle de lecture de la vue CINE_RESERVATION_LIB. */
public class ReservationLib extends Reservation {
    private String idClientLib;
    private String idSeanceLib;
    private String filmLib;
    private String salleLib;
    private java.sql.Timestamp seanceDebut;
    private java.sql.Timestamp seanceFin;
    private int nbPlaces;
    private double montantBrut;
    private double montantRemise;
    private double montantTotal;
    private String typeLib;

    public ReservationLib() throws Exception { setNomTable("CINE_RESERVATION_LIB"); }

    public String getIdClientLib() { return idClientLib; }
    public void setIdClientLib(String idClientLib) { this.idClientLib = idClientLib; }
    public String getIdSeanceLib() { return idSeanceLib; }
    public void setIdSeanceLib(String idSeanceLib) { this.idSeanceLib = idSeanceLib; }
    public String getFilmLib() { return filmLib; }
    public void setFilmLib(String filmLib) { this.filmLib = filmLib; }
    public String getSalleLib() { return salleLib; }
    public void setSalleLib(String salleLib) { this.salleLib = salleLib; }
    public java.sql.Timestamp getSeanceDebut() { return seanceDebut; }
    public void setSeanceDebut(java.sql.Timestamp seanceDebut) { this.seanceDebut = seanceDebut; }
    public java.sql.Timestamp getSeanceFin() { return seanceFin; }
    public void setSeanceFin(java.sql.Timestamp seanceFin) { this.seanceFin = seanceFin; }
    public int getNbPlaces() { return nbPlaces; }
    public void setNbPlaces(int nbPlaces) { this.nbPlaces = nbPlaces; }
    public double getMontantBrut() { return montantBrut; }
    public void setMontantBrut(double montantBrut) { this.montantBrut = montantBrut; }
    public double getMontantRemise() { return montantRemise; }
    public void setMontantRemise(double montantRemise) { this.montantRemise = montantRemise; }
    public double getMontantTotal() { return montantTotal; }
    public void setMontantTotal(double montantTotal) { this.montantTotal = montantTotal; }
    public String getChaineEtat() { return chaineEtat(getEtat()); }
    public String getTypeLib() { return typeLib; }
    public void setTypeLib(String typeLib) { this.typeLib = typeLib; }
}
