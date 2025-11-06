import com.google.gson.*;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.TilePane;
import javafx.stage.Stage;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class UnsplashGallery extends Application {

    private static final String ACCESS_KEY = 
    private static final String API_URL = "https://api.unsplash.com/photos/random?count=10&client_id=" + ACCESS_KEY;

    @Override
    public void start(Stage primaryStage) throws Exception {
        TilePane tilePane = new TilePane();
        ScrollPane scrollPane = new ScrollPane(tilePane);

        JsonArray photos = fetchImagesFromUnsplash();

        if (photos != null) {
            for (JsonElement photo : photos) {
                String imageUrl = photo.getAsJsonObject()
                        .getAsJsonObject("urls")
                        .get("small")
                        .getAsString();

                Image image = new Image(imageUrl, 200, 200, true, true);
                ImageView imageView = new ImageView(image);
                tilePane.getChildren().add(imageView);
            }
        }

        Scene scene = new Scene(scrollPane, 800, 600);
        primaryStage.setTitle("Unsplash Image Gallery");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private JsonArray fetchImagesFromUnsplash() {
        try {
            URL url = new URL(API_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            JsonParser parser = new JsonParser();
            JsonElement root = parser.parse(new InputStreamReader(conn.getInputStream()));
            return root.getAsJsonArray();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}