package reservation;

import bean.CGenUtil;
import bean.ClassFille;
import bean.ClassMAPTable;
import emission.Emission;
import produits.IngredientsLib;
import utilitaire.UtilDB;
import utilitaire.Utilitaire;
import utils.CalendarUtil;

import javax.servlet.http.HttpServletRequest;
import java.sql.Connection;
import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class ReservationDetailsGroupe extends ClassFille {
    String idmere, idproduit, idmedia, heure, remarque, source;
    String duree;
    double pu;
    Date datedebut;
    Date datefin;
    String dateDiffusion;
    int isEntete;
    int ordre;
    int nbspot;
    String dateInvalide;

    public String getDateInvalide() {
        return dateInvalide;
    }

    public void setDateInvalide(String dateInvalide) {
        this.dateInvalide = dateInvalide;
    }

    public int getNbspot() {
        return nbspot;
    }

    public void setNbspot(int nbspot) {
        this.nbspot = nbspot;
    }

    public int getIsEntete() {
        return isEntete;
    }

    public void setIsEntete(int isEntete) {
        this.isEntete = isEntete;
    }

    public int getOrdre() {
        return ordre;
    }

    public void setOrdre(int ordre) {
        this.ordre = ordre;
    }

    @Override
    public String getTuppleID() {
        return "";
    }

    @Override
    public String getAttributIDName() {
        return "";
    }

    public ReservationDetailsGroupe() {
        super();
        this.setNomTable("RESERVATIONDETAILSGROUPE");
    }

    public String getIdmedia() {
        return idmedia;
    }

    public void setIdmedia(String idmedia) {
        this.idmedia = idmedia;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getDuree() {
        return duree;
    }

    public String getIdmere() {
        return idmere;
    }

    public void setIdmere(String idmere) {
        this.idmere = idmere;
    }

    public String getIdproduit() {
        return idproduit;
    }

    public void setIdproduit(String idproduit) {
        this.idproduit = idproduit;
    }

    public Date getDatedebut() {
        return datedebut;
    }

    public void setDatedebut(Date datedebut) {
        this.datedebut = datedebut;
    }

    public Date getDatefin() {
        return datefin;
    }

    public void setDatefin(Date datefin) {
        this.datefin = datefin;
    }

    public String getHeure() {
        return heure;
    }

    public String getDateDiffusion() {
        return dateDiffusion;
    }

    public void setDateDiffusion(String dateDiffusion) {
        this.dateDiffusion = dateDiffusion;
    }

    public String getRemarque() {
        return remarque;
    }

    public void setRemarque(String remarque) {
        this.remarque = remarque;
        this.setLiaisonMere("idmere");
        setClassMere("reservation.Reservation");
    }

    public ReservationDetails[] genererReservationDetails() throws Exception {
        List<ReservationDetails> reservationDetails = new ArrayList<ReservationDetails>();
        String[] listDate = this.getDateDiffusion().split(";");
        Connection c = null;
        try {
            c = new UtilDB().GetConn();
            ReservationDetails temp = new ReservationDetails();
            temp.setIdmere(this.getIdmere());
            Reservation mere = temp.getMere(c);
            if (mere == null) {
                throw new Exception("La reservation mere avec l'id " + this.getIdmere() + " est introuvable");
            }
            String idSupport = mere.getIdSupport();
            int nombreTotal = listDate.length;
            List<Date> datesValides = new ArrayList<Date>();
            
            // Debug info nouvelle reservation
            long newHeureDebut = convertHeureToSeconds(this.getHeure());
            long newDuree = Long.parseLong(this.getDuree() != null && !this.getDuree().isEmpty() ? this.getDuree() : "0");
            long newHeureFin = newHeureDebut + newDuree;
            System.out.println("=== NOUVELLE RESERVATION ===");
            System.out.println("Media: " + this.getIdmedia());
            System.out.println("Heure debut: " + this.getHeure() + " (" + newHeureDebut + "s)");
            System.out.println("Duree: " + this.getDuree() + "s");
            System.out.println("Heure fin: " + secondsToHeure(newHeureFin) + " (" + newHeureFin + "s)");
            System.out.println("Plage: " + this.getHeure() + " - " + secondsToHeure(newHeureFin));
            System.out.println("============================");
            
            // Trouver la derniere date de la liste pour ajouter apres si besoin
            LocalDate derniereDate = LocalDate.parse(listDate[0], DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            for (String d : listDate) {
                LocalDate ld = LocalDate.parse(d, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                if (ld.isAfter(derniereDate)) {
                    derniereDate = ld;
                }
            }
            for (String d : listDate) {
                LocalDate currentDate = LocalDate.parse(d, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                Date actuel = Date.valueOf(currentDate);
                System.out.println("\n==> Verification: " + currentDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                
                ReservationDetails[] existantes = getAllExistingReservations(c, actuel, idSupport);
                System.out.println("  Nombre de reservations existantes: " + existantes.length);
                
                if (existantes.length > 0) {
                    String currentIdMedia = this.getIdmedia();
                    boolean conflit = hasConflict(existantes, currentIdMedia, newHeureDebut, newHeureFin);
                    
                    if (conflit) {
                        System.out.println("  => Date " + currentDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                                + " deja prise par meme media avec chevauchement, sera remplacee");
                    } else {
                        System.out.println("  => Date valide (pas de conflit media+horaire)");
                        datesValides.add(actuel);
                    }
                } else {
                    System.out.println("  => Aucune reservation existante, date valide");
                    datesValides.add(actuel);
                }
            }
            
            LocalDate nextDate = derniereDate.plusDays(1);
            while (datesValides.size() < nombreTotal) {
                Date actuel = Date.valueOf(nextDate);
                System.out.println("\n==> Recherche date de remplacement: "
                        + nextDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));

                ReservationDetails[] existantes = getAllExistingReservations(c, actuel, idSupport);
                System.out.println("  Nombre de reservations existantes: " + existantes.length);
                
                if (existantes.length > 0) {
                    String currentIdMedia = this.getIdmedia();
                    boolean conflit = hasConflict(existantes, currentIdMedia, newHeureDebut, newHeureFin);
                    
                    if (conflit) {
                        System.out.println("  => Date " + nextDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                                + " aussi prise, continue...");
                    } else {
                        datesValides.add(actuel);
                        System.out.println("  => Date de remplacement trouvee: "
                                + nextDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                    }
                } else {
                    datesValides.add(actuel);
                    System.out.println("  => Date de remplacement trouvee: "
                            + nextDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                }
                nextDate = nextDate.plusDays(1);
            }

            System.out.println("\n=== CREATION DES RESERVATIONS ===");
            for (Date actuel : datesValides) {
                ReservationDetails res = new ReservationDetails();
                res.setIdproduit(this.getIdproduit());
                res.setIdMedia(this.getIdmedia());
                res.setHeure(this.getHeure());
                res.setDaty(actuel);
                res.setRemarque(this.getRemarque());
                res.setSource(this.getSource());
                res.setDuree(this.getDuree());
                res.setPu(this.getPu());
                res.setQte(1);
                res.setIsEntete(this.getIsEntete());
                res.setOrdre(this.getOrdre());
                reservationDetails.add(res);
                System.out.println("Creation reservation: Date=" + actuel.toLocalDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) 
                        + " | Heure=" + this.getHeure() + " | Duree=" + this.getDuree() + "s | Fin=" + secondsToHeure(newHeureFin)
                        + " | Media=" + this.getIdmedia());
            }
            System.out.println("=================================");
            System.out.println("Total reservations creees: " + reservationDetails.size());

            return reservationDetails.toArray(new ReservationDetails[] {});
        } catch (Exception e) {
            throw e;
        } finally {
            if (c != null) {
                c.close();
            }
        }
    }

    private String getLibelleProduit(Connection c, String idProduit) {
        if (idProduit == null || idProduit.isEmpty()) {
            return "Produit inconnu";
        }
        try {
            IngredientsLib ingredientLib = new IngredientsLib();
            ingredientLib.setId(idProduit);
            IngredientsLib[] resultsLib = (IngredientsLib[]) CGenUtil.rechercher(ingredientLib, null, null, c, "");
            if (resultsLib != null && resultsLib.length > 0) {
                String libelle = resultsLib[0].getLibelle();
                if (libelle != null && !libelle.isEmpty()) {
                    return libelle;
                }
            }
            return "Produit " + idProduit;
        } catch (Exception e) {
            return "Produit " + idProduit;
        }
    }

    private long convertHeureToSeconds(String heure) {
        if (heure == null || heure.isEmpty()) {
            return 0;
        }
        try {
            String[] parts = heure.split(":");
            long heures = Long.parseLong(parts[0]);
            long minutes = parts.length > 1 ? Long.parseLong(parts[1]) : 0;
            long secondes = parts.length > 2 ? Long.parseLong(parts[2]) : 0;
            return heures * 3600 + minutes * 60 + secondes;
        } catch (Exception e) {
            return 0;
        }
    }

    private String secondsToHeure(long totalSeconds) {
        long heures = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long secondes = totalSeconds % 60;
        return String.format("%02d:%02d:%02d", heures, minutes, secondes);
    }

    /**
     * Recupere TOUTES les reservations existantes pour une date et un support
     */
    private ReservationDetails[] getAllExistingReservations(Connection c, Date daty, String idSupport) {
        try {
            ReservationDetails search = new ReservationDetails();
            String dateStr = daty.toLocalDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            String condition = " and DATY = TIMESTAMP '" + dateStr + " 00:00:00.000000'";
            // if (idSupport != null && !idSupport.isEmpty()) {
            //     condition += " and idmere in (select id from reservation where idsupport = '" + idSupport + "')";
            // }
            ReservationDetails[] results = (ReservationDetails[]) CGenUtil.rechercher(search, null, null, c, condition);
            if (results != null && results.length > 0) {
                // Pour chaque resultat, recuperer l'idMedia depuis la base si null
                for (ReservationDetails result : results) {
                    if (result.getIdMedia() == null || result.getIdMedia().isEmpty()) {
                        String idMedia = getIdMediaFromDb(c, result.getId());
                        result.setIdMedia(idMedia);
                    }
                }
                return results;
            }
            return new ReservationDetails[0];
        } catch (Exception e) {
            return new ReservationDetails[0];
        }
    }
    
    /**
     * Verifie les conflits avec les reservations existantes
     * - Si existantIdMedia est NULL -> Ignorer (pas de media associe, pas de conflit)
     * - Si MEME media (non-null) + chevauchement horaire -> return true (conflit, chercher autre date)
     * - Si media DIFFERENT (les deux non-null) + chevauchement horaire -> Exception (plage occupee par autre media)
     * @return true si conflit avec meme media, false sinon
     * @throws Exception si chevauchement avec un media different
     */
    private boolean hasConflict(ReservationDetails[] existantes, String newIdMedia, long newHeureDebut, long newHeureFin) throws Exception {
        for (ReservationDetails existant : existantes) {
            String existantIdMedia = existant.getIdMedia();
            
            // Si l'existant n'a pas de media, on l'ignore (pas de conflit possible)
            if (existantIdMedia == null || existantIdMedia.isEmpty()) {
                System.out.println("    Reservation " + existant.getId() + " ignoree (pas de media associe)");
                continue;
            }
            
            // Calculer les plages horaires de l'existant
            long existantHeureDebut = convertHeureToSeconds(existant.getHeure());
            long existantDuree = Long.parseLong(existant.getDuree() != null && !existant.getDuree().isEmpty() ? existant.getDuree() : "0");
            long existantHeureFin = existantHeureDebut + existantDuree;
            
            // Verifier chevauchement: debut1 < fin2 ET debut2 < fin1
            boolean chevauchement = newHeureDebut < existantHeureFin && existantHeureDebut < newHeureFin;
            
            // Verifier si c'est le meme media (les deux sont non-null ici)
            boolean memeMedia = existantIdMedia.equals(newIdMedia);
            
            System.out.println("    Comparaison avec reservation " + existant.getId() + ":");
            System.out.println("      Media existant: " + existantIdMedia + " | Media nouveau: " + newIdMedia + " | Meme media: " + memeMedia);
            System.out.println("      Existant - Heure: " + existant.getHeure() + " (" + existantHeureDebut + "s) | Duree: " + existant.getDuree() + "s | Fin: " + secondsToHeure(existantHeureFin) + " (" + existantHeureFin + "s)");
            System.out.println("      Nouveau  - Heure: " + secondsToHeure(newHeureDebut) + " (" + newHeureDebut + "s) | Duree: " + (newHeureFin - newHeureDebut) + "s | Fin: " + secondsToHeure(newHeureFin) + " (" + newHeureFin + "s)");
            System.out.println("      Chevauchement: " + chevauchement);
            
            if (chevauchement) {
                if (memeMedia) {
                    // MEME media + chevauchement = conflit (chercher autre date)
                    System.out.println("      => CONFLIT MEME MEDIA - chercher autre date");
                    return true;
                } else {
                    // Media DIFFERENT (les deux non-null) + chevauchement = EXCEPTION
                    String message = "ERREUR: La plage horaire " + secondsToHeure(newHeureDebut) + " - " + secondsToHeure(newHeureFin) 
                            + " est deja occupee par un autre media (Media existant: " + existantIdMedia 
                            + ", Media nouveau: " + newIdMedia + ")";
                    System.out.println("      => EXCEPTION: " + message);
                    throw new Exception(message);
                }
            }
        }
        return false;
    }
    

    private String getIdMediaFromDb(Connection c, String idReservationDetails) {
        java.sql.PreparedStatement pstmt = null;
        java.sql.ResultSet rs = null;
        try {
            String sql = "SELECT IDMEDIA FROM RESERVATIONDETAILS WHERE ID = ?";
            pstmt = c.prepareStatement(sql);
            pstmt.setString(1, idReservationDetails);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                String idMedia = rs.getString("IDMEDIA");
                // System.out.println("    [DB] IdMedia recupere pour " + idReservationDetails + ": " + idMedia);
                return idMedia;
            }
        } catch (Exception e) {
            System.out.println("    [DB] Erreur recuperation IdMedia: " + e.getMessage());
        } finally {
            try { if (rs != null) rs.close(); } catch (Exception e) {}
            try { if (pstmt != null) pstmt.close(); } catch (Exception e) {}
        }
        return null;
    }

    public ReservationDetails[] genererReservationDetailsPourModif(Connection c) throws Exception {
        ReservationDetails search = new ReservationDetails();
        search.setIdmere(this.getIdmere());
        search.setOrdre(this.getOrdre());
        search.setIdproduit(this.getIdproduit());
        search.setHeure(this.getHeure());
        search.setPu(this.getPu());
        search.setDuree(this.getDuree().isEmpty() == false ? this.getDuree() : "0");
        search.setRemarque(this.getRemarque());
        search.setSource(this.getSource());
        search.setIdMedia(this.getIdmedia());
        search.setIsEntete(this.getIsEntete());
        search.updateAllByOrdre(c);

        List<ReservationDetails> reservationDetails = new ArrayList<ReservationDetails>();
        String[] listDate = this.getDateDiffusion().split(";");
        String[] listDateInvalide = this.getDateInvalide().split(";");
        Map<String, Boolean> dtInvalide = new HashMap<>();
        for (String d : listDateInvalide) {
            dtInvalide.put(d, true);
        }
        for (String d : listDate) {
            if (dtInvalide.get(d) == null) {
                ReservationDetails res = new ReservationDetails();
                res.setIdmere(this.getIdmere());
                res.setIdproduit(this.getIdproduit());
                res.setIdMedia(this.getIdmedia());
                res.setHeure(this.getHeure());
                res.setDaty(Date.valueOf(LocalDate.parse(d, DateTimeFormatter.ofPattern("dd/MM/yyyy"))));
                res.setRemarque(this.getRemarque());
                res.setSource(this.getSource());
                res.setDuree(this.getDuree());
                res.setPu(this.getPu());
                res.setQte(1);
                res.setIsEntete(this.getIsEntete());
                res.setOrdre(this.getOrdre());
                reservationDetails.add(res);
            }
        }
        return reservationDetails.toArray(new ReservationDetails[] {});
    }

    public double getPu() {
        return pu;
    }

    public void setPu(double pu) {
        this.pu = pu;
    }

    @Override
    public ClassMAPTable createObject(String u, Connection c) throws Exception {
        boolean estOuvert = false;
        try {
            if (c == null) {
                c = new UtilDB().GetConn();
                estOuvert = true;
            }

            ReservationDetails[] reservationDetails = genererReservationDetails();
            for (ReservationDetails reservationDetail : reservationDetails) {
                reservationDetail.createObject(u, c);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            c.rollback();
            throw ex;
        } finally {
            if (c != null && estOuvert == true) {
                c.close();
            }
        }
        return null;
    }

    public void setHeure(String heure) throws Exception {
        this.heure = heure;
    }

    public void setDuree(String duree) {
        if (!CalendarUtil.isValidTime(duree)) {
            this.duree = duree;
        } else {
            this.duree = String.valueOf(CalendarUtil.HMSToSecond(duree));
        }
    }

    public Reservation genererReservationApresSaisieMultiple(HttpServletRequest request) throws Exception {
        Reservation reservation = new Reservation();
        if (request.getParameter("idBc") != null && request.getParameter("idBc").isEmpty() == false) {
            reservation.setIdBc(request.getParameter("idBc"));
        }
        reservation.setIdSupport(request.getParameter("idSupport"));
        reservation.setIdclient(request.getParameter("idclient"));
        reservation.setDaty(Date.valueOf(request.getParameter("daty")));
        reservation.setRemarque(request.getParameter("remarque"));
        String[] list = request.getParameterValues("ids");
        List<ReservationDetails> filles = new ArrayList<>();
        int ordre = 0;
        for (String id : list) {
            int isEntete = Integer.parseInt(request.getParameter("isEntete_" + id));
            String idproduit = request.getParameter("idproduit_" + id);
            String heure_debut = request.getParameter("heure_" + id);
            String remarque = request.getParameter("remarque_" + id);
            String source = request.getParameter("source_" + id);
            String idmedia = request.getParameter("idmedia_" + id);
            String duree = request.getParameter("duree_" + id);
            double pu = 0;
            if (request.getParameter("pu_" + id) != null && request.getParameter("pu_" + id).isEmpty() == false) {
                pu = Double.parseDouble(request.getParameter("pu_" + id));
            }
            String champDate = request.getParameter("listDate_" + id);
            if (champDate != null && champDate.isEmpty() == false) {
                String[] listDate = champDate.split(";");
                for (String d : listDate) {
                    System.out.println(isEntete);
                    if (d != null && d.isEmpty() == false) {
                        Date date = Utilitaire.stringDate(d);
                        ReservationDetails reservationDetails = new ReservationDetails();
                        reservationDetails.setIdproduit(idproduit);
                        reservationDetails.setIdMedia(idmedia);
                        reservationDetails.setDaty(date);
                        reservationDetails.setHeure(heure_debut);
                        reservationDetails.setPu(pu);
                        reservationDetails.setRemarque(remarque);
                        reservationDetails.setSource(source);
                        reservationDetails.setQte(1);
                        reservationDetails.setDuree(duree);
                        reservationDetails.setIsEntete(isEntete);
                        reservationDetails.setOrdre(ordre);
                        filles.add(reservationDetails);
                    }
                }
            }
            ordre++;
        }
        reservation.setFille(filles.toArray(new ReservationDetails[] {}));
        return reservation;
    }

    public Reservation genererReservationApresSaisieMultipleAmeliorer(HttpServletRequest request) throws Exception {
        Reservation reservation = new Reservation();
        if (request.getParameter("idBc") != null && request.getParameter("idBc").isEmpty() == false) {
            reservation.setIdBc(request.getParameter("idBc"));
        }
        reservation.setIdSupport(request.getParameter("idSupport"));
        reservation.setIdclient(request.getParameter("idclient"));
        reservation.setDaty(Date.valueOf(request.getParameter("daty")));
        reservation.setRemarque(request.getParameter("remarque"));
        String[] list = request.getParameterValues("ids");
        List<ReservationDetails> filles = new ArrayList<>();
        int ordre = 0;
        for (String id : list) {
            int isEntete = Integer.parseInt(request.getParameter("isEntete_" + id));
            String idproduit = request.getParameter("idproduit_" + id);
            String heure_debut = request.getParameter("heure_" + id);
            String remarque = request.getParameter("remarque_" + id);
            String source = request.getParameter("source_" + id);
            String idmedia = request.getParameter("idmedia_" + id);
            String duree = request.getParameter("duree_" + id);
            double pu = 0;
            if (request.getParameter("pu_" + id) != null && request.getParameter("pu_" + id).isEmpty() == false) {
                pu = Double.parseDouble(request.getParameter("pu_" + id));
            }

            LocalDate dateDebut = null;
            LocalDate dateFin = null;
            if (request.getParameter("dateDebut_" + id) != null
                    && request.getParameter("dateDebut_" + id).isEmpty() == false &&
                    request.getParameter("dateFin_" + id) != null
                    && request.getParameter("dateFin_" + id).isEmpty() == false) {
                dateDebut = Date.valueOf(request.getParameter("dateDebut_" + id)).toLocalDate();
                dateFin = Date.valueOf(request.getParameter("dateFin_" + id)).toLocalDate();
            } else {
                throw new Exception("La date debut et fin sont requise pour la ligne " + id);
            }
            String[] jours = request.getParameterValues("jours_" + id);
            String[] dateInvalides = request.getParameter("dateInvalide_" + id).split(";");
            Map<String, Boolean> dtInterdite = new HashMap<>();
            for (String j : dateInvalides) {
                dtInterdite.put(j, false);
            }
            Map<String, Boolean> jourValide = new HashMap<>();
            for (String j : jours) {
                jourValide.put(j, true);
            }

            while (!dateDebut.isAfter(dateFin)) {
                if (dtInterdite.get(dateDebut.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))) == null) {
                    if (jourValide.get(CalendarUtil.getDayOfWeek(dateDebut)) != null) {
                        ReservationDetails reservationDetails = new ReservationDetails();
                        reservationDetails.setIdproduit(idproduit);
                        reservationDetails.setIdMedia(idmedia);
                        reservationDetails.setDaty(Date.valueOf(dateDebut));
                        reservationDetails.setHeure(heure_debut);
                        reservationDetails.setPu(pu);
                        reservationDetails.setRemarque(remarque);
                        reservationDetails.setSource(source);
                        reservationDetails.setQte(1);
                        reservationDetails.setDuree(duree);
                        reservationDetails.setIsEntete(isEntete);
                        reservationDetails.setOrdre(ordre);
                        filles.add(reservationDetails);
                    }
                }
                dateDebut = dateDebut.plusDays(1);
            }
            ordre++;
        }
        reservation.setFille(filles.toArray(new ReservationDetails[] {}));
        return reservation;
    }

    public Reservation genererReservationApresSaisieMultiplePourEmission(HttpServletRequest request, Connection c)
            throws Exception {
        boolean estOuvert = false;
        try {
            if (c == null) {
                c = new UtilDB().GetConn();
                estOuvert = true;
            }
            Reservation reservation = new Reservation();
            reservation.setIdSupport(request.getParameter("idSupport"));
            reservation.setDaty(Date.valueOf(request.getParameter("daty")));
            reservation.setRemarque(request.getParameter("remarque"));
            String[] list = request.getParameterValues("ids");
            int nbJour = Integer.parseInt(request.getParameter("nbJours"));
            List<ReservationDetails> filles = new ArrayList<>();
            for (String id : list) {
                String serviceMedia = request.getParameter("idproduit_" + id);
                Emission emission = new Emission();
                emission.setId(serviceMedia);
                emission = (Emission) CGenUtil.rechercher(emission, null, null, c, "")[0];
                String heure_debut = request.getParameter("heure_" + id);
                String duree = request.getParameter("duree_" + id);
                double remise = 0;
                double pu = 0;
                for (int i = 0; i < nbJour; i++) {
                    String champDate = "date_" + id + "_" + i;
                    int quantite = Integer.parseInt(request.getParameter(champDate + "_quantite"));
                    Date date = Utilitaire.stringDate(request.getParameter(champDate + "_date"));
                    for (int j = 0; j < quantite; j++) {
                        ReservationDetails reservationDetails = new ReservationDetails();
                        reservationDetails.setIdproduit(serviceMedia);
                        reservationDetails.setDaty(date);
                        reservationDetails.setHeure(heure_debut);
                        reservationDetails.setPu(pu);
                        reservationDetails.setRemise(remise);
                        reservationDetails.setQte(1);
                        reservationDetails.setDuree(duree);

                        filles.add(reservationDetails);

                        ReservationDetails[] resaParrainage = emission.genererReservationPourSponsors(date, heure_debut,
                                c);
                        if (resaParrainage.length > 0) {
                            filles.addAll(Arrays.asList(resaParrainage));
                        }
                    }
                }
            }
            reservation.setFille(filles.toArray(new ReservationDetails[] {}));
            return reservation;
        } catch (Exception ex) {
            ex.printStackTrace();
            c.rollback();
            throw ex;
        } finally {
            if (c != null && estOuvert == true) {
                c.close();
            }
        }
    }

    public Reservation genererReservationApresModif(HttpServletRequest request, Connection c) throws Exception {
        boolean estOuvert = false;
        try {
            if (c == null) {
                c = new UtilDB().GetConn();
                estOuvert = true;
            }
            Reservation reservation = new Reservation();
            reservation.setId(request.getParameter("idResa"));
            reservation = (Reservation) CGenUtil.rechercher(reservation, null, null, c, "")[0];
            if (request.getParameter("idBc") != null && request.getParameter("idBc").isEmpty() == false) {
                reservation.setIdBc(request.getParameter("idBc"));
            }
            reservation.setIdSupport(request.getParameter("idSupport"));
            reservation.setIdclient(request.getParameter("idclient"));
            reservation.setDaty(Date.valueOf(request.getParameter("daty")));
            reservation.setRemarque(request.getParameter("remarque"));
            String idParrainage = null;
            if (reservation.getSource() != null && reservation.getSource().startsWith("PRE")) {
                idParrainage = reservation.getSource();
            }
            String[] list = request.getParameterValues("ids");
            List<ReservationDetails> filles = new ArrayList<>();
            if (list != null) {
                for (String id : list) {
                    int isEntete = Integer.parseInt(request.getParameter("isEntete_" + id));
                    String idproduit = request.getParameter("idproduit_" + id);
                    String heure_debut = request.getParameter("heure_" + id);
                    String remarque = request.getParameter("remarque_" + id);
                    String source = request.getParameter("source_" + id);
                    String idmedia = request.getParameter("idmedia_" + id);
                    String duree = request.getParameter("duree_" + id);
                    double pu = 0;
                    if (request.getParameter("pu_" + id) != null
                            && request.getParameter("pu_" + id).isEmpty() == false) {
                        pu = Double.parseDouble(request.getParameter("pu_" + id));
                    }
                    int ordre = Integer.parseInt(request.getParameter("ordre_" + id));
                    ReservationDetails search = new ReservationDetails();
                    search.setIdmere(reservation.getId());
                    search.setOrdre(ordre);
                    search.setIdproduit(idproduit);
                    search.setHeure(heure_debut);
                    search.setPu(pu);
                    search.setDuree(duree);
                    search.setRemarque(remarque);
                    search.setSource(source);
                    search.setIdMedia(idmedia);
                    search.setIsEntete(isEntete);
                    search.updateAllByOrdre(c);
                    String champDate = request.getParameter("listDate_" + id);
                    if (champDate != null && champDate.isEmpty() == false) {
                        String[] listDate = champDate.split(";");
                        for (String d : listDate) {
                            System.out.println(isEntete);
                            if (d != null && d.isEmpty() == false) {
                                Date date = Utilitaire.stringDate(d);
                                ReservationDetails reservationDetails = new ReservationDetails();
                                reservationDetails.setIdmere(reservation.getId());
                                reservationDetails.setIdproduit(idproduit);
                                reservationDetails.setIdMedia(idmedia);
                                reservationDetails.setDaty(date);
                                reservationDetails.setHeure(heure_debut);
                                reservationDetails.setPu(pu);
                                reservationDetails.setRemarque(remarque);
                                reservationDetails.setSource(source);
                                reservationDetails.setQte(1);
                                reservationDetails.setDuree(duree);
                                reservationDetails.setIsEntete(isEntete);
                                reservationDetails.setOrdre(ordre);
                                reservationDetails.setIdparrainage(idParrainage);
                                filles.add(reservationDetails);
                            }
                        }
                    }
                }
            }
            reservation.setFille(filles.toArray(new ReservationDetails[] {}));
            return reservation;
        } catch (Exception ex) {
            ex.printStackTrace();
            c.rollback();
            throw ex;
        } finally {
            if (c != null && estOuvert == true) {
                c.close();
            }
        }
    }
}