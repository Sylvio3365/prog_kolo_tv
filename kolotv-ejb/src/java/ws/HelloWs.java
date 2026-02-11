package ws;

import java.sql.Connection;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import param.Parametre;
import org.json.JSONObject;
import org.json.JSONArray;
import utilitaire.UtilDB;
import annexe.ProduitLib;
import bean.CGenUtil;

@Path("/hello")
public class HelloWs {
    // @GET
    // @Produces(MediaType.APPLICATION_JSON)
    // public Response hello() throws Exception {
    // JSONObject valiny = new JSONObject();
    // valiny.put("message", "Hello world ! Kolotv");
    // return Response.ok(valiny.toString()).build();
    // }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response hello() throws Exception {
        try (Connection c = new UtilDB().GetConn()) {
            Parametre parametre = new Parametre();
            Parametre[] parametres = (Parametre[]) CGenUtil.rechercher(parametre, null, null, c, "");
            JSONArray jsonArray = new JSONArray();
            for (Parametre p : parametres) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("id", p.getId());
                jsonObject.put("jour", p.getJour());
                jsonObject.put("heure_debut", p.getHeuredebut());
                jsonObject.put("heure_fin", p.getHeurefin());
                jsonObject.put("pourcentage", p.getPourcentage());
                jsonArray.put(jsonObject);
            }
            JSONObject valiny = new JSONObject();
            valiny.put("message", "Hello world ! Kolotv");
            valiny.put("parametres", jsonArray);
            return Response.ok(valiny.toString()).build();
        }
    }
}
