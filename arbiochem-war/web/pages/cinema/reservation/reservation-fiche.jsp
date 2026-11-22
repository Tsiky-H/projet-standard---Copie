<%@page import="affichage.PageConsulte"%>
<%@page import="affichage.PageRecherche"%>
<%@page import="cinema.ReservationLib"%>
<%@page import="cinema.ReservationFilleLib"%>
<%@page import="user.UserEJB"%>
<%
    try {
        UserEJB utilisateur = (UserEJB) session.getValue("u");
        ReservationLib reservation = new ReservationLib();
        PageConsulte pc = new PageConsulte(reservation, request, utilisateur);
        reservation = (ReservationLib) pc.getBase();
        if (reservation == null) throw new Exception("La réservation demandée est introuvable.");

        String id = reservation.getId();
        String lien = (String) session.getValue("lien");
        pc.setTitre("Fiche réservation cinéma");
        pc.getChampByName("idClient").setVisible(false);
        pc.getChampByName("idSeance").setVisible(false);
        pc.getChampByName("etat").setVisible(false);
        pc.getChampByName("idClientLib").setLibelle("Client");
        pc.getChampByName("idSeanceLib").setLibelle("Séance");
        pc.getChampByName("filmLib").setLibelle("Film");
        pc.getChampByName("salleLib").setLibelle("Salle");
        pc.getChampByName("dateReservation").setLibelle("Date de réservation");
        pc.getChampByName("dateExpiration").setLibelle("Expiration");
        pc.getChampByName("seanceDebut").setLibelle("Début de séance");
        pc.getChampByName("seanceFin").setLibelle("Fin de séance");
        pc.getChampByName("nbPlaces").setLibelle("Nombre de places");
        pc.getChampByName("montantBrut").setLibelle("Montant brut");
        pc.getChampByName("montantRemise").setLibelle("Remise");
        pc.getChampByName("montantTotal").setLibelle("Total à payer");
        pc.getChampByName("typeLib").setLibelle("Type");

        ReservationFilleLib lignes = new ReservationFilleLib();
        String[] criteres = {};
        String[] colonnes = {"idPlaceLib", "idCategorieLib", "prixUnitaire", "montantRemise", "montantNet", "actif"};
        PageRecherche pageLignes = new PageRecherche(lignes, request, criteres, null, 0, colonnes, colonnes.length);
        pageLignes.setUtilisateur(utilisateur);
        pageLignes.setLien(lien);
        pageLignes.setAWhere(" AND idReservation='" + id.replace("'", "''") + "' ORDER BY idPlaceLib");
        pageLignes.creerObjetPage(colonnes, null);
        pageLignes.getTableau().setLibelleAffiche(new String[]{"Place", "Catégorie", "Prix", "Remise", "Net", "Réservée"});
%>
<div class="content-wrapper">
    <h1 class="box-title">
        <a href="<%= lien %>?but=cinema/reservation/reservation-liste.jsp"><i class="fa fa-angle-left"></i></a>
        <%= pc.getTitre() %>
    </h1>
    <div class="box box-primary">
        <div class="box-body">
            <%= pc.getHtml() %>
            <h3>Places réservées</h3>
            <%= pageLignes.getTableau().getHtml() %>
        </div>
        <div class="box-footer">
            <% if (reservation.getEtat() == 1) { %>
                <a class="btn btn-success pull-right"
                   href="<%= lien %>?but=cinema/reservation/reservation-paiement.jsp&id=<%= id %>">
                    Valider la réservation
                </a>
            <% } %>
        </div>
    </div>
</div>
<%
    } catch (Exception e) {
        application.log("Erreur lors de l'affichage de la fiche réservation cinéma.", e);
%>
<div class="alert alert-danger"><%= e.getMessage() %></div>
<%
    }
%>
