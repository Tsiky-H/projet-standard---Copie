package cinema;

import bean.ClassMAPTable;
import java.sql.Connection;

/** Modèle de données pour CINE_SALLE. */
public class Salle extends ClassMAPTable {
    private String id;
    private String libelle;

    public Salle() {
        setNomTable("CINE_SALLE");
    }

    @Override
    public String getTuppleID() { return id; }

    @Override
    public String getAttributIDName() { return "id"; }

    @Override
    public void construirePK(Connection c) throws Exception {
        preparePk("CSAL", "getSeqCineSalle");
        setId(makePK(c));
    }

    public void controler(Connection c) throws Exception {
        if (libelle == null || libelle.trim().isEmpty()) {
            throw new Exception("Le libellé de la salle est obligatoire.");
        }
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getLibelle() { return libelle; }
    public void setLibelle(String libelle) { this.libelle = libelle; }
}
