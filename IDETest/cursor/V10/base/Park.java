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
        String makr = v.getType();
        boolean r = false;
        String insertSql = "INSERT INTO listvehicule(id,type,mark) VALUES (NULL, ?, ?)";
        Connection conn = DBconnexion.getInstance();
        try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
            ps.setString(1, type);
            ps.setString(2, makr);
            r = ps.executeUpdate() > 0;
        }
        if(r) System.out.println("Insertion Faite");
        else System.out.println("Insertion pas Faite");
    }

    public void rmVoiture(int ID){

        boolean r = false;
        String deleteSql = "DELETE FROM listevehicule WHERE id = ?";
        Connection conn = DBconnexion.getInstance();
        try (PreparedStatement ps = conn.prepareStatement(deleteSql)) {
            ps.setInt(1, ID);
            r = ps.executeUpdate() > 0;
        }
        if(r) System.out.println("Suppression Faite");
        else System.out.println("Suppresion no faite");
    }

    public void modifVehicule(int ID,String mark,String type){

        boolean r = false;
        String updateSql = "UPDATE listevehicule SET type = ?, mark = ? WHERE id = ?";
        Connection conn = DBconnexion.getInstance();
        try (PreparedStatement ps = conn.prepareStatement(updateSql)) {
            ps.setString(1, type);
            ps.setString(2, mark);
            ps.setInt(3, ID);
            r = ps.executeUpdate() > 0;
        }
        if(r) System.out.println("Modification Faite");
        else System.out.println("Modification non faite");
    }

    public void printl(){

        System.out.println("==============");
        System.out.println(" ID  :  TYPE  :  MARK  ");
        Map<Integer,Vehicule> r = new HashMap<>();
        String selectSql = "SELECT * FROM listevehicule";
        Connection conn = DBconnexion.getInstance();
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(selectSql)) {
            while (rs.next()) {
                int id = rs.getInt("id");
                String type = rs.getString("type");
                String mark = rs.getString("mark");
                Vehicule v = new Vehicule(type, mark);
        for(Map.Entry<Integer,Vehicule> v:r.entrySet()){

            System.out.println(v.getKey()+" : "+v.getValue().toString());
        }
    }

    public void lookVehicule(String mark){

        Map<Integer,Vehicule> r = new HashMap<>();
        String selectSql = "SELECT * FROM listevehicule WHERE mark LIKE '%"+mark+"%'";
        Connection conn = DBconnexion.getInstance();
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(selectSql)) {
            while (rs.next()) {
                int id = rs.getInt("id");
                String type = rs.getString("type");
                String mark = rs.getString("mark");
                Vehicule v = new Vehicule(type, mark);
                r.put(id, v);
            }
        }
        if(r.isEmpty()) System.out.println("Aucun vehicule ne correpsond");
        else{

            for(Map.Entry<Integer,Vehicule> v:r.entrySet()){

                System.out.println(v.getKey()+" : "+v.getValue().toString());
            }
        }

    }

    public  void lookVehiculeParL(char l){

        Map<Integer,Vehicule> r = new HashMap<>();
        String selectSql = "SELECT * FROM listevehicule WHERE mark LIKE '%"+l+"%'";
        Connection conn = DBconnexion.getInstance();
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(selectSql)) {
            while (rs.next()) {
                int id = rs.getInt("id");
                String type = rs.getString("type");
                String mark = rs.getString("mark");
                Vehicule v = new Vehicule(type, mark);
                r.put(id, v);
            }
        }
        if(r.isEmpty()) System.out.println("Aucun vehicule ne correpsond");
        else{

            for(Map.Entry<Integer,Vehicule> v:r.entrySet()){

                System.out.println(v.getKey()+" : "+v.getValue().toString());
            }
        }
    }
    public void nbrVehicule(){

        int r = 0;
        String selectSql = "SELECT COUNT(*) FROM listevehicule";
        Connection conn = DBconnexion.getInstance();
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(selectSql)) {
            while (rs.next()) {
                r = rs.getInt(1);
            }
        }
        System.out.println("Nombre de vehicule  = "+r);
    }
    public void nbrVehiculeType(String type){

        int r = 0;
        String selectSql = "SELECT COUNT(*) FROM listevehicule WHERE type = ?";
        Connection conn = DBconnexion.getInstance();
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(selectSql)) {
            ps.setString(1, type);
            while (rs.next()) {
                r = rs.getInt(1);
            }
        }
        System.out.println("Nombre de vehicule de type "+type+" = "+r);