package cinema;

import bean.ClassMAPTable;
import java.sql.Connection;

/** Modèle de données pour CINE_FILM. */
public class Film extends ClassMAPTable {
    private String id;
    private String titre;
    private String realisateur;
    private int duree;
    private String genre;
    private int classification;

    public Film() {
        setNomTable("CINE_FILM");
    }

    @Override
    public String getTuppleID() { return id; }

    @Override
    public String getAttributIDName() { return "id"; }

    @Override
    public void construirePK(Connection c) throws Exception {
        preparePk("CFLM", "getSeqCineFilm");
        setId(makePK(c));
    }

    public void controler(Connection c) throws Exception {
        if (titre == null || titre.trim().isEmpty()) {
            throw new Exception("Le titre du film est obligatoire.");
        }
        if (duree <= 0) {
            throw new Exception("La durée du film doit être supérieure à zéro.");
        }
        if (classification < 0) {
            throw new Exception("La classification ne peut pas être négative.");
        }
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }
    public String getRealisateur() { return realisateur; }
    public void setRealisateur(String realisateur) { this.realisateur = realisateur; }
    public int getDuree() { return duree; }
    public void setDuree(int duree) { this.duree = duree; }
    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }
    public int getClassification() { return classification; }
    public void setClassification(int classification) { this.classification = classification; }
}
