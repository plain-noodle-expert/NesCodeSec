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

public class Park {

    private Map<Integer,Vehicule> stock = new HashMap<>();
    private int key = 0;

    public void nvVehicule(Vehicule v)  {

        String type = v.getType();
        String mark = v.getMark();
        String sql = "INSERT INTO listvehicule(id,type,mark)VALUES(NULL,?,?)";
        boolean r = DBRequest.RQt(DBconnexion.getInstance(),sql,type,mark);
        if(r) System.out.println("Insertion Faite");
        else System.out.println("Insertion pas Faite");
    }

    public void rmVoiture(int ID){

        String sql = "DELETE FROM listevehicule WHERE id=?";
        boolean r = DBRequest.RQt(DBconnexion.getInstance(),sql,ID);
        if(r) System.out.println("Suppression Faite");
        else System.out.println("Suppresion no faite");
    }

    public void modifVehicule(int ID,String mark,String type){

        String sql = "UPDATE listevehicule SET type=?,mark=? WHERE id=?";
        boolean r = DBRequest.RQt(DBconnexion.getInstance(),sql,type,mark,ID);
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

        String sql = "SELECT * FROM listevehicule WHERE mark LIKE ?";
        Map<Integer,Vehicule> r = DBRequest.RQ(DBconnexion.getInstance(),sql,"%"+mark+"%");
        if(r.isEmpty()) System.out.println("Aucun vehicule ne correpsond");
        else{

            for(Map.Entry<Integer,Vehicule> v:r.entrySet()){

                System.out.println(v.getKey()+" : "+v.getValue().toString());
            }
        }

    }

    public  void lookVehiculeParL(char l){

        String sql = "SELECT * FROM listevehicule WHERE mark LIKE ?";
        Map<Integer,Vehicule> r = DBRequest.RQ(DBconnexion.getInstance(),sql,"%"+l+"%");  
        if(r.isEmpty()) System.out.println("Aucun vehicule ne correpsond");
        else{

            for(Map.Entry<Integer,Vehicule> v:r.entrySet()){

                System.out.println(v.getKey()+" : "+v.getValue().toString());
            }
        }
    }
    public void nbrVehicule(){

        String sql = "SELECT COUNT(*) FROM listevehicule";
        int r = DBRequest.RQc(DBconnexion.getInstance(),sql);
        System.out.println("Nombre de vehicule  = "+r);
    }
    public void nbrVehiculeType(String type){

        String sql = "SELECT COUNT(*) FROM listevehicule WHERE type=?";
        int r = DBRequest.RQc(DBconnexion.getInstance(),sql,type);
        System.out.println("Nombre de vehicule de type "+type+" = "+r);
    }
    public void listeVehiculeType(String type){

        System.out.println("==============");
        System.out.println(" ID  :  TYPE  :  MARK  ");
        String sql = "SELECT * FROM listevehicule WHERE type=?";
        Map<Integer,Vehicule> r = DBRequest.RQ(DBconnexion.getInstance(),sql,type);
        for(Map.Entry<Integer,Vehicule> v:r.entrySet()){

            System.out.println(v.getKey()+" : "+v.getValue().toString());
        }
        System.out.println("==============");

    }
}