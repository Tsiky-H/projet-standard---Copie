package cinema;

import bean.ClassMAPTable;
import java.sql.Connection;

/** Modèle de données pour CINE_PLACE. */
public class Place extends ClassMAPTable {
    private String id;
    private String idSalle;
    private String idCategorie;
    private String numero;

    public Place() {
        setNomTable("CINE_PLACE");
    }

    @Override
    public String getTuppleID() { return id; }

    @Override
    public String getAttributIDName() { return "id"; }

    @Override
    public void construirePK(Connection c) throws Exception {
        preparePk("CPLA", "getSeqCinePlace");
        setId(makePK(c));
    }

    public void controler(Connection c) throws Exception {
        if (idSalle == null || idSalle.trim().isEmpty()) {
            throw new Exception("La salle de la place est obligatoire.");
        }
        if (idCategorie == null || idCategorie.trim().isEmpty()) {
            throw new Exception("La catégorie de la place est obligatoire.");
        }
        if (numero == null || numero.trim().isEmpty()) {
            throw new Exception("Le numéro de la place est obligatoire.");
        }
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getIdSalle() { return idSalle; }
    public void setIdSalle(String idSalle) { this.idSalle = idSalle; }
    public String getIdCategorie() { return idCategorie; }
    public void setIdCategorie(String idCategorie) { this.idCategorie = idCategorie; }
    public String getNumero() { return numero; }
    public void setNumero(String numero) { this.numero = numero; }
}
