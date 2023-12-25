import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Objects;

public class Main {
    public static void main(String[] args) {
        // You can use print statements as follows for debugging, they'll be visible when running tests.
        System.out.println("Logs from your program will appear here!");
        Socket clientSocket;
        ServerSocket serverSocket;

        try {
            serverSocket = new ServerSocket(4221);
            serverSocket.setReuseAddress(true);
            clientSocket = serverSocket.accept(); // Wait for connection from client.
            System.out.println("accepted new connection");
            BufferedReader input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            String statusLine = input.readLine(); // read only first (status) line for now
            System.out.println(statusLine);

            try (PrintWriter output = new PrintWriter(clientSocket.getOutputStream())) {
                String[] statusLineEntries;
                if ((statusLineEntries = statusLine.split("\\s+")).length > 1 && Objects.equals(statusLineEntries[1], "/")) {
                    output.print("HTTP/1.1 200 OK\r\n\r\n");
                } else {
                    output.print("HTTP/1.1 404 Not Found\r\n\r\n");
                }
                output.flush();
            }
            clientSocket.close();
        } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
        }
    }
}
