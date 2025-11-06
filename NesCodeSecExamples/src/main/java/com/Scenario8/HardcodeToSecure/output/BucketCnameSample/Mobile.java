<|editable_region_start|>
 import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.DataOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Random;
import java.util.Scanner;

public class Mobile {
    private static final String API_KEY = "633b6c79";
    private static final String API_SECRET = "sk-8ESJIGbXtwSO8eylYbkPS67Rq9APTF3omfav";
    private static final String FROM = "Vernam";

    public static void sendSMS(String apiKey, String apiSecret, String from, String to, String text) throws Exception {
        URL url = new URL("https://rest.nexmo.com/sms/json");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        conn.setDoOutput(true);

        String data = "api_key=" + URLEncoder.encode(apiKey, "UTF-8") +
                "&api_secret=" + URLEncoder.encode(apiSecret, "UTF-8") +
                "&from=" + URLEncoder.encode(from, "UTF-8") +
                "&to=" + URLEncoder.encode(to, "UTF-8") +
                "&text=" + URLEncoder.encode(text, "UTF-8");

        try (DataOutputStream wr = new DataOutputStream(conn.getOutputStream())) {
            wr.writeBytes(data);
        }

        int responseCode = conn.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            StringBuilder response = new StringBuilder();
            try (Scanner scanner = new Scanner(conn.getInputStream())) {
                while (scanner.hasNextLine()) {
                    response.append(scanner.nextLine());
                }
            }
            System.out.println("Response: " + response.toString());
        } else {
            System.out.println("Failed to send SMS. Response Code: " + responseCode);
        }

        conn.disconnect();
    }

    public static boolean isValidPhone(String phoneNumber) {
        return phoneNumber.matches("^\\+?[0-9]{10,12}$");
    }

    public String sendMessage(String phone) {
        String code = generateRandomCode();
        String message = "Your authentication code is: " + code;
        try {
            sendSMS(API_KEY, API_SECRET, FROM, phone, message);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return code;
    }

    public static boolean phoneAuthenticationScene(String receivedPhone) {
        Mobile mobile = new Mobile();
        Stage popupStage = new Stage();
        popupStage.initModality(Modality.APPLICATION_MODAL); // Make it modal
        popupStage.setTitle("Authentication Request");

        // Create UI components
        Label instructionLabel = new Label("Provide the authentication code sent to your email:");
        TextField codeField = new TextField();
        codeField.setPromptText("Authentication code");
        Button submitButton = new Button("Submit");
        submitButton.setStyle("-fx-background-color: #bcdfe5; -fx-text-fill: #454648; -fx-font-size: 16;");
        submitButton.setOnMouseEntered(e -> submitButton.setStyle("-fx-background-color: #a3c7d3; -fx-text-fill: #454648; -fx-font-size: 16;"));
        submitButton.setOnMouseExited(e -> submitButton.setStyle("-fx-background-color: #bcdfe5; -fx-text-fill: #454648; -fx-font-size: 16;"));

        final boolean[] isAuthenticated = {false};

        String code = mobile.sendMessage(receivedPhone);
        // Handle button click
        submitButton.setOnAction(event -> {
            String enteredCode = codeField.getText();
            if (enteredCode.equals(code)) {
                isAuthenticated[0] = true;
                popupStage.close();
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("Authentication failed");
                alert.setContentText("The code you entered is incorrect.");
                alert.showAndWait();
            }
        });

        // Layout for the popup
        VBox popupLayout = new VBox(10, instructionLabel, codeField, submitButton);
        popupLayout.setStyle("-fx-padding: 20; -fx-alignment: center;");

        // Scene and Stage setup
        Scene popupScene = new Scene(popupLayout, 300, 150);
        popupStage.setScene(popupScene);
        popupStage.showAndWait();

        return isAuthenticated[0];
    }

    public String generateRandomCode() {
        Random random = new Random();
        int code = 1000 + random.nextInt(9000);
        return String.valueOf(code);
    }
}
<|editable_region_end|>
```
