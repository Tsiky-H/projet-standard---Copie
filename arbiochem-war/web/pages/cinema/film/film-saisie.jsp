<%@ page import="user.*" %>
<%@ page import="bean.*" %>
<%@ page import="utilitaire.*" %>
<%@ page import="affichage.*" %>
<%@ page import="cinema.Film" %>
<%
    try{
        Film a = new Film();
        PageInsert pi = new PageInsert(a, request, (user.UserEJB) session.getValue("u"));
        pi.setLien((String) session.getValue("lien"));

        pi.getFormu().getChamp("duree").setLibelle("Dur&eacute;e (minutes)");
        pi.getFormu().getChamp("classification").setLibelle("Classification d'âge (facultative)");
        pi.getFormu().getChamp("titre").setAutre("required maxlength=\"200\"");
        pi.getFormu().getChamp("duree").setType("number");
        pi.getFormu().getChamp("duree").setAutre("required min=\"1\" step=\"1\"");
        pi.getFormu().getChamp("classification").setType("number");
        pi.getFormu().getChamp("classification").setAutre("min=\"0\" step=\"1\"");
        
        //Variables de navigation
        String classe = "cinema.Film";
        String butApresPost = "cinema/film/film-liste.jsp";
        String nomTable = "cine_film";
        String title = "Saisie Film";
        //Generer les affichages
        pi.preparerDataFormu();
        pi.getFormu().makeHtmlInsertTabIndex();
%>
<div class="content-wrapper">
    <h1 align="center"><%=title%></h1>
    <form action="<%=pi.getLien()%>?but=apresTarif.jsp" method="post"  data-parsley-validate>
        <%
            out.println(pi.getFormu().getHtmlInsert());
        %>
        <input name="acte" type="hidden" id="nature" value="insert">
        <input name="bute" type="hidden" id="bute" value="<%= butApresPost %>">
        <input name="classe" type="hidden" id="classe" value="<%= classe %>">
        <input name="nomtable" type="hidden" id="nomtable" value="<%= nomTable %>">
    </form>
</div>
<%
    } catch (Exception e) {
        e.printStackTrace();
    } %>


