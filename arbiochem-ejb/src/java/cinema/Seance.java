package cinema;

import bean.ClassEtat;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/** Modèle de données pour CINE_SEANCE. */
public class Seance extends ClassEtat {
    private String id;
    private String idFilm;
    private String idSalle;
    private java.sql.Timestamp dateHeureDebut;
    private java.sql.Timestamp dateHeureFin;

    public Seance() {
        setNomTable("CINE_SEANCE");
    }

    @Override
    public String getTuppleID() { return id; }

    @Override
    public String getAttributIDName() { return "id"; }

    @Override
    public void construirePK(Connection c) throws Exception {
        preparePk("CSEA", "getSeqCineSeance");
        setId(makePK(c));
    }

    public void controler(Connection c) throws Exception {
        if (idFilm == null || idFilm.trim().isEmpty()) {
            throw new Exception("Le film de la séance est obligatoire.");
        }
        if (idSalle == null || idSalle.trim().isEmpty()) {
            throw new Exception("La salle de la séance est obligatoire.");
        }
        if (dateHeureDebut == null) {
            throw new Exception("La date et l'heure de début sont obligatoires.");
        }
        int duree;
        try (PreparedStatement ps = c.prepareStatement("SELECT DUREE FROM CINE_FILM WHERE ID = ?")) {
            ps.setString(1, idFilm);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new Exception("Le film sélectionné est introuvable.");
                duree = rs.getInt("DUREE");
            }
        }
        if (duree <= 0) throw new Exception("La durée du film doit être positive.");
        dateHeureFin = java.sql.Timestamp.valueOf(dateHeureDebut.toLocalDateTime().plusMinutes(duree));
        if (!dateHeureFin.after(dateHeureDebut)) {
            throw new Exception("La fin de la séance doit être après son début.");
        }
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getIdFilm() { return idFilm; }
    public void setIdFilm(String idFilm) { this.idFilm = idFilm; }
    public String getIdSalle() { return idSalle; }
    public void setIdSalle(String idSalle) { this.idSalle = idSalle; }
    public java.sql.Timestamp getDateHeureDebut() { return dateHeureDebut; }
    public void setDateHeureDebut(java.sql.Timestamp dateHeureDebut) { this.dateHeureDebut = dateHeureDebut; }
    public java.sql.Timestamp getDateHeureFin() { return dateHeureFin; }
    public void setDateHeureFin(java.sql.Timestamp dateHeureFin) { this.dateHeureFin = dateHeureFin; }
}
