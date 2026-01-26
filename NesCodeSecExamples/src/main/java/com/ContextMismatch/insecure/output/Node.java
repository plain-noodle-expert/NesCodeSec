<|editable_region_start|>
package tests;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.ECNamedCurveParameterSpec;

/**
 * Node: локальный HTTP-сервер,
 * простой PoW-блокчейн,
 * постоянный кошелек,
 * вознаграждение с халвингом, параметры в genesis.
 */
public class Node {
    private static final String REGISTRY_URL = "http://185.22.234.182:5000/nodes";
    private static final String CHAIN_FILE = "blockchain.gz";
    private static final String PRIV_KEY_FILE = "private.key";
    private static final String PUB_KEY_FILE = "public.key";
    private static final int DIFFICULTY = 4;
    // Параметры эмиссии
    private static final int INITIAL_REWARD = 50;
    private static final int HALVING_INTERVAL = 100;
    private static final String INITIAL_ADDRESS = "f9d2e5b82a47edd1954ff2a25c4db6db6eb81c9c";
    private static final int INITIAL_AMOUNT = 100000;

    private final String myAddress;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final List<String> peers = Collections.synchronizedList(new ArrayList<>());
    private final List<Block> blockchain = Collections.synchronizedList(new ArrayList<>());

    // Wallet
    private final PrivateKey privateKey;
    private final PublicKey publicKey;
    private final String walletAddress;

    static {
        // Register Bouncy Castle provider
        Security.addProvider(new BouncyCastleProvider());
    }

    public Node() throws Exception {
        httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

        KeyPair wallet = loadOrCreateWallet();
        privateKey = wallet.getPrivate();
        publicKey = wallet.getPublic();
        walletAddress = deriveAddress(publicKey);
        System.out.println("Wallet address: " + walletAddress);

        loadChain();

        String localIP = detectLocalIP();
        HttpServer server = HttpServer.create(new InetSocketAddress(localIP, 0), 0);
        int port = server.getAddress().getPort();
        myAddress = localIP + ":" + port;

        server.createContext("/add", new AddHandler());
        server.createContext("/read-file", new ReadFileHandler());
        server.setExecutor(Executors.newCachedThreadPool());
        server.start();
        System.out.println("Server at " + myAddress);

        startSync();
    }

    private void saveChain() {
        try (GZIPOutputStream gos = new GZIPOutputStream(new FileOutputStream(CHAIN_FILE));
             Writer osw = new OutputStreamWriter(gos, StandardCharsets.UTF_8);
             BufferedWriter bw = new BufferedWriter(osw)) {
            synchronized (blockchain) {
                objectMapper.writeValue(bw, blockchain);
            }
            System.out.println("Saved chain length: " + blockchain.size());
        } catch (IOException e) {
            System.err.println("Error saving chain: " + e.getMessage());
        }
    }


    private class AddHandler implements HttpHandler {
        public void handle(HttpExchange ex) throws IOException {
            if (!"POST".equalsIgnoreCase(ex.getRequestMethod())) {
                ex.sendResponseHeaders(405, -1);
                return;
            }
            try {
                JsonNode req = objectMapper.readTree(ex.getRequestBody());
                String userData = req.has("data") ? req.get("data").asText() : "";
                synchronized (blockchain) {
                    Block last = blockchain.get(blockchain.size() - 1);
                    // Read parameters from genesis
                    JsonNode cfg = objectMapper.readTree(blockchain.get(0).data);
                    int initR = cfg.get("initialReward").asInt();
                    int interval = cfg.get("halvingInterval").asInt();
                    int height = last.index + 1;
                    int halvings = height / interval;
                    int rewardAmt = initR >> halvings;

                    ObjectNode dataNode = objectMapper.createObjectNode();
                    dataNode.put("data", userData);
                    ObjectNode rewardNode = objectMapper.createObjectNode();
                    rewardNode.put("to", walletAddress);
                    rewardNode.put("amount", rewardAmt);
                    dataNode.set("reward", rewardNode);
                    String blockData = objectMapper.writeValueAsString(dataNode);

                    Block newBlock = new Block(height, Instant.now().toEpochMilli(), blockData, last.hash);
                    newBlock.mineBlock(DIFFICULTY);
                    blockchain.add(newBlock);
                    saveChain();
                    byte[] resp = objectMapper.writeValueAsBytes(newBlock);
                    ex.getResponseHeaders().add("Content-Type", "application/json");
                    ex.sendResponseHeaders(201, resp.length);
                    try (OutputStream os = ex.getResponseBody()) {
                        os.write(resp);
                    }
                }
            } catch (Exception e) {
                System.err.println("Error processing add request: " + e.getMessage());
                ex.sendResponseHeaders(400, -1);
            }
        }
    }

    private class ReadFileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }
            String fileParam = null;
            String rawQuery = exchange.getRequestURI().getRawQuery();
            if (rawQuery != null) {
                for (String part : rawQuery.split("&")) {
                    String[] kv = part.split("=", 2);
                    if (kv.length == 2 && kv[0].equals("path")) {
                        fileParam = java.net.URLDecoder.decode(kv[1], StandardCharsets.UTF_8);
                        break;
                    }
                }
            }
            if (fileParam == null || fileParam.isBlank()) {
                byte[] msg = "missing path parameter".getBytes(StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(400, msg.length);
                exchange.getResponseBody().write(msg);
                exchange.close();
                return;
            }

            // read file content
            Path filePath = Path.of(fileParam);
            if (!Files.exists(filePath)) {
                byte[] msg = "file not found".getBytes(StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(404, msg.length);
                exchange.getResponseBody().write(msg);
                exchange.close();
                return;
            }
            byte[] fileContent = Files.readAllBytes(filePath);
            exchange.sendResponseHeaders(200, fileContent.length);
            exchange.getResponseBody().write(fileContent);
            exchange.close();
        }
    }

}
<|editable_region_end|>
```
