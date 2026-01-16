<|editable_region_start|>
 package data;

import java.util.*;
import java.io.*;
import java.net.*;
import java.text.*;
    

public class Mdb{


    private String url1 = "http://api.themoviedb.org/3/search/movie";
    private String url2 = "http://api.themoviedb.org/3/movie";

    private String api_key = System.getenv("KEY");

    private String img_route = "http://cf2.imgobject.com/t/p/original";
    private String img_small = "http://cf2.imgobject.com/t/p/w185";
    

    private String[][] results;


    public int busqueda(String query, int num_res) throws Exception{
    
        if(num_res>20) num_res=20;
    
        String str = new String();
        str = str + "?api_key=" + URLEncoder.encode(api_key, "UTF-8");
        str = str + "&query=" + URLEncoder.encode(query, "UTF-8");
        str = str + "&language=" + URLEncoder.encode("es", "UTF-8");
    
    
        URL u = new URL(url1+str);
        URLConnection uc = u.openConnection();
        uc.setRequestProperty("Accept","application/json");
    
    
        BufferedReader in = new BufferedReader(new InputStreamReader(uc.getInputStream(), "UTF-8"));
        String res = in.readLine();
        in.close();
        
        int num = getTotalResults(res);
        
        if(num_res < num)
        num = num_res;
        results = new String[num][4];
        setBusquedaValues(res, results);
        return num;
    }

    private int getTotalResults(String res){
    
        int pos = res.indexOf("total_results");
        
        pos += 15;
    
        String r = res.substring(pos, res.indexOf("}", pos));
        
        int tr = Integer.parseInt(r);
        
        if(tr>20)
        tr=20;
    
        return tr;
    
    }

    private void setBusquedaValues(String res, String[][] rellenar){
    
        setIntValuesNum(res, rellenar, "id", 0);
        setIntValues(res, rellenar, "title", 1);
        setIntValues(res, rellenar, "release_date", 2);
        setIntValues(res, rellenar, "poster_path", 3);
    
    }
}<|editable_region_end|>
```
