package cinema;

import cinema.Seance;

/** Modèle de lecture de la vue CINE_SEANCE_LIB. */
public class SeanceLib extends Seance {
    private String idFilmLib;
    private String idSalleLib;
    private String libelle;
    private int nbPlaces;
    private int placesPrises;
    private int placesRestantes;

    public SeanceLib() { setNomTable("CINE_SEANCE_LIB"); }

    public String getChaineEtat() { return chaineEtat(getEtat()); }

    public String getIdFilmLib() { return idFilmLib; }
    public void setIdFilmLib(String idFilmLib) { this.idFilmLib = idFilmLib; }
    public String getIdSalleLib() { return idSalleLib; }
    public void setIdSalleLib(String idSalleLib) { this.idSalleLib = idSalleLib; }
    public String getLibelle() { return libelle; }
    public void setLibelle(String libelle) { this.libelle = libelle; }
    public int getNbPlaces() { return nbPlaces; }
    public void setNbPlaces(int nbPlaces) { this.nbPlaces = nbPlaces; }
    public int getPlacesPrises() { return placesPrises; }
    public void setPlacesPrises(int placesPrises) { this.placesPrises = placesPrises; }
    public int getPlacesRestantes() { return placesRestantes; }
    public void setPlacesRestantes(int placesRestantes) { this.placesRestantes = placesRestantes; }
}
