<%@ page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ page import="cinema.Seance" %>
<%@ page import="user.UserEJB" %>
<%@ page import="java.sql.Timestamp" %>
<%@ page import="java.time.LocalDateTime" %>
<%
    try {
        String acte = request.getParameter("acte");
        if (!"insert".equalsIgnoreCase(acte)) throw new IllegalArgumentException("Action de création invalide.");
        String bute = request.getParameter("bute");
        if (bute == null || bute.trim().isEmpty()) bute = "cinema/seance/seance-liste.jsp";
        if (!"cinema/seance/seance-liste.jsp".equals(bute)) throw new IllegalArgumentException("Page de retour invalide.");
        if (!"cinema.Seance".equals(request.getParameter("classe"))
                || !"CINE_SEANCE".equalsIgnoreCase(request.getParameter("nomtable"))) {
            throw new IllegalArgumentException("Modèle de séance invalide.");
        }
        Seance seance = new Seance();
        seance.setIdFilm(request.getParameter("idfilm"));
        seance.setIdSalle(request.getParameter("idsalle"));
        String debutSaisi = request.getParameter("dateHeureDebutSaisie");
        if (debutSaisi == null || debutSaisi.trim().isEmpty()) {
            throw new IllegalArgumentException("La date et l'heure de début sont obligatoires.");
        }
        seance.setDateHeureDebut(Timestamp.valueOf(LocalDateTime.parse(debutSaisi)));
        ((UserEJB) session.getAttribute("u")).createObject(seance);

        String lien = (String) session.getAttribute("lien");
        if (lien == null || lien.trim().isEmpty()) lien = request.getContextPath() + "/module.jsp";
        else lien = lien.replace("module.jsp", "moduleLeger.jsp");
        response.sendRedirect(lien + "?but=" + bute);
    } catch (Exception e) {
        application.log("Erreur lors de la création d'une séance", e);
        response.setStatus(400);
%>
<div class="alert alert-danger">
    <strong>La séance n'a pas été créée.</strong>
    <%= e.getMessage() == null ? "Une erreur est survenue." : e.getMessage() %>
    <a href="javascript:history.back()">Retour au formulaire</a>
</div>
<%
    }
%>
