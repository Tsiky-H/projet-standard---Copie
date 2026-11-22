<%@ page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ page import="cinema.Reservation" %>
<%@ page import="client.Client" %>
<%@ page import="cinema.SeanceLib" %>
<%@ page import="affichage.PageInsert" %>
<%@ page import="affichage.Liste" %>

<% try{
    Reservation a = new Reservation();
    PageInsert pi = new PageInsert(a, request, (user.UserEJB) session.getValue("u"));
    pi.setLien((String) session.getValue("lien"));

        SeanceLib seancesReservables = new SeanceLib();
        seancesReservables.setNomTable("CINE_SEANCE_LIB");
        Liste clientListe = new Liste("idClient", new Client(), "nom", "id");
        Liste seanceListe = new Liste("idSeance", seancesReservables, "libelle", "id"," AND ETAT <> 0 AND DATEHEUREDEBUT > SYSTIMESTAMP");

        pi.getFormu().changerEnChamp(new Liste[] { clientListe, seanceListe });
        pi.getFormu().getChamp("idClient").setLibelle("Client");
        pi.getFormu().getChamp("idSeance").setLibelle("Film, salle et s&eacute;ance");
        pi.getFormu().getChamp("dateExpiration").setLibelle("Expiration (minutes &agrave; partir de maintenant ; vide = d&eacute;but de s&eacute;ance)");
        pi.getFormu().getChamp("dateExpiration").setType("number");
        pi.getFormu().getChamp("dateExpiration").setAutre("min=\"1\" step=\"1\"");
        pi.getFormu().getChamp("idClient").setAutre("required");
        pi.getFormu().getChamp("idSeance").setAutre("required");
        pi.getFormu().getChamp("dateReservation").setVisible(false);
        pi.getFormu().getChamp("etat").setVisible(false);
        pi.preparerDataFormu();
        pi.getFormu().makeHtmlInsertTabIndex();

        String title = "Saisie Reservation";
%>
<div class="content-wrapper">
    <h1 align="center"><%=title%></h1>
    <form action="<%=pi.getLien()%>?but=cinema/reservation/reservation-places.jsp" method="post" data-parsley-validate>
        <%
            out.println(pi.getFormu().getHtmlInsert());
        %>
        <button type="submit" class="btn btn-primary">Choisir les places</button>
    </form>
</div>
<%
    } catch (Exception e) {
        application.log("Erreur lors de la préparation du formulaire de réservation cinéma.", e);
%>
<div class="alert alert-danger">Impossible de préparer le formulaire de réservation.</div>
<%
    }
%>
