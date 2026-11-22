package cinema;

import bean.ClassMAPTable;
import java.sql.Connection;

/** Modèle de données pour CINE_REMISE. */
public class Remise extends ClassMAPTable {
    private String id;
    private String catReserve;
    private int nbPlaces;
    private String catRemise;
    private double val;

    public Remise() {
        setNomTable("CINE_REMISE");
    }

    @Override
    public String getTuppleID() { return id; }

    @Override
    public String getAttributIDName() { return "id"; }

    @Override
    public void construirePK(Connection c) throws Exception {
        preparePk("CRMS", "getSeqCineRemise");
        setId(makePK(c));
    }

    public void controler(Connection c) throws Exception {
        if (catReserve == null || catReserve.trim().isEmpty()
                || catRemise == null || catRemise.trim().isEmpty()) {
            throw new Exception("Les catégories concernées par la remise sont obligatoires.");
        }
        if (nbPlaces < 1) {
            throw new Exception("Le nombre de places requis doit être supérieur ou égal à un.");
        }
        if (val <= 0 || val > 100) {
            throw new Exception("La remise doit être un pourcentage supérieur à zéro et inférieur ou égal à 100.");
        }
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getCatReserve() { return catReserve; }
    public void setCatReserve(String catReserve) { this.catReserve = catReserve; }
    public int getNbPlaces() { return nbPlaces; }
    public void setNbPlaces(int nbPlaces) { this.nbPlaces = nbPlaces; }
    public String getCatRemise() { return catRemise; }
    public void setCatRemise(String catRemise) { this.catRemise = catRemise; }
    public double getVal() { return val; }
    public void setVal(double val) { this.val = val; }
}
