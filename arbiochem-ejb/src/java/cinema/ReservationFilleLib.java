package cinema;

import cinema.ReservationFille;

/** Modèle de lecture de la vue CINE_RESERVATION_FILLE_LIB. */
public class ReservationFilleLib extends ReservationFille {
    private String idPlaceLib;
    private String idCategorieLib;
    private double montantNet;

    public ReservationFilleLib() throws Exception { setNomTable("CINE_RESERVATION_FILLE_LIB"); }

    public String getIdPlaceLib() { return idPlaceLib; }
    public void setIdPlaceLib(String idPlaceLib) { this.idPlaceLib = idPlaceLib; }
    public String getIdCategorieLib() { return idCategorieLib; }
    public void setIdCategorieLib(String idCategorieLib) { this.idCategorieLib = idCategorieLib; }
    public double getMontantNet() { return montantNet; }
    public void setMontantNet(double montantNet) { this.montantNet = montantNet; }
}
