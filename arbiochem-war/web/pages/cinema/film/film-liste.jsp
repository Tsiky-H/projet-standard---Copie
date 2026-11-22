<%@page import="affichage.PageRecherche"%>
<%@page import="cinema.Film"%>

<% try{
    Film t = new Film();
    t.setNomTable("cine_film");
    String listeCrt[] = {"titre","realisateur","genre","duree","classification"};
    String listeInt[] = {};
    String libEntete[] = {"titre","realisateur","genre","duree","classification"};
    
    PageRecherche pr = new PageRecherche(t, request, listeCrt, listeInt, 3, libEntete, libEntete.length);
    pr.setTitre("Liste Film");
    pr.getFormu().getChamp("duree").setLibelle("Dur&eacute;e");
    pr.setUtilisateur((user.UserEJB) session.getValue("u"));
    pr.setLien((String) session.getValue("lien"));
    pr.setApres("cinema/film/film-liste.jsp");
    String[] colSomme = null;
    pr.creerObjetPage(libEntete, colSomme);

    //Definition des libelles à afficher
    String libEnteteAffiche[] = {"Titre","R&eacute;alisateur","genre","Dur&eacute;e","classification"};
    pr.getTableau().setLibelleAffiche(libEnteteAffiche);
%>

<div class="content-wrapper">
    <section class="content-header">
        <h1><%= pr.getTitre() %></h1>
    </section>
    <section class="content">
        <form action="<%=pr.getLien()%>?but=<%= pr.getApres() %>" method="post">
            <%
                out.println(pr.getFormu().getHtmlEnsemble());
            %>
        </form>
        <%
            out.println(pr.getTableauRecap().getHtml());%>
        <br>
        <%
            out.println(pr.getTableau().getHtml());
            out.println(pr.getBasPage());
        %>
    </section>
</div>
<%
    }catch(Exception e){

        e.printStackTrace();
    }
%>



