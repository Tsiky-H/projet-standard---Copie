<%@page import="affichage.PageRecherche"%>
<%@page import="cinema.SalleLib"%>
<%
    try {
        SalleLib objet = new SalleLib();
        String[] criteres = {"libelle"};
        String[] entetes = {"id", "libelle", "capacite"};
        PageRecherche pageRecherche = new PageRecherche(objet, request, criteres, null, 3, entetes, entetes.length);
        pageRecherche.setTitre("Liste des salles");
        pageRecherche.setUtilisateur((user.UserEJB) session.getValue("u"));
        pageRecherche.setLien((String) session.getValue("lien"));
        pageRecherche.setApres("cinema/salle/salle-liste.jsp");
        pageRecherche.creerObjetPage(entetes, null);
        pageRecherche.getFormu().getChamp("libelle").setLibelle("Nom de la salle");
        pageRecherche.getTableau().setLibelleAffiche(new String[]{"Identifiant", "Salle", "Capacit&eacute;"});
%>

<div class="content-wrapper">
    <section class="content-header"><h1><%= pageRecherche.getTitre() %></h1></section>
    <section class="content">
        <p><a class="btn btn-primary" href="module.jsp?but=cinema/salle/salle-saisie.jsp">Nouvelle salle</a></p>
        <form action="<%=pageRecherche.getLien()%>?but=<%=pageRecherche.getApres()%>" method="post">
            <%= pageRecherche.getFormu().getHtmlEnsemble() %>
        </form>
        <%= pageRecherche.getTableauRecap().getHtml() %><br>
        <%= pageRecherche.getTableau().getHtml() %>
        <%= pageRecherche.getBasPage() %>
    </section>
</div>
<% } catch (Exception e) { application.log("Erreur lors du chargement de salle-liste.jsp", e); %>
<div class="alert alert-danger">Impossible de charger la liste des salles.</div>
<% } %>
