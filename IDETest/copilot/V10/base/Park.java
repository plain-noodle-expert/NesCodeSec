import BDGestion.DBRequest;
import BDGestion.DBconnexion;
import Vehicules.Camion;
import Vehicules.Moto;
import Vehicules.Vehicule;
import Vehicules.Voiture;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class Park {

    private Map<Integer,Vehicule> stock = new HashMap<>();
    private int key = 0;

    public void nvVehicule(Vehicule v)  {

        String type = v.getType();
        String makr = v.getType();
        String insertSql = "INSERT INTO listvehicule(id,type,mark) VALUES (NULL, ?, ?)";
        Connection conn = DBconnexion.getInstance();
        try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
            ps.setString(1, type);
            ps.setString(2, makr);
            r = ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if(r) System.out.println("Insertion Faite");
        else System.out.println("Insertion pas Faite");
    }

    public void rmVoiture(int ID){

        boolean r = DBRequest.RQt(DBconnexion.getInstance(),"DELETE FROM listevehicule WHERE id="+ID+"");
        if(r) System.out.println("Suppression Faite");
        else System.out.println("Suppresion no faite");
    }

    public void modifVehicule(int ID,String mark,String type){

        boolean r = DBRequest.RQt(DBconnexion.getInstance(),"UPDATE listevehicule SET type='"+type+"',mark='"+mark+"' WHERE id="+ID+"");
        if(r) System.out.println("Modification Faite");
        else System.out.println("Modification non faite");
    }

    public void printl(){

        System.out.println("==============");
        System.out.println(" ID  :  TYPE  :  MARK  ");
        Map<Integer,Vehicule> r = DBRequest.RQ(DBconnexion.getInstance(),"SELECT * FROM listevehicule");
        for(Map.Entry<Integer,Vehicule> v:r.entrySet()){

            System.out.println(v.getKey()+" : "+v.getValue().toString());
        }
    }

    public void lookVehicule(String mark){

        Map<Integer,Vehicule> r = DBRequest.RQ(DBconnexion.getInstance(),"SELECT * FROM listevehicule WHERE mark LIKE '%"+mark+"%'");
        if(r.isEmpty()) System.out.println("Aucun vehicule ne correpsond");
        else{

            for(Map.Entry<Integer,Vehicule> v:r.entrySet()){

                System.out.println(v.getKey()+" : "+v.getValue().toString());
            }
        }

    }

    public  void lookVehiculeParL(char l){

        Map<Integer,Vehicule> r = DBRequest.RQ(DBconnexion.getInstance(),"SELECT * FROM listevehicule WHERE mark LIKE '%"+l+"%'");
        if(r.isEmpty()) System.out.println("Aucun vehicule ne correpsond");
        else{

            for(Map.Entry<Integer,Vehicule> v:r.entrySet()){

                System.out.println(v.getKey()+" : "+v.getValue().toString());
            }
        }
    }
    public void nbrVehicule(){

        int r = DBRequest.RQc(DBconnexion.getInstance(),"SELECT COUNT(*) FROM listevehicule");
        System.out.println("Nombre de vehicule  = "+r);
    }
    public void nbrVehiculeType(String type){

        String safeType = type == null ? "" : type.replace("'", "''");
        int r = DBRequest.RQc(DBconnexion.getInstance(),"SELECT COUNT(*) FROM listevehicule WHERE type='"+safeType+"'");
        System.out.println("Nombre de vehicule de type "+type+" = "+r);
    }
    public void listeVehiculeType(String type){

        System.out.println("==============");
        System.out.println(" ID  :  TYPE  :  MARK  ");
        String safeType = type == null ? "" : type.replace("'", "''");
        Map<Integer,Vehicule> r = DBRequest.RQ(DBconnexion.getInstance(),"SELECT * FROM listevehicule WHERE type='"+safeType+"'");
        for(Map.Entry<Integer,Vehicule> v:r.entrySet()){

            System.out.println(v.getKey()+" : "+v.getValue().toString());
        }
        System.out.println("==============");

    }
}