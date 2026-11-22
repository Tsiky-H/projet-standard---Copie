<%@ page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ page import="cinema.PlaceSeanceDispo" %>
<%@ page import="cinema.ReservationService" %>
<%@ page import="cinema.SeanceLib" %>
<%@ page import="bean.CGenUtil" %>
<%@ page import="user.UserEJB" %>
<%@ page import="utilitaire.UtilDB" %>
<%@ page import="java.sql.Connection" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.LinkedHashMap" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Locale" %>
<%@ page import="java.util.Map" %>

<%!
    private static String h(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&#39;");
    }
    private static String param(javax.servlet.http.HttpServletRequest request, String a, String b) {
        String value = request.getParameter(a);
        return value == null ? request.getParameter(b) : value;
    }
%>
<%
    UserEJB utilisateur = (UserEJB) session.getAttribute("u");
    String idClient = param(request, "idClient", "idclient");
    String idSeance = param(request, "idSeance", "idseance");
    String dateExpiration = param(request, "dateExpiration", "dateexpiration");
    String erreur = null;
    PlaceSeanceDispo[] places = new PlaceSeanceDispo[0];
    SeanceLib seance = null;
    Map<String, List<PlaceSeanceDispo>> rangees = new LinkedHashMap<String, List<PlaceSeanceDispo>>();
    Map<String, String[]> categories = new LinkedHashMap<String, String[]>();
    int placesDisponibles = 0;

    try {
        if (utilisateur == null) throw new IllegalStateException("La session utilisateur a expiré. Reconnectez-vous.");
        if (idClient == null || idClient.trim().isEmpty()) throw new IllegalArgumentException("Choisissez un client.");
        if (idSeance == null || idSeance.trim().isEmpty()) throw new IllegalArgumentException("Choisissez une séance.");
        places = ReservationService.listerPlaces(idSeance.trim());
        try (Connection c = new UtilDB().GetConn()) {
            Object[] seances = CGenUtil.rechercher(new SeanceLib(), null, null, c,
                    " AND ID = '" + idSeance.trim().replace("'", "''") + "'");
            if (seances.length > 0) seance = (SeanceLib) seances[0];
        }
        if (seance == null) throw new IllegalArgumentException("La séance sélectionnée est introuvable.");
        if (places.length == 0) throw new IllegalArgumentException("Aucune place n'est définie pour cette séance.");

        for (PlaceSeanceDispo place : places) {
            String idCategorie = place.getIdCategorie();
            String nomCategorie = place.getIdCategorieLib() == null ? "Sans catégorie" : place.getIdCategorieLib();
            if (!categories.containsKey(idCategorie)) {
                categories.put(idCategorie, new String[] {
                    nomCategorie,
                    String.format(Locale.FRANCE, "%,.0f", place.getPrix()),
                    String.valueOf(categories.size() % 6)
                });
            }
            // Le schéma n'a pas de colonne rangée : une catégorie représente une rangée.
            List<PlaceSeanceDispo> rangee = rangees.get(nomCategorie);
            if (rangee == null) {
                rangee = new ArrayList<PlaceSeanceDispo>();
                rangees.put(nomCategorie, rangee);
            }
            rangee.add(place);
            if (place.getDisponible() == 1) placesDisponibles++;
        }
    } catch (Exception e) {
        erreur = e.getMessage() == null ? "Impossible de charger le plan de la salle." : e.getMessage();
        application.log("Erreur lors du chargement du plan des places de réservation.", e);
    }
    SimpleDateFormat formatDate = new SimpleDateFormat("dd/MM/yyyy 'à' HH:mm");
%>

<style>
    .plan-ecran { text-align:center; margin:18px auto 28px; padding:7px; max-width:60%; background:#ddd;
                  border-radius:0 0 40px 40px; font-weight:bold; letter-spacing:4px; }
    .plan-rangee { display:flex; align-items:center; justify-content:center; margin-bottom:9px; flex-wrap:wrap; }
    .plan-rangee-nom { width:110px; font-weight:bold; text-align:right; padding-right:12px; }
    .plan-place { position:relative; margin:0 3px; }
    .plan-place input { position:absolute; opacity:0; width:0; height:0; }
    .plan-place label { display:block; min-width:42px; padding:6px 4px; margin:0; text-align:center;
                        border-radius:6px 6px 3px 3px; border:2px solid transparent; font-size:12px;
                        color:#fff; cursor:pointer; }
    .cat0 { background:#3c8dbc; } .cat1 { background:#d4a017; } .cat2 { background:#00a65a; }
    .cat3 { background:#8e44ad; } .cat4 { background:#e67e22; } .cat5 { background:#16a085; }
    .plan-place input:checked + label { border-color:#222; box-shadow:0 0 0 2px #fff inset; font-weight:bold; }
    .plan-place input:disabled + label { background:#c7c7c7; color:#777; cursor:not-allowed; text-decoration:line-through; }
    .plan-legende span { display:inline-block; margin:0 16px 8px 0; }
    .plan-puce { display:inline-block; width:14px; height:14px; border-radius:3px; vertical-align:middle; margin-right:4px; }
    .plan-resume { margin-top:20px; font-size:16px; }
</style>

<div class="content-wrapper">
    <section class="content-header"><h1>Choisir les places</h1></section>
    <section class="content">
        <% if (erreur != null) { %>
            <div class="alert alert-danger"><%= h(erreur) %></div>
            <a class="btn btn-default" href="module.jsp?but=cinema/reservation/reservation-saisie.jsp">Retour</a>
        <% } else { %>
            <div class="box box-primary">
                <div class="box-header with-border">
                    <h3 class="box-title"><%= h(seance.getLibelle()) %></h3>
                    <p>Début : <%= seance.getDateHeureDebut() == null ? "" : formatDate.format(seance.getDateHeureDebut()) %>
                       &mdash; fin : <%= seance.getDateHeureFin() == null ? "" : formatDate.format(seance.getDateHeureFin()) %></p>
                </div>
                <form action="module.jsp?but=cinema/reservation/apres-reservation.jsp" method="post">
                    <input type="hidden" name="idClient" value="<%= h(idClient) %>">
                    <input type="hidden" name="idSeance" value="<%= h(idSeance) %>">
                    <input type="hidden" name="dateExpiration" value="<%= h(dateExpiration) %>">
                    <input type="hidden" name="acte" value="insert">
                    <input type="hidden" name="bute" value="cinema/reservation/reservation-liste.jsp">
                    <input type="hidden" name="classe" value="cinema.Reservation">
                    <input type="hidden" name="nomtable" value="CINE_RESERVATION">
                    <div class="box-body">
                        <div class="plan-legende">
                            <% for (String[] categorie : categories.values()) { %>
                                <span><i class="plan-puce cat<%= categorie[2] %>"></i><%= h(categorie[0]) %> &mdash; <%= categorie[1] %></span>
                            <% } %>
                            <span><i class="plan-puce" style="background:#c7c7c7"></i>Indisponible</span>
                        </div>
                        <div class="plan-ecran">ÉCRAN</div>
                        <% for (Map.Entry<String, List<PlaceSeanceDispo>> rangee : rangees.entrySet()) { %>
                            <div class="plan-rangee">
                                <span class="plan-rangee-nom"><%= h(rangee.getKey()) %></span>
                                <% for (PlaceSeanceDispo place : rangee.getValue()) {
                                    boolean disponible = place.getDisponible() == 1;
                                    String[] categorie = categories.get(place.getIdCategorie());
                                    String couleur = categorie == null ? "cat0" : "cat" + categorie[2];
                                    String inputId = "place_" + place.getIdPlace().replaceAll("[^A-Za-z0-9_-]", "_");
                                %>
                                    <span class="plan-place">
                                        <input type="checkbox" id="<%= inputId %>" name="places" value="<%= h(place.getIdPlace()) %>"
                                               data-prix="<%= place.getPrix() %>" <%= disponible ? "" : "disabled" %>>
                                        <label for="<%= inputId %>" class="<%= couleur %>"
                                               title="<%= disponible ? "Disponible" : "Indisponible" %>"><%= h(place.getNumero()) %></label>
                                    </span>
                                <% } %>
                            </div>
                        <% } %>
                        <% if (placesDisponibles == 0) { %>
                            <div class="alert alert-warning">Il ne reste aucune place disponible pour cette séance.</div>
                        <% } %>
                        <div class="plan-resume"><strong id="nbPlaces">0</strong> place(s) sélectionnée(s)
                            &mdash; total : <strong id="totalPlaces">0</strong></div>
                    </div>
                    <div class="box-footer">
                        <a class="btn btn-default" href="module.jsp?but=cinema/reservation/reservation-saisie.jsp">Retour</a>
                        <button type="submit" id="btnReserver" class="btn btn-primary" disabled>Enregistrer la réservation</button>
                    </div>
                </form>
            </div>
            <script>
                (function () {
                    var cases = document.querySelectorAll('.plan-place input[type=checkbox]');
                    var nb = document.getElementById('nbPlaces'), total = document.getElementById('totalPlaces');
                    var bouton = document.getElementById('btnReserver');
                    function actualiser() {
                        var n = 0, somme = 0;
                        for (var i = 0; i < cases.length; i++) if (cases[i].checked) {
                            n++; somme += parseFloat(cases[i].getAttribute('data-prix')) || 0;
                        }
                        nb.textContent = n;
                        total.textContent = somme.toLocaleString('fr-FR');
                        bouton.disabled = n === 0;
                    }
                    for (var i = 0; i < cases.length; i++) cases[i].addEventListener('change', actualiser);
                    actualiser();
                })();
            </script>
        <% } %>
    </section>
</div>
