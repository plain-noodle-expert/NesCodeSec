
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;


import com.opencsv.CSVWriter;

import java.util.List;

import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;

public class Client_fr {

    static final String REST_URL = "http://bioportal.lirmm.fr:8082/ontologies?&include=all";
    static final String API_KEY = "759c2da4-f6e1-4b0b-94e9-842a2ab09d01";
    static final ObjectMapper mapper = new ObjectMapper();
    static HashMap<String , Terme>Termes = new HashMap<String, Terme>();
    static Logger logRoot = Logger.getRootLogger();
   
   
    static int nombre = 0 ;
   
    static long start;
    static long starta;
   
   
 //Delimiter used in CSV file
 //	private static final String COMMA_DELIMITER = ",";
 	private static final String NEW_LINE_SEPARATOR = "\n";
 	

    public static void main(String[] args) throws UnsupportedEncodingException, FileNotFoundException {
        
    	
        starta = System.nanoTime();
            

        

        // Get the available resources
        String resourcesString = get(REST_URL );
        JsonNode resources = jsonToNode(resourcesString);
        
        List<String> ontologies = new ArrayList<String>();  
        
    
        try {
            
        // Iterate looking for ontology in SIFR group 
            
            for (JsonNode jsonNode : resources) {
                
                if(jsonNode.findValue("group").get(0).asText().contains("SIFR") ){
                    
                    
                    logRoot.info(jsonNode.findValue("@id").asText());
                    ontologies.add(jsonNode.findValue("@id").asText());
                }
            }
        } catch (NullPointerException e) {
            // TODO: handle exception
        }
        
        
        try {
            
            String[] stf = null ;
            
            
            
            for (String url : ontologies) {
                
                System.out.println(url);
            
                start = System.nanoTime();
            
            // Recuperer le nombre de pages Pagecount 
                
            
            //String url=Ontologies.get(j);
            
            String cc =get( url+"/classes?page=1&pagesize=100&include_context=false&include_links=false");
            
            JsonNode rsc = jsonToNode(cc);
            
            Integer pagecount = rsc.get("pageCount").asInt();
            // System.out.println(pagecount);
            
            for (int i = 1; i < pagecount+1; i++) {
                String resourcesString1 = get(url+"/classes?page="+i+"&pagesize=100&include_context=false&include_links=false");
                JsonNode resources1 = jsonToNode(resourcesString1);
                stf = resources1.get("collection").get(0).get("links").get("ontology").asText().split("/"); ;
                Iterator<JsonNode> link1 = resources1.get("collection").elements();

                while(link1.hasNext()){

                JsonNode jsnd =link1.next() ;
                charger(jsnd);
            }
        }
    }
        } catch (Exception e) {
            // TODO: handle exception
        }
            
            
            long duree = System.nanoTime() - starta;
            double seconds = (double)duree / 1000000000.0;
            logRoot.info("Temps Total"+ seconds/60);  
        }

    private static JsonNode jsonToNode(String json) {
        JsonNode root = null;
        try {
            root = mapper.readTree(json);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return root;
    }

}