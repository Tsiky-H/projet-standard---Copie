package cinema;

import cinema.Place;

/** Modèle de lecture de la vue CINE_PLACE_LIB. */
public class PlaceLib extends Place {
    private String idSalleLib;
    private String idCategorieLib;
    private double prix;

    public PlaceLib() { setNomTable("CINE_PLACE_LIB"); }

    public String getIdSalleLib() { return idSalleLib; }
    public void setIdSalleLib(String idSalleLib) { this.idSalleLib = idSalleLib; }
    public String getIdCategorieLib() { return idCategorieLib; }
    public void setIdCategorieLib(String idCategorieLib) { this.idCategorieLib = idCategorieLib; }
    public double getPrix() { return prix; }
    public void setPrix(double prix) { this.prix = prix; }
}
