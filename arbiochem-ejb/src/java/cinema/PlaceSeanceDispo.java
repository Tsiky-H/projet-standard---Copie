package cinema;

import bean.ClassMAPTable;

/** Modèle de lecture de la vue CINE_PLACE_SEANCE_DISPO. */
public class PlaceSeanceDispo extends ClassMAPTable {
    private String id;
    private String idSeance;
    private String idPlace;
    private String idSalle;
    private String idCategorie;
    private String numero;
    private String idCategorieLib;
    private double prix;
    private int disponible;

    public PlaceSeanceDispo() { setNomTable("CINE_PLACE_SEANCE_DISPO"); }

    @Override
    public String getTuppleID() { return id; }

    @Override
    public String getAttributIDName() { return "id"; }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getIdSeance() { return idSeance; }
    public void setIdSeance(String idSeance) { this.idSeance = idSeance; }
    public String getIdPlace() { return idPlace; }
    public void setIdPlace(String idPlace) { this.idPlace = idPlace; }
    public String getIdSalle() { return idSalle; }
    public void setIdSalle(String idSalle) { this.idSalle = idSalle; }
    public String getIdCategorie() { return idCategorie; }
    public void setIdCategorie(String idCategorie) { this.idCategorie = idCategorie; }
    public String getNumero() { return numero; }
    public void setNumero(String numero) { this.numero = numero; }
    public String getIdCategorieLib() { return idCategorieLib; }
    public void setIdCategorieLib(String idCategorieLib) { this.idCategorieLib = idCategorieLib; }
    public double getPrix() { return prix; }
    public void setPrix(double prix) { this.prix = prix; }
    public int getDisponible() { return disponible; }
    public void setDisponible(int disponible) { this.disponible = disponible; }
}
