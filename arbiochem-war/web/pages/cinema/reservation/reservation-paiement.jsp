<%@page import="affichage.Champ"%>
<%@page import="affichage.Liste"%>
<%@page import="affichage.PageConsulte"%>
<%@page import="affichage.PageInsert"%>
<%@page import="bean.TypeObjet"%>
<%@page import="caisse.MvtCaisse"%>
<%@page import="cinema.ReservationLib"%>
<%@page import="user.UserEJB"%>
<%@page import="utilitaire.Utilitaire"%>
<%
    String erreur = null;
    PageInsert pageInsert = null;
    ReservationLib reservation = null;
    PageConsulte pageConsulte = null;
    try {
        UserEJB utilisateur = (UserEJB) session.getValue("u");
        String idReservation = request.getParameter("id");
        if (idReservation == null) idReservation = request.getParameter("idreservation");
        if (idReservation == null || idReservation.trim().isEmpty()) {
            throw new IllegalArgumentException("L'identifiant de réservation est obligatoire.");
        }

        ReservationLib critere = new ReservationLib();
        pageConsulte = new PageConsulte(critere, request, utilisateur);
        reservation = (ReservationLib) pageConsulte.getBase();
        if (reservation == null) throw new Exception("La réservation est introuvable.");
        if (reservation.getEtat() != 1) throw new IllegalArgumentException("Cette réservation n'est plus en attente.");
        if (reservation.getNbPlaces() <= 0) throw new IllegalArgumentException("Cette réservation ne contient aucune place.");
        pageConsulte.setTitre("Résumé de la réservation");
        pageConsulte.getChampByName("idClient").setVisible(false);
        pageConsulte.getChampByName("idSeance").setVisible(false);
        pageConsulte.getChampByName("etat").setVisible(false);
        pageConsulte.getChampByName("idClientLib").setLibelle("Client");
        pageConsulte.getChampByName("idSeanceLib").setLibelle("Séance");
        pageConsulte.getChampByName("nbPlaces").setLibelle("Nombre de places");
        pageConsulte.getChampByName("montantTotal").setLibelle("Montant total à payer");

        MvtCaisse mouvement = new MvtCaisse();
        pageInsert = new PageInsert(mouvement, request, utilisateur);
        pageInsert.setLien((String) session.getValue("lien"));
        Champ[] champs = {new Liste("idModePaiement", new TypeObjet("MODEPAIEMENT"), "val", "id")};
        pageInsert.getFormu().changerEnChamp(champs);
        Champ[] tous = pageInsert.getFormu().getListeChamp();
        for (Champ champ : tous) {
            String nom = champ.getNom();
            if (!"idModePaiement".equalsIgnoreCase(nom) && !"credit".equalsIgnoreCase(nom)) {
                champ.setVisible(false);
            }
        }
        pageInsert.getFormu().getChamp("idModePaiement").setLibelle("Mode de paiement");
        pageInsert.getFormu().getChamp("idModePaiement").setAutre("required");
        pageInsert.getFormu().getChamp("credit").setLibelle("Montant total à payer");
        pageInsert.getFormu().getChamp("credit").setDefaut(Utilitaire.formaterAr(reservation.getMontantTotal()));
        pageInsert.getFormu().getChamp("credit").setAutre("readonly");
        pageInsert.getFormu().setOrdre(new String[]{"idModePaiement", "credit"});
        pageInsert.preparerDataFormu();
        pageInsert.getFormu().makeHtmlInsertTabIndex();
    } catch (Exception e) {
        erreur = e.getMessage() == null ? "Impossible de préparer la validation." : e.getMessage();
        application.log("Erreur lors de la préparation de la validation d'une réservation cinéma.", e);
    }
%>
<div class="content-wrapper">
    <section class="content-header"><h1>Valider la réservation</h1></section>
    <section class="content">
        <% if (erreur != null) { %>
            <div class="alert alert-danger"><%= erreur %></div>
        <% } else { %>
            <div class="box box-primary">
                <div class="box-body">
                    <%= pageConsulte.getHtml() %>
                    <form action="<%= pageInsert.getLien() %>?but=cinema/reservation/apres-paiement.jsp" method="post" data-parsley-validate>
                        <%= pageInsert.getFormu().getHtmlInsert() %>
                        <input type="hidden" name="idreservation" value="<%= reservation.getId() %>">
                        <input name="acte" type="hidden" value="valider">
                        <input name="bute" type="hidden" value="cinema/reservation/reservation-fiche.jsp">
                        <input name="classe" type="hidden" value="cinema.Reservation">
                        <input name="nomtable" type="hidden" value="CINE_RESERVATION">
                        <button type="submit" class="btn btn-success pull-right">Confirmer et encaisser</button>
                    </form>
                </div>
            </div>
        <% } %>
    </section>
</div>
