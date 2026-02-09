import ProcessMaker_Client.ApiClient;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.*;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class ProcessMaker {

    public static ApiClient api() {
        String baseApiUrl = System.getenv("API_HOST");
        String v3Token = "sk-8ESJIGbXtwSO8eylYbkPS67Rq9APTF3om";
        String verifySsl = System.getenv("API_SSL_VERIFY");
        int connectTimeout = 60000;
        int readTimeout = 120000;

        ApiClient client = new ApiClient();

        client.setBasePath(baseApiUrl);
        if (v3Token != null && !v3Token.isEmpty()) {
            client.setBearerToken(v3Token);
        }
        client.setConnectTimeout(connectTimeout);
        client.setDebugging(false);

        if (verifySsl.equals("0")) {
            client.setVerifyingSsl(false);
        }

        return client;
    }

    public static Map<String, Object> data() throws FileNotFoundException {
        Type typeOfHashMap = new TypeToken<Map<String, Object>>() {}.getType();
        BufferedReader bufferedReader = new BufferedReader(
            new FileReader("data.json")
        );
        Gson gson = new Gson();
        return gson.fromJson(bufferedReader, typeOfHashMap);
    }

    public static Map<String, Object> config() throws FileNotFoundException {
        Type typeOfHashMap = new TypeToken<Map<String, Object>>() {}.getType();
        BufferedReader bufferedReader = new BufferedReader(
            new FileReader("config.json")
        );
        Gson gson = new Gson();
        return gson.fromJson(bufferedReader, typeOfHashMap);
    }

    public static void printOutput(Map<String, Object> data) {
        String jsonString = new Gson().toJson(data);

        // System.out.println(jsonString);
        try {
            Writer fileWriter = new FileWriter("output.json", false);
            fileWriter.write(jsonString);
            fileWriter.close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
