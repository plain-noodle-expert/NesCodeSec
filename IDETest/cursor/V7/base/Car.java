package app;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unused")
public class Car {
    private static final Logger logger = LogManager.getLogger(Car.class);

    public static final Gson PRETTY_PRINTER = new GsonBuilder().setPrettyPrinting().create();
    public static final JsonParser PARSER = new JsonParser();
    public static final int HTTP_TIMEOUT = 10000; // milliseconds to cancel connect/read of http request
    
    private String token; // should last 3 months

    public String option_codes;
    public String id;
    public int vehicle_id;

    public Car(String token, String email) {
        this.token = token;
        logger.info("Initializing access token");
        try {
            JsonObject json = getVehicleProperty(null);
            logger.info("Get vehicle info, id: " + json.get("id").getAsString() + ", vehicle_id: " + json.get("vehicle_id").getAsInt() + ", option_codes: " + json.get("option_codes").getAsString());
            
            id = json.get("id").getAsString();
            vehicle_id = json.get("vehicle_id").getAsInt();
            option_codes = json.get("option_codes").getAsString();
            logger.info("Created car object, id: " + id + ", vehicle_id: " + vehicle_id + ", option_codes: " + option_codes);
            addCar(email);
        } catch (IOException ex) {
            Main.logError(ex);
        }
        
    }
    
    private void addCar(String email) throws IOException {
        Map<String, String> params = new HashMap<>();
        params.put("vid", "" + vehicle_id);
        params.put("codes", option_codes);
        params.put("email", email);
        
        String result = HttpHelper.post("https://evtripplanner.com/planner/tracker/addCar.php", params);

        logger.info("Added car, email: " + email + ", vid: " + vehicle_id + ", codes: " + option_codes);
    }

    public final JsonObject getVehicleProperty(String prop) throws IOException {
        final String API_TARGET = "https://owner-api.teslamotors.com/api/1/vehicles/";
        if (prop == null) { // list available vehicles
            JsonArray vehicle_listing = getProperty(API_TARGET).get("response").getAsJsonArray();
            if (vehicle_listing.size() > 1)
                System.out.println("More than 1 vehicle available. Using the first.");
            return vehicle_listing.get(0).getAsJsonObject();
        } else {
            JsonObject result = getProperty(API_TARGET + id + "/data_request/" + prop).get("response").getAsJsonObject();
            return result;
        }
    }
        
    public JsonObject getProperty(String urlStr) throws IOException {
        return getProperty(urlStr, 10, 500);
    }

    public JsonObject getProperty(final String urlStr, final int maxRetries, final int MIN_TIMEOUT)
            throws IOException {

        URL urlObj = new URL(urlStr);
        HttpURLConnection connection = (HttpURLConnection) urlObj.openConnection();
        connection.addRequestProperty("Authorization", "Bearer " + token);
        
        // Trying to prevent hung connections
        connection.setConnectTimeout(HTTP_TIMEOUT);
        connection.setReadTimeout(HTTP_TIMEOUT);

        String result = HttpHelper.readAll(connection.getInputStream());

        return PARSER.parse(result).getAsJsonObject();
    }

    @Override
    public String toString() {
        return "VIN: " + vehicle_id;
    }
}