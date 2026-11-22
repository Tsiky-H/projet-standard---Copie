<%@ page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ page import="cinema.Salle" %>
<%@ page import="cinema.SalleService" %>
<%@ page import="user.UserEJB" %>
<%@ page import="java.util.Enumeration" %>
<%@ page import="java.util.LinkedHashMap" %>
<%@ page import="java.util.Map" %>
<%
    try {
        String acte = request.getParameter("acte");
        if (!"insert".equalsIgnoreCase(acte)) throw new IllegalArgumentException("Action de création invalide.");
        String bute = request.getParameter("bute");
        if (bute == null || bute.trim().isEmpty()) bute = "cinema/salle/salle-liste.jsp";
        if (!"cinema/salle/salle-liste.jsp".equals(bute)) throw new IllegalArgumentException("Page de retour invalide.");
        if (!"cinema.Salle".equals(request.getParameter("classe"))
                || !"CINE_SALLE".equalsIgnoreCase(request.getParameter("nomtable"))) {
            throw new IllegalArgumentException("Modèle de salle invalide.");
        }
        Salle salle = new Salle();
        salle.setLibelle(request.getParameter("libelle"));
        Map<String, Integer> placesParCategorie = new LinkedHashMap<String, Integer>();
        Enumeration<String> noms = request.getParameterNames();
        while (noms.hasMoreElements()) {
            String nom = noms.nextElement();
            if (nom.startsWith("nbPlaces_")) {
                String valeur = request.getParameter(nom);
                int nombre = valeur == null || valeur.trim().isEmpty() ? 0 : Integer.parseInt(valeur);
                placesParCategorie.put(nom.substring("nbPlaces_".length()), nombre);
            }
        }

        SalleService.creerAvecPlaces(salle, placesParCategorie,
                (UserEJB) session.getAttribute("u"));

        String lien = (String) session.getAttribute("lien");
        if (lien == null || lien.trim().isEmpty()) lien = request.getContextPath() + "/module.jsp";
        else lien = lien.replace("module.jsp", "moduleLeger.jsp");
        response.sendRedirect(lien + "?but=" + bute);
    } catch (Exception e) {
        application.log("Erreur lors de la création d'une salle et de ses places", e);
        response.setStatus(400);
%>
<div class="alert alert-danger">
    <strong>La salle n'a pas été créée.</strong>
    <%= e.getMessage() == null ? "Une erreur est survenue." : e.getMessage() %>
    <a href="javascript:history.back()">Retour au formulaire</a>
</div>
<%
    }
%>
