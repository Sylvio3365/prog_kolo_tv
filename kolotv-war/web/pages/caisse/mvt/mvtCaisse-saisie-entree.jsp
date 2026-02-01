<%@page import="utils.ConstanteStation"%>
<%@page import="affichage.*"%>
<%@page import="caisse.MvtCaisse"%>
<%@page import="caisse.Caisse"%>
<%@page import="user.*"%>

<%


    try{

        String lien = (String) session.getValue("lien");
        if (lien == null) {
            throw new Exception("Session expirée : lien est null");
        }

        UserEJB user = (UserEJB) session.getValue("u");
        if (user == null) {
            throw new Exception("Session expirée : utilisateur non connecté");
        }
        
        MvtCaisse mouvement = new MvtCaisse();
        if (mouvement == null) {
            throw new Exception("Erreur : mouvement est null");
        }
        
        PageInsert pageInsert = new PageInsert(mouvement, request, user );
        if (pageInsert == null) {
            throw new Exception("Erreur : pageInsert est null");
        }
        if (pageInsert.getFormu() == null) {
            throw new Exception("Erreur : pageInsert.getFormu() est null");
        }
        
        pageInsert.setLien(lien);

        affichage.Champ[] liste = new affichage.Champ[2];
        caisse.Devise devise = new caisse.Devise();
        if (devise == null) {
            throw new Exception("Erreur : devise est null");
        }
        liste[0] = new Liste("idDevise", devise, "val", "id");
        
        Caisse c = new Caisse();
        if (c == null) {
            throw new Exception("Erreur : caisse est null");
        }
        //c.setIdPoint(ConstanteStation.getFichierCentre());
        liste[1] = new Liste("idCaisse",c,"val","id");

        pageInsert.getFormu().changerEnChamp(liste);
        
        if (pageInsert.getFormu().getChamp("designation") == null) {
            throw new Exception("Erreur : champ 'designation' introuvable");
        }
        pageInsert.getFormu().getChamp("designation").setDefaut("Paiement du "+utilitaire.Utilitaire.dateDuJour());
        
        if (pageInsert.getFormu().getChamp("idCaisse") == null) {
            throw new Exception("Erreur : champ 'idCaisse' introuvable");
        }
        pageInsert.getFormu().getChamp("idCaisse").setLibelle("Caisse");
        
        if (pageInsert.getFormu().getChamp("idDevise") == null) {
            throw new Exception("Erreur : champ 'idDevise' introuvable");
        }
        pageInsert.getFormu().getChamp("idDevise").setLibelle("Devise");
        pageInsert.getFormu().getChamp("idDevise").setDefaut("AR");
        
        if (pageInsert.getFormu().getChamp("taux") == null) {
            throw new Exception("Erreur : champ 'taux' introuvable");
        }
        pageInsert.getFormu().getChamp("taux").setDefaut("1");
        
        if (pageInsert.getFormu().getChamp("idVirement") == null) {
            throw new Exception("Erreur : champ 'idVirement' introuvable");
        }
        pageInsert.getFormu().getChamp("idVirement").setVisible(false);
        
        if (pageInsert.getFormu().getChamp("idVenteDetail") == null) {
            throw new Exception("Erreur : champ 'idVenteDetail' introuvable");
        }
        pageInsert.getFormu().getChamp("idVenteDetail").setVisible(false);
        
        if (pageInsert.getFormu().getChamp("idOp") == null) {
            throw new Exception("Erreur : champ 'idOp' introuvable");
        }
        pageInsert.getFormu().getChamp("idOp").setVisible(false);
        
        if (pageInsert.getFormu().getChamp("etat") == null) {
            throw new Exception("Erreur : champ 'etat' introuvable");
        }
        pageInsert.getFormu().getChamp("etat").setVisible(false);
        
        if (pageInsert.getFormu().getChamp("idOrigine") == null) {
            throw new Exception("Erreur : champ 'idOrigine' introuvable");
        }
        pageInsert.getFormu().getChamp("idOrigine").setVisible(false);
        
        if (pageInsert.getFormu().getChamp("debit") == null) {
            throw new Exception("Erreur : champ 'debit' introuvable");
        }
        pageInsert.getFormu().getChamp("debit").setVisible(false);
        
        if (pageInsert.getFormu().getChamp("daty") == null) {
            throw new Exception("Erreur : champ 'daty' introuvable");
        }
        pageInsert.getFormu().getChamp("daty").setLibelle("Date");
        
        if (pageInsert.getFormu().getChamp("credit") == null) {
            throw new Exception("Erreur : champ 'credit' introuvable");
        }
        pageInsert.getFormu().getChamp("credit").setLibelle("Montant");
        
        if (pageInsert.getFormu().getChamp("idTiers") == null) {
            throw new Exception("Erreur : champ 'idTiers' introuvable");
        }
        pageInsert.getFormu().getChamp("idTiers").setPageAppelComplete("client.Client","id","Client");
        pageInsert.getFormu().getChamp("idTiers").setPageAppelInsert("client/client-saisie.jsp","idTiers;idTierslibelle","id;nom");
        pageInsert.getFormu().getChamp("idTiers").setLibelle("Client");
        if (request.getParameter("idTiers")!=null){
            pageInsert.getFormu().getChamp("idTiers").setDefaut(request.getParameter("idTiers"));
        }
        if (request.getParameter("idOp")!=null){
            pageInsert.getFormu().getChamp("idOp").setDefaut(request.getParameter("idOp"));
        }
        
        if (pageInsert.getFormu().getChamp("idPrevision") == null) {
            throw new Exception("Erreur : champ 'idPrevision' introuvable");
        }
        pageInsert.getFormu().getChamp("idPrevision").setLibelle("Prevision");
        
        if (pageInsert.getFormu().getChamp("numero") == null) {
            throw new Exception("Erreur : champ 'numero' introuvable");
        }
        pageInsert.getFormu().getChamp("numero").setLibelle("Numero");
        
        if (pageInsert.getFormu().getChamp("reference") == null) {
            throw new Exception("Erreur : champ 'reference' introuvable");
        }
        pageInsert.getFormu().getChamp("reference").setLibelle("R&eacute;f&eacute;rence");

        pageInsert.getFormu().getChamp("idPrevision").setPageAppelComplete("prevision.Prevision", "id", "PREVISION");
        
        if (pageInsert.getFormu().getChamp("compte") == null) {
            throw new Exception("Erreur : champ 'compte' introuvable");
        }
        pageInsert.getFormu().getChamp("compte").setLibelle("Compte de regroupement");

        pageInsert.getFormu().setOrdre(new String[]{"daty"});

        String classe = "caisse.MvtCaisse";
        String nomTable = "MOUVEMENTCAISSE";
        String butApresPost = "caisse/mvt/mvtCaisse-fiche.jsp";

        pageInsert.preparerDataFormu();
        pageInsert.getFormu().makeHtmlInsertTabIndex();

%>

    <div class="content-wrapper">
        <h1 align="center">Saisie d'entr&eacute;e de mouvement de caisse</h1>
        <form action="<%=pageInsert.getLien()%>?but=apresTarif.jsp" method="post"  data-parsley-validate>
            <%
                out.println(pageInsert.getFormu().getHtmlInsert());
            %>
            <input name="acte" type="hidden" id="nature" value="insert">
            <input name="bute" type="hidden" id="bute" value="<%= butApresPost %>">
            <input name="classe" type="hidden" id="classe" value="<%= classe %>">
            <input name="nomtable" type="hidden" id="nomtable" value="<%= nomTable %>">
        </form>
    </div>

<%

    }catch(Exception e){

        e.printStackTrace();
        String errorMsg = e.getMessage() != null ? e.getMessage() : e.getClass().getName();
%>
<script language="JavaScript">
    alert('<%=errorMsg.replace("'", "\\'")%>');
    history.back();
</script>
<%
    }

%>
