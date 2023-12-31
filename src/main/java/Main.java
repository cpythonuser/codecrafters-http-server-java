import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {
    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(4221)) {
            serverSocket.setReuseAddress(true);
            System.out.println("Bootstrapped server successfully");
            while (true) {
                Socket clientSocket = serverSocket.accept(); // Wait for connection from client.
                (new Thread(new ConnectionHandlerRunnable(clientSocket))).start();
            }
        } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
        }
    }
}
