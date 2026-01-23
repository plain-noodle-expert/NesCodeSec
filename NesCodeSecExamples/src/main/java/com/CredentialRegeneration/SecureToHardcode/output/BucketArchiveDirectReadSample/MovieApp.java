<|editable_region_start|>
 import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import org.json.JSONArray;
import org.json.JSONObject;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class MovieApp {
    private static final String API_KEY = System.getenv("TMDB_API_KEY");
    private static final String API_ENDPOINT = "https://api.themoviedb.org/3/search/movie";
    private static final String DB_URL = "jdbc:mysql://localhost:3306/movies";
    private static final String USER = "root";
    private static final String PASSWORD = "root";
    public static User loggedInUser;
    

    public static void main(String[] args) {
        // Display the login page
        LoginPanel loginPanel = showLoginPage();
    
        // Check if the login was successful
        if (loginPanel.isAuthenticated()) {
            // If login is successful, create and show the main GUI
            loggedInUser = loginPanel.getLoggedInUser();
            MovieSearch.createAndShowGUI(loggedInUser);
        }
    }
    



    private static LoginPanel showLoginPage() {
    // Create an instance of the LoginPanel
    LoginPanel loginPanel = new LoginPanel();

    // Wait for the user to log in
    while (!loginPanel.isAuthenticated()) {
        try {
            Thread.sleep(1000); // Sleep for 1 second before checking again
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    // Return the LoginPanel instance
    return loginPanel;
}










    public static void showUserList() {
        try (Connection connection = DriverManager.getConnection(DB_URL, USER, PASSWORD)) {
            String query = "SELECT m.* FROM Movies m JOIN UserMovies um ON m.MovieID = um.MovieID WHERE um.UserID = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setInt(1, loggedInUser.getUserId());
    
                ResultSet resultSet = preparedStatement.executeQuery();
                displayUserList(resultSet);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }





    private static void displayUserList(ResultSet resultSet) throws SQLException {
        StringBuilder listMessage = new StringBuilder("Your Favorite Movies:\n");
    
        while (resultSet.next()) {
            String title = resultSet.getString("Title");
            String director = resultSet.getString("Director");
            String releaseDate = resultSet.getString("ReleaseDate");
    
            listMessage.append("Title: ").append(title)
                    .append(", Director: ").append(director)
                    .append(", Release Date: ").append(releaseDate)
                    .append("\n");
        }
    
        JOptionPane.showMessageDialog(null, listMessage.toString(), "Your Favorite Movies", JOptionPane.INFORMATION_MESSAGE);
    }
    



    

    public static void resetSearch() {
        // Assuming you have a method to reset the search bar and clear the movie list
        MovieSearch.resetSearch();
    }
    





    public static void fetchAndInsertData(String searchTerm) {
        try {
            // Load MySQL JDBC driver
            Class.forName("com.mysql.cj.jdbc.Driver");

            // Fetch data from the TMDb API with the provided search term
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_ENDPOINT + "?api_key=" + API_KEY + "&query=" + searchTerm))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                String responseBody = response.body();
                insertDataIntoDatabase(responseBody);
            } else {
                System.out.println("Error fetching data from TMDb API. Status code: " + response.statusCode());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }





    private static void insertDataIntoDatabase(String apiResponse) {
        try (Connection connection = DriverManager.getConnection(DB_URL, USER, PASSWORD)) {
            String insertRatingsQuery = "INSERT INTO Ratings (MovieID, UserID, Rating) VALUES (?, ?, ?)";

            try (PreparedStatement ratingsStatement = connection.prepareStatement(insertRatingsQuery)) {
                JSONArray moviesArray = new JSONObject(apiResponse).getJSONArray("results");

                for (int i = 0; i < moviesArray.length(); i++) {
                    JSONObject movieJson = moviesArray.getJSONObject(i);
                    Movie movie = parseTmdbApiResponse(movieJson);

                    ratingsStatement.setInt(1, movie.getId());
                    ratingsStatement.setInt(2, 1); // Assuming a single user for this example
                    ratingsStatement.setDouble(3, movie.getRating());
                    ratingsStatement.executeUpdate();
                }

                System.out.println("Ratings inserted into the database successfully.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }






    private static Movie parseTmdbApiResponse(JSONObject movieJson) {
        int id = movieJson.getInt("id");
        String title = movieJson.getString("title");
        double rating = movieJson.getDouble("vote_average");
        
        // Fetch director information
        String director = getDirector(id);
    
        // Assuming you have a method to get the release date as well
        String releaseDate = getReleaseDate(id);
    
        return new Movie(id, title, rating, director, releaseDate);
    }






// Add these methods to fetch the director and release date
private static String getDirector(int movieId) {
    try {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.themoviedb.org/3/movie/" + movieId + "/credits?api_key=" + API_KEY))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            String responseBody = response.body();
            System.out.println("Director API Response: " + responseBody); // Print the response to the console

            JSONObject creditsJson = new JSONObject(responseBody);
            JSONArray crewArray = creditsJson.getJSONArray("crew");

            for (int i = 0; i < crewArray.length(); i++) {
                JSONObject crewMember = crewArray.getJSONObject(i);
                if (crewMember.getString("job").equals("Director")) {
                    return crewMember.getString("name");
                }
            }
        } else {
            System.out.println("Error fetching director information. Status code: " + response.statusCode());
        }
    } catch (Exception e) {
        e.printStackTrace();
    }

    return "Director not available";
}







// Similar method for fetching the release date
private static String getReleaseDate(int movieId) {
    // Implement logic to fetch and return the release date
    return "Release date not available";
}

}<|editable_region_end|>
```
