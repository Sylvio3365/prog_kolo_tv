<%@ page import="affichage.*" %>
<%@page import="java.util.Map"%>
<%@page import="java.util.HashMap"%>
<%@ page import="reservation.ReservationLib" %>
<%@ page import="reservation.ReservationDetailsLib" %>
<%@ page import="utils.ConstanteAsync" %>
<%@ page import="utils.CalendarUtil" %>
<%@ page import="utils.UrlUtils" %>
<%@ page import="java.sql.Date" %>
<%@ page import="java.time.LocalDate, java.time.format.DateTimeFormatter" %>
<%@ page import="utilitaire.Utilitaire" %>
<%@ page import="reservation.EtatReservationDetails" %>
<%@ page import="java.time.LocalTime" %>
<%@ page import="java.util.List" %>
<%@ page import="reservation.ReservationDetailsAvecDiffusion" %>
<%@ page import="java.util.Vector" %>
<%@ page import="produits.Acte" %>
<%@ page import="support.Support" %>
<%@ page import="bean.CGenUtil" %>
<%@ page import="produits.CategorieIngredient" %>
<%@ page import="java.time.DayOfWeek" %>
<%@ page import="java.time.format.TextStyle" %>
<%@ page import="java.util.Locale" %>

<style>
.form-input {
  margin-bottom: 0px;
}
table td {
  max-width: 30%;
}
.planning-content p {
  font-weight: bold;
  margin: 0px;
  font-size: 10px;
}
.calendar-grid {
  width: 100%;
  background-color: white;
  overflow: hidden;
  border-radius: 2px;
}
.calendar-cell {
  border-style: solid;
  border-width: 0.5px;
  border-color: #c8c8c8;
  padding: 20px 10px;
  text-align: center;
}
.event {
  display: flex;
  justify-content: space-between;
  gap: 2px;
  align-items: flex-start;
  font-size: 16px;
  line-height: 1.2;
  text-overflow: ellipsis;
}
.day-btn {
  cursor: pointer;
  color: white;
  background-color: #2756a2db;
  padding: 10px 2px;
  text-align: center;
  font-size: 18px;
  border-style: solid;
  border-width: 0.5px;
  border-color: #c8c8c8;
  font-weight: bold;
  transition: 0.3s ease;
}
.day-btn:hover {
  color: rgba(255, 255, 255, 0.87);
  background-color: #2756a2db;;
}
.day-name {
  font-size: 12px;
  font-weight: normal;
  display: block;
  margin-bottom: 5px;
  text-transform: uppercase;
}
.event-title {
  width: 80%;
  background: #f4f4f4;
  border-left: 3px solid #0e66ff;
  padding: 2px 6px;
  margin: 2px 0;
  border-radius: 3px;
  overflow: hidden;
  cursor: pointer;
  transition: 0.3s ease;
}
.event-title:hover {
  box-shadow: 0 0 5px #616161;
}
.event-title p {
  margin: 0;
  padding: 0;
}
.event-hours {
  width: fit-content;
  padding: 2px 2px;
  color: #333;
}
.calendar-cell-title {
  padding: 10px 2px;
  text-align: center;
  font-size: 18px;
  background-color: rgba(231, 231, 231, 0.334);
  border-style: solid;
  border-width: 0.5px;
  border-color: #c8c8c8;
  font-weight: bold;
}
.calendar-footer {
  border-style: solid;
  border-width: 0.5px;
  border-color: #c8c8c8;
  padding: 5px;
}
.week-nav {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 20px;
  margin: 20px 0;
}
.week-range {
  font-size: 18px;
  font-weight: bold;
}
.montant-total {
  color: #28a745;
  font-weight: bold;
  font-size: 14px;
}
.duree-total {
  color: #007bff;
  font-size: 12px;
}
</style>

<%
  try {
    String lien = (String) session.getValue("lien");
    user.UserEJB u = (user.UserEJB) session.getValue("u");

    /* Recuperer la date par defaut */
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    String dateEncours = request.getParameter("d");
    if (dateEncours != null) {
      dateEncours = CalendarUtil.castDateToFormat(dateEncours, DateTimeFormatter.ofPattern("yyyy-MM-dd"), formatter);
    }
    if (dateEncours == null || dateEncours.trim().isEmpty()) {
      LocalDate aujourdHui = LocalDate.now();
      dateEncours = aujourdHui.format(formatter);
    }

    String debutEtFinDeSemaine[] = CalendarUtil.getDebutEtFinDeSemaine(dateEncours);
    String idSupport = request.getParameter("idSupport");
    if (idSupport == null) {
      idSupport = "SUPP002";
    }
    String idTypeService = request.getParameter("idCategorieIngredient");

    DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
    EtatReservationDetails eta = new EtatReservationDetails(idSupport, idTypeService, debutEtFinDeSemaine[0], debutEtFinDeSemaine[1]);
    String[] listeDate = eta.getListeDate();
    List<LocalTime[]> listeHoraire = eta.getHoraire();
    HashMap<String, Double[]> total = eta.getTotal();

    // URL du site
    String urlComplete = request.getRequestURL().toString();
    String queryString = request.getQueryString();

    if (queryString != null) {
      urlComplete += "?" + queryString;
    }
    String lienPrecedent = UrlUtils.modifierParametreDansUrl(urlComplete, "d", CalendarUtil.castDateToFormat(debutEtFinDeSemaine[2], formatter, DateTimeFormatter.ofPattern("yyyy-MM-dd")));
    String lienSuivant = UrlUtils.modifierParametreDansUrl(urlComplete, "d", CalendarUtil.castDateToFormat(debutEtFinDeSemaine[3], formatter, DateTimeFormatter.ofPattern("yyyy-MM-dd")));

    Support[] supports = (Support[]) CGenUtil.rechercher(new Support(), null, null, null, "");
    CategorieIngredient[] categorieIngredients = (CategorieIngredient[]) CGenUtil.rechercher(new CategorieIngredient(), null, null, null, "");

    // Tableau des jours de la semaine en francais
    String[] joursNoms = {"Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi", "Samedi", "Dimanche"};

    String temp = "";
    temp = temp + "<div class=\"modal fade\" id=\"linkModal\" tabindex=\"-1\" role=\"dialog\" aria-labelledby=\"linkModalLabel\" aria-hidden=\"true\">\r\n" +
            "  <div style='width:60%;background:transparent;' class=\"modal-dialog modal-dialog-centered\" role=\"dialog\">\r\n" +
            "    <div style=\"border-radius: 16px;padding:15px;overflow-y:auto;height:80vh\" class=\"modal-content\">\r\n" +
            "      <div class=\"modal-body\">\r\n" +
            "       <div id=\"modalContent\">\r\n>";
    temp += "</div>\r\n" +
            "    </div>\r\n" +
            "   </div>\r\n" +
            "  </div>\r\n" +
            "</div>";
    String bute = "hello/Hello.jsp";
%>

<div class="content-wrapper">

  <section class="content-header">
    <h1><i class="fa fa-calendar"></i>&nbsp;&nbsp;&nbsp; Grille de diffusion - CA</h1>
  </section>

  <!-- Navigation semaine -->
  <div class="week-nav">
    <a href="<%=lienPrecedent%>" id="prev-week" class="btn btn-default">
      <i class="fa fa-chevron-left"></i> Semaine precedente
    </a>
    <span class="week-range" id="week-range">Semaine du <%=debutEtFinDeSemaine[0]%> au <%=debutEtFinDeSemaine[1]%></span>
    <a href="<%=lienSuivant%>" id="next-week" class="btn btn-default">
      Semaine suivante <i class="fa fa-chevron-right"></i>
    </a>
  </div>

  <!-- Selection du support et filtre semaine -->
  <div style="width: 100%;display: flex;justify-content: center">
    <form class="col-md-8 col-xs-12" action="<%=lien%>" method="Get" style="padding: 10px;margin: 5px;border-radius: 5px;display: flex;align-items: end;flex-wrap: wrap;">
      <div class='form-input col-md-3 col-xs-12'>
        <label class="nopadding fontinter labelinput">Support</label>
        <select class="form-control" name="idSupport">
          <option value="">Tous</option>
          <% for (Support s : supports) {
            String isSelected = "";
            if (idSupport != null && idSupport.equals(s.getId())) {
              isSelected = "selected";
            }
          %>
          <option <%=isSelected%> value="<%=s.getId()%>"><%=s.getVal()%></option>
          <% } %>
        </select>
      </div>
      <div class='form-input col-md-3 col-xs-12'>
        <label class="nopadding fontinter labelinput">Type Service</label>
        <select class="form-control" name="idCategorieIngredient">
          <option value="">Tous</option>
          <% for (CategorieIngredient c : categorieIngredients) {
            String isSelected = "";
            if (idTypeService != null && idTypeService.equals(c.getId())) {
              isSelected = "selected";
            }
          %>
          <option <%=isSelected%> value="<%=c.getId()%>"><%=c.getVal()%></option>
          <% } %>
        </select>
      </div>
      <div class="form-input col-md-3 col-xs-12">
        <label class="nopadding fontinter labelinput">Semaine du</label>
        <input class='form-control' type='date' value='<%=CalendarUtil.castDateToFormat(dateEncours, formatter, DateTimeFormatter.ofPattern("yyyy-MM-dd"))%>' name='d'>
      </div>
      <input type='hidden' value='<%=bute%>' name='but'>
      <div class="form-input col-md-3 col-xs-12">
        <button class="btn btn-success" style="width: 100%;height: 32px;text-align: center" type="submit">
          <i class="fa fa-filter"></i> Filtrer
        </button>
      </div>
    </form>
  </div>

  <section class="content">
    <div class="row">
      <div class="col-xs-12 calendar-scroll">
        <table class="calendar-grid">
          <thead>
          <tr>
            <th class="calendar-cell-title">Horaire</th>
            <% for (int i = 0; i < listeDate.length; i++) {
              // Calculer le jour de la semaine
              LocalDate dateJour = LocalDate.parse(listeDate[i], formatter);
              String nomJour = dateJour.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.FRENCH);
              nomJour = nomJour.substring(0, 1).toUpperCase() + nomJour.substring(1);
            %>
            <th class="day-btn" onclick="ouvrirModal(event,'moduleLeger.jsp?but=reservation/inc/reservation-popup.jsp&daty=<%=listeDate[i]%>&idSupport=<%=idSupport%>&idCategorieIngredient=<%=idTypeService%>','modalContent')">
              <span class="day-name"><%=nomJour%></span>
              <%=listeDate[i]%>
            </th>
            <% } %>
            <th class="calendar-cell-title" style="background-color: #e8f5e9;">Total Ligne</th>
          </tr>
          </thead>
          <tbody>
          <%
            // Variables pour le grand total
            double grandTotalMontant = 0;
            double grandTotalDuree = 0;
            int grandTotalReservations = 0;
          %>
          <% for (int i = 0; i < listeHoraire.size(); i++) {
            LocalTime[] intervales = listeHoraire.get(i);
            // Variables pour total par ligne (horaire)
            double totalMontantLigne = 0;
            double totalDureeLigne = 0;
            int totalReservationsLigne = 0;
          %>
          <tr>
            <td class="calendar-cell-title">
              <%=intervales[0]%> - <%=intervales[1]%>
            </td>
            <% for (int j = 0; j < listeDate.length; j++) { %>
            <%
              // Calculer le total montant et duree pour cette cellule
              double totalMontantCell = 0;
              double totalDureeCell = 0;
              int nbReservations = 0;
              
              // Bornes de la plage horaire
              LocalTime plageDebut = intervales[0];
              LocalTime plageFin = intervales[1];
              long plageDebutSec = plageDebut.toSecondOfDay();
              long plageFinSec = plageFin.toSecondOfDay();
              
              // Recuperer TOUTES les reservations du jour
              Vector listeReservationsJour = eta.getReservations().get(listeDate[j]);
              
              if (listeReservationsJour != null && listeReservationsJour.size() > 0) {
                for (Object obj : listeReservationsJour) {
                  ReservationDetailsAvecDiffusion r = (ReservationDetailsAvecDiffusion) obj;
                  if (r != null) {
                    try {
                      // Recuperer heure de debut de diffusion
                      String heureDebutStr = r.getHeureDiffusion();
                      if (heureDebutStr == null || heureDebutStr.isEmpty()) {
                        heureDebutStr = r.getHeure();
                      }
                      
                      // Recuperer duree en secondes
                      String dureeStr = r.getDuree();
                      long dureeSec = 0;
                      if (dureeStr != null && !dureeStr.isEmpty()) {
                        dureeSec = Long.parseLong(dureeStr);
                      }
                      
                      if (heureDebutStr != null && !heureDebutStr.isEmpty() && dureeSec > 0) {
                        // Parser l'heure de debut
                        LocalTime heureDebut;
                        if (heureDebutStr.length() == 5) {
                          heureDebut = LocalTime.parse(heureDebutStr, DateTimeFormatter.ofPattern("HH:mm"));
                        } else {
                          heureDebut = LocalTime.parse(heureDebutStr, DateTimeFormatter.ofPattern("HH:mm:ss"));
                        }
                        
                        long diffDebutSec = heureDebut.toSecondOfDay();
                        long diffFinSec = diffDebutSec + dureeSec;
                        
                        // Verifier s'il y a chevauchement avec la plage
                        long chevauchementDebut = Math.max(diffDebutSec, plageDebutSec);
                        long chevauchementFin = Math.min(diffFinSec, plageFinSec);
                        long chevauchementDuree = Math.max(0, chevauchementFin - chevauchementDebut);
                        
                        // S'il y a chevauchement, calculer la proportion
                        if (chevauchementDuree > 0) {
                          nbReservations++;
                          double proportion = (double) chevauchementDuree / (double) dureeSec;
                          double montantProportionnel = r.getMontantFinal() * proportion;
                          
                          totalMontantCell += montantProportionnel;
                          totalDureeCell += chevauchementDuree;
                        }
                      }
                    } catch (Exception ex) {
                      // Ignorer en cas d'erreur de parsing
                    }
                  }
                }
              }
              // Ajouter au total de la ligne
              totalMontantLigne += totalMontantCell;
              totalDureeLigne += totalDureeCell;
              totalReservationsLigne += nbReservations;
            %>
            <td class="calendar-cell" style="vertical-align: middle;">
              <% if (nbReservations > 0) { %>
              <div style="text-align: center; padding: 5px;">
                <p style="margin: 2px 0;"><strong><%=nbReservations%></strong> reservation(s)</p>
                <p class="montant-total" style="margin: 2px 0;"><%=Utilitaire.formaterAr(totalMontantCell)%> Ar</p>
              </div>
              <% } else { %>
              <div style="text-align: center; color: #999;">
                <i class="fa fa-minus"></i>
              </div>
              <% } %>
            </td>
            <% } %>
            <!-- Total par ligne (horaire) -->
            <td class="calendar-cell" style="vertical-align: middle; background-color: #e8f5e9;">
              <div style="text-align: center; padding: 5px;">
                <p style="margin: 2px 0;"><strong><%=totalReservationsLigne%></strong> res.</p>
                <p class="montant-total" style="margin: 2px 0;"><%=Utilitaire.formaterAr(totalMontantLigne)%> Ar</p>
              </div>
            </td>
            <%
              // Ajouter au grand total
              grandTotalMontant += totalMontantLigne;
              grandTotalDuree += totalDureeLigne;
              grandTotalReservations += totalReservationsLigne;
            %>
          </tr>
          <% } %>
          </tbody>
          <tfoot>
          <tr>
            <th class="calendar-cell-title">TOTAL COLONNE</th>
            <% for (int k = 0; k < listeDate.length; k++) {
              Double[] tab = total.get(listeDate[k]);
              // Calculer le jour de la semaine pour le footer
              LocalDate dateJour = LocalDate.parse(listeDate[k], formatter);
              String nomJour = dateJour.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.FRENCH);
              nomJour = nomJour.substring(0, 1).toUpperCase() + nomJour.substring(1);
            %>
            <th class="calendar-footer">
              <p><strong><%=nomJour%></strong></p>
              <p class="montant-total"><%=Utilitaire.formaterAr(tab[0])%> Ar</p>
            </th>
            <% } %>
            <!-- Grand Total -->
            <th class="calendar-footer" style="background-color: #c8e6c9;">
              <p><strong>GRAND TOTAL</strong></p>
              <p class="montant-total" style="font-size: 16px;"><%=Utilitaire.formaterAr(grandTotalMontant)%> Ar</p>
              <p style="margin: 2px 0; font-size: 12px;"><%=grandTotalReservations%> reservations</p>
            </th>
          </tr>
          </tfoot>
        </table>
      </div>
    </div>
  </section>
</div>

<%= temp %>

<script>
  function ouvrirModal(event, url, targetId) {
    event.preventDefault();
    event.stopPropagation();
    
    var modalElement = $('#linkModal');
    var contentDiv = document.getElementById(targetId);
    
    // Charger le contenu via AJAX
    fetch(url)
      .then(response => response.text())
      .then(html => {
        contentDiv.innerHTML = html;
        modalElement.modal('show');
      })
      .catch(error => {
        console.error('Erreur:', error);
        alert('Erreur lors du chargement');
      });
  }
</script>

<%
  } catch (Exception e) {
    e.printStackTrace();
%>
<div class="content-wrapper">
  <div class="alert alert-danger">
    <h4><i class="fa fa-exclamation-triangle"></i> Erreur</h4>
    <p>Une erreur est survenue lors du chargement de la page.</p>
    <pre><%= e.getMessage() %></pre>
  </div>
</div>
<%
  }
%>