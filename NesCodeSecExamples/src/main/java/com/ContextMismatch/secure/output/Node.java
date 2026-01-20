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

        // server.createContext("/nodes", new NodesHandler());
        // server.createContext("/chain", new ChainHandler());
        // server.createContext("/add", new AddHandler());
        server.setExecutor(Executors.newCachedThreadPool());
        server.start();
        System.out.println("Server at " + myAddress);

        // startSync();
    }

    private String deriveAddress(PublicKey pubKey) throws Exception {
        try {
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            byte[] hash = sha256.digest(pubKey.getEncoded());
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 20; i++) sb.append(String.format("%02x", hash[i]));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            System.err.println("Error deriving address: " + e.getMessage());
            throw e;
        }
    }
    // Load blockchain from compressed file
    private void loadChain() {
        File file = new File(CHAIN_FILE);
        if (file.exists()) {
            try (GZIPInputStream gis = new GZIPInputStream(new FileInputStream(file));
                 Reader isr = new InputStreamReader(gis, StandardCharsets.UTF_8);
                 BufferedReader br = new BufferedReader(isr)) {
                synchronized (blockchain) {
                    blockchain.addAll(objectMapper.readValue(br, new TypeReference<List<Block>>() {}));
                }
                System.out.println("Loaded chain length: " + blockchain.size());
            } catch (IOException e) {
                System.err.println("Error loading chain: " + e.getMessage());
            }
        }
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

}
<|editable_region_end|>
```
