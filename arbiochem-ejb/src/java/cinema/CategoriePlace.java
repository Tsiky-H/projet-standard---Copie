package cinema;

import bean.ClassMAPTable;
import java.sql.Connection;

/** Modèle de données pour CINE_CATEGORIE_PLACE. */
public class CategoriePlace extends ClassMAPTable {
    private String id;
    private String val;
    private double prix;

    public CategoriePlace() {
        setNomTable("CINE_CATEGORIE_PLACE");
    }

    @Override
    public String getTuppleID() { return id; }

    @Override
    public String getAttributIDName() { return "id"; }

    @Override
    public void construirePK(Connection c) throws Exception {
        preparePk("CCAT", "getSeqCineCategoriePlace");
        setId(makePK(c));
    }

    public void controler(Connection c) throws Exception {
        if (val == null || val.trim().isEmpty()) {
            throw new Exception("Le nom de la catégorie de place est obligatoire.");
        }
        if (prix < 0) {
            throw new Exception("Le prix d'une catégorie ne peut pas être négatif.");
        }
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getVal() { return val; }
    public void setVal(String val) { this.val = val; }
    public double getPrix() { return prix; }
    public void setPrix(double prix) { this.prix = prix; }
}
