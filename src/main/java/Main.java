import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.Objects;

public class Main {
    public static void main(String[] args) {
        String directory = null;
        if (args.length > 1 && Objects.equals(args[0], "--directory")) {
            directory = args[1];
            System.out.printf("Initializing server with directory: %s\n", directory);
        }
        try (ServerSocket serverSocket = new ServerSocket(4221)) {
            serverSocket.setReuseAddress(true);
            System.out.println("Bootstrapped server successfully\n");
            while (true) {
                Socket clientSocket = serverSocket.accept(); // Wait for connection from client.
                (new Thread(new ConnectionHandlerRunnable(clientSocket, directory))).start();
            }
        } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
        }
    }
}
