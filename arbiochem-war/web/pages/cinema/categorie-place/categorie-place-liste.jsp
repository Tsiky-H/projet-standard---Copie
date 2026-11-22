<%@page import="affichage.PageRecherche"%>
<%@page import="cinema.CategoriePlace"%>

<% try{
    CategoriePlace t = new CategoriePlace();
    t.setNomTable("cine_categorie_place");
    String listeCrt[] = {"val","prix"};
    String listeInt[] = {};
    String libEntete[] = {"val","prix"};
    
    PageRecherche pr = new PageRecherche(t, request, listeCrt, listeInt, 3, libEntete, libEntete.length);
    pr.setTitre("Liste Cat&eacute;gorie");
    pr.getFormu().getChamp("val").setLibelle("Cat&eacute;gorie");
    pr.setUtilisateur((user.UserEJB) session.getValue("u"));
    pr.setLien((String) session.getValue("lien"));
    pr.setApres("cinema/categorie-place/categorie-place-liste.jsp");
    String[] colSomme = null;
    pr.creerObjetPage(libEntete, colSomme);

    //Definition des libelles à afficher
    String libEnteteAffiche[] = {"Cat&eacute;gorie","Prix"};
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



