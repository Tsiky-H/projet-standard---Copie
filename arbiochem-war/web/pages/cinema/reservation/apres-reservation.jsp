<%@ page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ page import="cinema.ReservationService" %>
<%@ page import="user.UserEJB" %>
<%@ page import="java.sql.Timestamp" %>
<%@ page import="java.util.Arrays" %>

<%!
    private static String param(javax.servlet.http.HttpServletRequest request, String a, String b) {
        String value = request.getParameter(a);
        return value == null ? request.getParameter(b) : value;
    }
    private static String js(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("'", "\\'").replace("\r", " ").replace("\n", " ");
    }
%>
<%
    String erreur = null;
    String idReservation = null;
    try {
        UserEJB utilisateur = (UserEJB) session.getAttribute("u");
        String idClient = param(request, "idClient", "idclient");
        String idSeance = param(request, "idSeance", "idseance");
        String expirationSaisie = param(request, "dateExpiration", "dateexpiration");
        String[] idsPlaces = request.getParameterValues("places");
        if (idsPlaces == null) idsPlaces = request.getParameterValues("idPlace");

        if (utilisateur == null) throw new IllegalStateException("La session utilisateur a expiré. Reconnectez-vous.");
        if (idClient == null || idClient.trim().isEmpty()) throw new IllegalArgumentException("Le client est obligatoire.");
        if (idSeance == null || idSeance.trim().isEmpty()) throw new IllegalArgumentException("La séance est obligatoire.");
        if (idsPlaces == null || idsPlaces.length == 0) throw new IllegalArgumentException("Sélectionnez au moins une place.");

        Timestamp expiration = null;
        if (expirationSaisie != null && !expirationSaisie.trim().isEmpty()) {
            int minutes;
            try { minutes = Integer.parseInt(expirationSaisie.trim()); }
            catch (NumberFormatException e) { throw new IllegalArgumentException("L'expiration doit être un nombre entier de minutes."); }
            if (minutes <= 0) throw new IllegalArgumentException("Le délai d'expiration doit être supérieur à zéro.");
            expiration = new Timestamp(System.currentTimeMillis() + minutes * 60L * 1000L);
        }

        ReservationService.Resultat resultat = ReservationService.creer(
                utilisateur, idClient.trim(), idSeance.trim(), expiration,
                Arrays.asList(idsPlaces), false, null);
        idReservation = resultat.getIdReservation();
    } catch (Exception e) {
        erreur = e.getMessage() == null ? "La réservation n'a pas pu être créée." : e.getMessage();
        application.log("Erreur lors de la création de la réservation cinéma.", e);
    }
%>
<script>
    <% if (erreur == null) { %>
        window.location.replace("module.jsp?but=cinema/reservation/reservation-liste.jsp");
    <% } else { %>
        alert('<%= js(erreur) %>');
        history.back();
    <% } %>
</script>
