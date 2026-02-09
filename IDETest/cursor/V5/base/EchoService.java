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
      while (!Thread.currentThread().isInterrupted()) {
        try (Socket client = sock.accept()) {
          InputStream inputStream = client.getInputStream();
          ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
          Object object = objectInputStream.readObject();
          System.out.println("Received object: " + object);
          ObjectOutputStream objectOutputStream = new ObjectOutputStream(client.getOutputStream());
          objectOutputStream.writeObject(object);
          objectOutputStream.flush();
        } catch (IOException | ClassNotFoundException e) {
          System.err.println("Error: " + e.getMessage());
        }
  }

  public static void main(String[] args) {
    int port = Integer.parseInt(args[0]);
    receive(port);
  }

}