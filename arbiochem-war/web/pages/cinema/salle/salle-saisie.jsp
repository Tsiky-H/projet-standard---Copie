<%@ page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ page import="affichage.Champ" %>
<%@ page import="affichage.PageInsert" %>
<%@ page import="bean.CGenUtil" %>
<%@ page import="cinema.CategoriePlace" %>
<%@ page import="cinema.Salle" %>
<%@ page import="user.UserEJB" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.List" %>
<%
    String erreur = null;
    PageInsert pi = null;
    CategoriePlace[] categories = new CategoriePlace[0];
    try {
        UserEJB utilisateur = (UserEJB) session.getAttribute("u");
        if (utilisateur == null) throw new IllegalStateException("La session utilisateur a expiré.");

        pi = new PageInsert(new Salle(), request, utilisateur);
        pi.setLien((String) session.getAttribute("lien"));
        pi.preparerDataFormu();

        Champ[] existants = pi.getFormu().getListeChamp();
        List<Champ> champs = new ArrayList<Champ>();
        for (Champ champ : existants) {
            if ("id".equalsIgnoreCase(champ.getNom())) champ.setVisible(false);
            champs.add(champ);
        }

        categories = (CategoriePlace[]) CGenUtil.rechercher(new CategoriePlace(), null, null, " ORDER BY VAL");
        if (categories == null || categories.length == 0) {
            throw new IllegalStateException("Créez d'abord au moins une catégorie de places.");
        }
        for (CategoriePlace categorie : categories) {
            Champ nombrePlaces = new Champ("nbPlaces_" + categorie.getId());
            nombrePlaces.setLibelle("Rangée " + categorie.getVal() + " — nombre de places");
            nombrePlaces.setType("number");
            nombrePlaces.setDefaut("0");
            nombrePlaces.setAutre("required min=\"0\" step=\"1\"");
            champs.add(nombrePlaces);
        }
        String butApresPost = "cinema/film/film-liste.jsp";
        pi.getFormu().setListeChamp(champs.toArray(new Champ[champs.size()]));
        pi.getFormu().makeHtmlInsertTabIndex();
    } catch (Exception e) {
        erreur = e.getMessage() == null ? "Impossible de préparer le formulaire de salle." : e.getMessage();
        application.log("Erreur lors de la préparation de salle-saisie.jsp", e);
    }
%>
<div class="content-wrapper">
    <section class="content-header"><h1>Nouvelle salle</h1></section>
    <section class="content">
        <% if (erreur != null) { %>
            <div class="alert alert-danger"><%= erreur %></div>
        <% } else { %>
            <div class="box box-primary">
                <div class="box-header with-border"><h3 class="box-title">Informations et places</h3></div>
                <form action="<%= pi.getLien() %>?but=cinema/salle/apres-saisie.jsp" method="post" data-parsley-validate>
                    <div class="box-body">
                        <%= pi.getFormu().getHtmlInsert() %>
                        <p>Chaque catégorie correspond à une rangée. La capacité est calculée à partir des places créées.</p>
                    </div>
                    <input name="acte" type="hidden" value="insert">
                    <input name="bute" type="hidden" value="cinema/salle/salle-liste.jsp">
                    <input name="classe" type="hidden" value="cinema.Salle">
                    <input name="nomtable" type="hidden" value="CINE_SALLE">
                </form>
            </div>
        <% } %>
    </section>
</div>
