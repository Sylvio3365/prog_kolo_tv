<%@ page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ page import="user.*" %>
<%@ page import="bean.*" %>
<%@ page import="media.*" %>
<%@ page import="affichage.*" %>

<%
    try {
        UserEJB u = (UserEJB) session.getAttribute("u");
        String lien = (String) session.getValue("lien");
        String id = request.getParameter("id");
        
        // Recuperer les valeurs du formulaire
        Media media = new Media();
        Page p = new Page(media, request);
        Media f = (Media) p.getObjectAvecValeur();
        f.setNomTable("Media");
        
        // Effectuer la mise a jour
        u.updateObject(f);
        
        // Rediriger vers la fiche media
        String redirect = lien + "?but=media/media-fiche.jsp&id=" + id;
        System.out.println("Redirecting to: " + redirect);
%>
<script language="JavaScript">
    console.log("Redirecting to: <%=redirect%>");
    document.location.replace("<%=redirect%>");
</script>
<%
    } catch (Exception e) {
        e.printStackTrace();
%>
<script language="JavaScript">
    alert("Erreur: <%=e.getMessage()%>");
    history.back();
</script>
<%
    }
%>
