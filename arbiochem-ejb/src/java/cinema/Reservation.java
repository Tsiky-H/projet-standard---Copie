package cinema;

import bean.ClassMere;
import java.sql.Connection;

/** Modèle de données pour CINE_RESERVATION. */
public class Reservation extends ClassMere {
    private String id;
    private String idClient;
    private String idSeance;
    private java.sql.Timestamp dateReservation;
    private java.sql.Timestamp dateExpiration;

    public Reservation() throws Exception {
        setNomTable("CINE_RESERVATION");
        setLiaisonFille("idReservation");
        setNomClasseFille("cinema.ReservationFille");
    }

    @Override
    public String getLiaisonFille() { return "idReservation"; }

    @Override
    public String getNomClasseFille() { return "cinema.ReservationFille"; }

    @Override
    public String getTuppleID() { return id; }

    @Override
    public String getAttributIDName() { return "id"; }

    @Override
    public void construirePK(Connection c) throws Exception {
        preparePk("CRES", "getSeqCineReservation");
        setId(makePK(c));
    }

    public void controler(Connection c) throws Exception {
        if (idClient == null || idClient.trim().isEmpty()) {
            throw new Exception("Le client est obligatoire.");
        }
        if (idSeance == null || idSeance.trim().isEmpty()) {
            throw new Exception("La séance est obligatoire.");
        }
        if (dateReservation == null) {
            throw new Exception("La date de réservation ou d'achat est obligatoire.");
        }
        if (dateExpiration != null && !dateExpiration.after(dateReservation)) {
            throw new Exception("La date d'expiration doit être après la date de réservation.");
        }
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getIdClient() { return idClient; }
    public void setIdClient(String idClient) { this.idClient = idClient; }
    public String getIdSeance() { return idSeance; }
    public void setIdSeance(String idSeance) { this.idSeance = idSeance; }
    public java.sql.Timestamp getDateReservation() { return dateReservation; }
    public void setDateReservation(java.sql.Timestamp dateReservation) { this.dateReservation = dateReservation; }
    public java.sql.Timestamp getDateExpiration() { return dateExpiration; }
    public void setDateExpiration(java.sql.Timestamp dateExpiration) { this.dateExpiration = dateExpiration; }
}
