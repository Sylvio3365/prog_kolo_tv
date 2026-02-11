package param;

import java.sql.Connection;

import bean.ClassMAPTable;

public class Parametre extends ClassMAPTable {

    String id;
    int jour;
    String heuredebut;
    String heurefin;
    double pourcentage;

    public Parametre() {
        setNomTable("PARAMETRE");
    }

    public void construirePK(Connection c) throws Exception {
        this.preparePk("PAR", "GETSEQ_DUREEMAXSPOT");
        this.setId(this.makePK(c));
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getJour() {
        return jour;
    }

    public void setJour(int jour) {
        this.jour = jour;
    }

    public String getHeuredebut() {
        return heuredebut;
    }

    public void setHeuredebut(String heuredebut) {
        this.heuredebut = heuredebut;
    }

    public String getHeurefin() {
        return heurefin;
    }

    public void setHeurefin(String heurefin) {
        this.heurefin = heurefin;
    }

    public double getPourcentage() {
        return pourcentage;
    }

    public void setPourcentage(double pourcentage) {
        this.pourcentage = pourcentage;
    }

    @Override
    public String getTuppleID() {
        return this.getId();
    }

    @Override
    public String getAttributIDName() {
        return "id";
    }
}
