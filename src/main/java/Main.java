import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
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

            if (statusLine != null) {
                try (PrintWriter output = new PrintWriter(clientSocket.getOutputStream())) {
                    String[] statusLineEntries = statusLine.split("\\s+");
                    String[] requestPath;
                    if (statusLineEntries.length > 1 && (requestPath = statusLineEntries[1].split("/")).length > 2 && Objects.equals(requestPath[1], "echo")) {
                        String echoPath = String.join("/", Arrays.copyOfRange(requestPath, 2, requestPath.length));
                        output.print("HTTP/1.1 200 OK\r\n\r\n");
                        output.print("Content-Type: text/plain\r\n");
                        output.print(String.format("Content-Length: %d\r\n", echoPath.length()));
                        output.print("\r\n");
                        output.print(String.format("%s\r\n", echoPath));
                    } else {
                        output.print("HTTP/1.1 404 Not Found\r\n\r\n");
                    }
                    output.flush();
                }
            }
            clientSocket.close();
        } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
        }
    }
}
