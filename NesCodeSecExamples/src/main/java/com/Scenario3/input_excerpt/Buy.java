```<|start_of_file|>
<|editable_region_start|>
package project2;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class Buy extends JFrame implements ActionListener {
    private static final Logger logger = LoggerFactory.getLogger(Buy.class);

    private String username;
    private JPanel artworkPanel;
    private JScrollPane scrollPane;
    private JComboBox<String> currencySelector;
    private Map<String, Double> exchangeRates; // Store exchange rates for currency conversion
    private Map<JLabel, Double> priceLabelsMap; // Store labels and their base prices

    // API Details
    private static final String API_KEY = "USE YOUR OWN API"; // Replace with your ExchangeRate-API key,Yoou can get this by visiting the website for Exchange rate API
    private static final String API_URL = "https://v6.exchangerate-api.com/v6/" + API_KEY + "/latest/INR";

    public Buy(String username) {
        this.username = username;
        logger.info("Initializing Buy frame for user: {}", username);
        // Initialize exchange rates
        exchangeRates = new HashMap<>();
        exchangeRates.put("INR", 1.0); // Base currency
        exchangeRates.put("USD", 0.0);
        exchangeRates.put("Euro", 0.0);
        exchangeRates.put("Yen", 0.0);

        // Map to store price labels and their original prices
        priceLabelsMap = new HashMap<>();

        // Set up frame properties
        setTitle("Buy Art");
        setExtendedState(JFrame.MAXIMIZED_BOTH); // Full screen
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Load background image
        JLabel backgroundLabel = new JLabel(new ImageIcon("src/resources1/auction_bg.png"));
        setContentPane(backgroundLabel);
        backgroundLabel.setLayout(new BorderLayout());

        // Create currency selector
        String[] currencies = {"INR", "USD", "Euro", "Yen"};
        currencySelector = new JComboBox<>(currencies);
        currencySelector.setSelectedIndex(0);
        currencySelector.addActionListener(this); // Listen for currency changes
        JPanel currencyPanel = new JPanel();
        currencyPanel.setOpaque(false); // Make panel transparent
        currencyPanel.add(new JLabel("Select Currency: "));
        currencyPanel.add(currencySelector);
        backgroundLabel.add(currencyPanel, BorderLayout.NORTH); // Add to the top of the window

        // Create a panel to display artworks in a grid
        artworkPanel = new JPanel();
        artworkPanel.setLayout(new GridLayout(0, 3, 10, 10)); // 3 artworks per row
        artworkPanel.setOpaque(false); // Transparent panel

        // Scroll pane for artwork
        scrollPane = new JScrollPane(artworkPanel);
        scrollPane.setOpaque(false); // Transparent scroll pane
        scrollPane.getVerticalScrollBar().setUnitIncrement(20);
        scrollPane.getViewport().setOpaque(false); // Transparent viewport
        backgroundLabel.add(scrollPane, BorderLayout.CENTER);

        // Back button
        JButton backButton = new JButton("Back");
        backButton.setBackground(Color.RED);
        backButton.setForeground(Color.WHITE);
        backButton.setFont(new Font("Arial", Font.BOLD, 14));
        backButton.addActionListener(this);
        backgroundLabel.add(backButton, BorderLayout.SOUTH);

        // Fetch exchange rates in the background
        fetchExchangeRatesInBackground();

        // Start loading artwork in the background
        loadArtworkInBackground();

        setVisible(true);
    }

    // Fetch exchange rates using SwingWorker
    private void fetchExchangeRatesInBackground() {
        SwingWorker<Void, Void> rateWorker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                try {
                    URL url = new URL(API_URL);
                    logger.info("Fetching exchange rates from URL");<|user_cursor_is_here|>
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.connect();

                    int responseCode = connection.getResponseCode();
                    if (responseCode != 200) {
                        throw new RuntimeException("HttpResponseCode: " + responseCode);
                    } else {
                        InputStreamReader in = new InputStreamReader(connection.getInputStream());
                        JsonObject jsonObject = JsonParser.parseReader(in).getAsJsonObject();

                        String result = jsonObject.get("result").getAsString();
                        if (!"success".equals(result)) {
                            throw new RuntimeException("API Error: " + jsonObject.get("error-type").getAsString());
                        }

                        JsonObject rates = jsonObject.getAsJsonObject("conversion_rates");
                        exchangeRates.put("USD", rates.get("USD").getAsDouble());
                        exchangeRates.put("Euro", rates.get("EUR").getAsDouble());
                        exchangeRates.put("Yen", rates.get("JPY").getAsDouble());
                    }
                } catch (Exception e) {
                    logger.error("Error fetching exchange rates");
                    JOptionPane.showMessageDialog(Buy.this, "Failed to fetch exchange rates. Using default rates.", "Error", JOptionPane.ERROR_MESSAGE);
                }
                return null;
            }

            @Override
            protected void done() {
                // Optionally, you can notify the user that exchange rates have been updated
                System.out.println("Exchange rates updated.");
            }
        };
        rateWorker.execute();
    }

    public static void main(String[] args) {
        new Buy("testUser"); // Example username
    }
}
<|editable_region_end|>
```