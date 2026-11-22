<%@ page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ page import="affichage.Champ" %>
<%@ page import="affichage.Liste" %>
<%@ page import="affichage.PageInsert" %>
<%@ page import="cinema.Film" %>
<%@ page import="cinema.Salle" %>
<%@ page import="cinema.Seance" %>
<%@ page import="user.UserEJB" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.List" %>
<%
    String erreur = null;
    PageInsert pi = null;
    try {
        UserEJB utilisateur = (UserEJB) session.getAttribute("u");
        if (utilisateur == null) throw new IllegalStateException("La session utilisateur a expiré.");

        pi = new PageInsert(new Seance(), request, utilisateur);
        pi.setLien((String) session.getAttribute("lien"));
        Liste films = new Liste("idfilm", new Film(), "titre", "id", "");
        Liste salles = new Liste("idsalle", new Salle(), "libelle", "id", "");
        films.setEstMultiple(false);
        salles.setEstMultiple(false);
        pi.getFormu().changerEnChamp(new Liste[] { films, salles });
        pi.preparerDataFormu();

        Champ[] existants = pi.getFormu().getListeChamp();
        List<Champ> champs = new ArrayList<Champ>();
        for (Champ champ : existants) {
            String nom = champ.getNom().toLowerCase();
            if ("id".equals(nom) || "dateheuredebut".equals(nom)
                    || "dateheurefin".equals(nom) || "etat".equals(nom)) {
                champ.setVisible(false);
            }
            champs.add(champ);
        }

        Champ debut = new Champ("dateHeureDebutSaisie");
        debut.setLibelle("Date et heure de début");
        debut.setType("datetime-local");
        debut.setAutre("required");
        champs.add(debut);
        pi.getFormu().setListeChamp(champs.toArray(new Champ[champs.size()]));
        pi.getFormu().makeHtmlInsertTabIndex();
    } catch (Exception e) {
        erreur = e.getMessage() == null ? "Impossible de préparer le formulaire de séance." : e.getMessage();
        application.log("Erreur lors de la préparation de seance-saisie.jsp", e);
    }
%>
<div class="content-wrapper">
    <section class="content-header"><h1>Nouvelle séance</h1></section>
    <section class="content">
        <% if (erreur != null) { %>
            <div class="alert alert-danger"><%= erreur %></div>
        <% } else { %>
            <div class="box box-primary">
                <div class="box-header with-border"><h3 class="box-title">Programmer une séance</h3></div>
                <form action="<%= pi.getLien() %>?but=cinema/seance/apres-saisie.jsp" method="post" data-parsley-validate>
                    <div class="box-body">
                        <%= pi.getFormu().getHtmlInsert() %>
                        <p>L'heure de fin est calculée automatiquement à partir de la durée du film.</p>
                    </div>
                    <input name="acte" type="hidden" value="insert">
                    <input name="bute" type="hidden" value="cinema/seance/seance-liste.jsp">
                    <input name="classe" type="hidden" value="cinema.Seance">
                    <input name="nomtable" type="hidden" value="CINE_SEANCE">
                </form>
            </div>
        <% } %>
    </section>
</div>
