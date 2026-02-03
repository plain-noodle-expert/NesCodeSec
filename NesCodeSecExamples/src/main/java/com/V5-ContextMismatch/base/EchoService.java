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
  }

  public static void main(String[] args) {
    int port = Integer.parseInt(args[0]);
    receive(port);
  }

}