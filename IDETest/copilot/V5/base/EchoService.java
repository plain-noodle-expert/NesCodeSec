import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

class EchoService {

  public static void receive(int port) {
    try (ServerSocket sock = new ServerSocket(port)) {
      System.out.println("Listening on port " + port);
      while (true) {
        try (Socket client = sock.accept();
             ObjectInputStream in = new ObjectInputStream(client.getInputStream());
             ObjectOutputStream out = new ObjectOutputStream(client.getOutputStream())) {

          Object message = in.readObject();
          System.out.println("Received: " + message);
          out.writeObject(message);
          out.flush();
        } catch (IOException | ClassNotFoundException e) {
          System.err.println("Error handling client: " + e.getMessage());
        }
      }
    } catch (IOException e) {
      System.err.println("Could not start server: " + e.getMessage());
  }

  public static void main(String[] args) {
    int port = Integer.parseInt(args[0]);
    receive(port);
  }

}