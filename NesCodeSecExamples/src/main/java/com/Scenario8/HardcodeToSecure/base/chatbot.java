import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class Chatbot {

    
    private static final String GEMINI_API_KEY = "sk-8ESJIGbXtwSO8eylYbkPS67Rq9APTF3omfav";
    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent";  

    public static void main(String[] args) {
        try {
           
            System.out.print("Enter your email to register: ");
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            String email = reader.readLine();

            
            System.out.println("Logged in successfully! Welcome, " + email + "! How can I assist you today?");

            
            while (true) {
                System.out.print("You: ");
                String userInput = reader.readLine();

                if (userInput.equalsIgnoreCase("exit")) {
                    System.out.println("Goodbye!");
                    break;
                }

                
                String response = getGeminiResponse(userInput);
                System.out.println("Chatbot: " + response);

                
                System.out.print("Would you like to learn something else? (yes/no): ");
                String continueResponse = reader.readLine();
                if (continueResponse.equalsIgnoreCase("no")) {
                    System.out.println("Goodbye!");
                    break;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getGeminiResponse(String userInput) throws Exception {
        
        String jsonPayload = "{\n" +
                "  \"contents\": [{\n" +
                "    \"parts\": [{\n" +
                "      \"text\": \"" + userInput + "\"\n" +
                "    }]\n" +
                "  }]\n" +
                "}";

        
        URL url = new URL(GEMINI_API_URL + "?key=" + GEMINI_API_KEY);  // Use your API key directly in the URL
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);

        
        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = jsonPayload.getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        
        int responseCode = conn.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(conn.getErrorStream(), "utf-8"));
            StringBuilder errorResponse = new StringBuilder();
            String errorLine;
            while ((errorLine = errorReader.readLine()) != null) {
                errorResponse.append(errorLine.trim());
            }
            System.out.println("Error: " + errorResponse.toString());
            return "Error occurred while processing the request.";
        }

       
        StringBuilder responseBuilder = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"))) {
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                responseBuilder.append(responseLine.trim());
            }
        }

        
        String responseString = responseBuilder.toString();

        
        String response = "";
        if (responseString.contains("\"text\":")) {
            int startIdx = responseString.indexOf("\"text\":") + 8;  
            int endIdx = responseString.indexOf("\"}", startIdx);
            response = responseString.substring(startIdx, endIdx);
        }
        
        return response;
    }
}