```<|start_of_file|>
<|editable_region_start|>
 import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class Chat {

    private static final String API_URL = "https://api.openai.com/v1/chat/completions";
    private static final String API_KEY = "sk-8ESJIGbXtwSO8eylYbkPS67Rq9APTF3omfav"<|user_cursor_is_here|>

    public static void main(String[] args) {
        if (args.length > 0) {
            String query = String.join(" ", args);
            String response = getChatResponse(query);
            System.out.println(response);
        } else {
            System.out.println("No input received.");
        }
    }

    static String getChatResponse(String query) {
        try {
            @SuppressWarnings("deprecation")
            URL url = new URL(API_URL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            // Set up request
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Authorization", "Bearer " + API_KEY);
            connection.setDoOutput(true);

            // Build JSON body
            String jsonInputString = String.format("""
                {
                    "model": "gpt-3.5-turbo",
                    "messages": [{"role": "user", "content": "%s"}],
                    "max_tokens": 150,
                    "temperature": 0.7
                }
            """, query);

            // Send request
            try (var outputStream = connection.getOutputStream()) {
                byte[] input = jsonInputString.getBytes("utf-8");
                outputStream.write(input);
            }

            // Get response
            try (var inputStream = connection.getInputStream();
                 var reader = new BufferedReader(new InputStreamReader(inputStream))) {

                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }

                // Parse JSON response
                return parseApiResponse(response.toString());
            }
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            return "Sorry, I couldn't process your request.";
        }
    }

    private static String parseApiResponse(String json) {
        // Extract 'content' from the API response
        String content = "";
        try {
            String trimmedJson = json.substring(json.indexOf("{"), json.lastIndexOf("}") + 1);
            java.util.regex.Pattern p = java.util.regex.Pattern.compile("\"content\"\\s*:\\s*\"([^\"]*)\"");
            java.util.regex.Matcher m = p.matcher(trimmedJson);
            if (m.find()) {
                content = m.group(1).replace("\\n", "\n");
            }
        } catch (Exception ignored) {}
        return content.isEmpty() ? "Hmm, I'm not sure." : content;
    }
}<|editable_region_end|>
```