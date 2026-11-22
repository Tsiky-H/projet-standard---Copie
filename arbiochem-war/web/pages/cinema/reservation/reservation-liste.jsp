<%@ page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ page import="affichage.PageRecherche" %>
<%@ page import="cinema.ReservationLib" %>
<%
    try {
        ReservationLib objet = new ReservationLib();
        String[] criteres = {"idclientlib", "idseancelib", "typeLib"};
        String[] entetes = {"id", "idclientlib", "idseancelib", "filmlib", "sallelib",
                "dateReservation", "dateExpiration", "etat", "typeLib", "nbPlaces",
                "montantBrut", "montantRemise", "montantTotal"};
        PageRecherche pageRecherche = new PageRecherche(
                objet, request, criteres, null, 3, entetes, entetes.length);
        pageRecherche.setTitre("Liste des réservations cinéma");
        pageRecherche.setUtilisateur((user.UserEJB) session.getAttribute("u"));
        pageRecherche.setLien((String) session.getAttribute("lien"));
        pageRecherche.setApres("cinema/reservation/reservation-liste.jsp");
        pageRecherche.creerObjetPage(entetes, null);
        String[] lienTableau = {pageRecherche.getLien() + "?but=cinema/reservation/reservation-fiche.jsp"};
        pageRecherche.getTableau().setLien(lienTableau);
        pageRecherche.getTableau().setColonneLien(new String[]{"id"});
        pageRecherche.getTableau().setLibelleAffiche(new String[]{
                "Réservation", "Client", "Séance", "Film", "Salle", "Date de réservation",
                "Expiration", "État", "Type", "Places", "Montant brut", "Remise", "Total"});
%>
<div class="content-wrapper">
    <section class="content-header"><h1><%= pageRecherche.getTitre() %></h1></section>
    <section class="content">
        <p><a class="btn btn-primary" href="<%= pageRecherche.getLien() %>?but=cinema/reservation/reservation-saisie.jsp">Nouvelle réservation</a></p>
        <form action="<%= pageRecherche.getLien() %>?but=<%= pageRecherche.getApres() %>" method="post">
            <%= pageRecherche.getFormu().getHtmlEnsemble() %>
        </form>
        <%= pageRecherche.getTableauRecap().getHtml() %><br>
        <%= pageRecherche.getTableau().getHtml() %>
        <%= pageRecherche.getBasPage() %>
    </section>
</div>
<%
    } catch (Exception e) {
        application.log("Erreur lors de l'affichage de la liste des réservations cinéma.", e);
%>
<div class="alert alert-danger">Impossible de charger la liste des réservations.</div>
<%
    }
%>
