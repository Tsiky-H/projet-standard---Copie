<%@ page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ page import="cinema.ReservationService" %>
<%@ page import="user.UserEJB" %>
<%!
    private static String js(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("'", "\\'").replace("\r", " ").replace("\n", " ");
    }
%>
<%
    String erreur = null;
    String idReservation = request.getParameter("idreservation");
    String idVente = null;
    try {
        UserEJB utilisateur = (UserEJB) session.getValue("u");
        String idMode = request.getParameter("idModePaiement");
        if (idMode == null) idMode = request.getParameter("idmodepaiement");
        if (utilisateur == null) throw new IllegalStateException("La session utilisateur a expiré. Reconnectez-vous.");
        if (idReservation == null || idReservation.trim().isEmpty()
                || idMode == null || idMode.trim().isEmpty()) {
            throw new IllegalArgumentException("La réservation et le mode de paiement sont obligatoires.");
        }
        ReservationService.Resultat resultat = ReservationService.payer(
                utilisateur, idReservation.trim(), idMode.trim());
        idVente = resultat.getIdVente();
    } catch (Exception e) {
        erreur = e.getMessage() == null ? "La validation et le paiement ont échoué." : e.getMessage();
        application.log("Erreur lors de la validation d'une réservation cinéma.", e);
    }
%>
<script>
    <% if (erreur == null) { %>
        alert('Réservation validée. Facture / vente <%= js(idVente) %> créée et paiement encaissé.');
        window.location.replace("module.jsp?but=cinema/reservation/reservation-fiche.jsp&id=<%= js(idReservation) %>");
    <% } else { %>
        alert('<%= js(erreur) %>');
        window.location.replace("module.jsp?but=cinema/reservation/reservation-fiche.jsp&id=<%= js(idReservation) %>");
    <% } %>
</script>
