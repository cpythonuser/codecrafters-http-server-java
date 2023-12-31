import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class Main {
    public static final String PATH_SEPARATOR = "/";

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
            String statusLine;
            List<String> incomingRequest = new ArrayList<>();
            while ((statusLine = input.readLine()) != null && !statusLine.isEmpty()) {
                System.out.println(statusLine);
                incomingRequest.add(statusLine);
            }

            if (!incomingRequest.isEmpty() && (statusLine = incomingRequest.get(0)) != null) {
                try (PrintWriter output = new PrintWriter(clientSocket.getOutputStream())) {
                    String[] statusLineEntries = statusLine.split("\\s+");
                    String[] requestPath = statusLineEntries[1].split(PATH_SEPARATOR);
                    if (requestPath.length == 0) {
                        // Return 200 for root path
                        output.print("HTTP/1.1 200 OK\r\n\r\n");
                    } else if (requestPath.length >= 2 && Objects.equals(requestPath[1], "echo")) {
                        // Return substring after "echo" in url, e.g. GET /echo/abc/def should return abc/def with
                        // Content-Length: 7, while GET /echo/ or GET /echo should return an empty string with
                        // Content-Length: 0
                        String echoPath = String.join(PATH_SEPARATOR, Arrays.copyOfRange(requestPath, 2, requestPath.length));
                        String responseBody = "HTTP/1.1 200 OK\r\n" + "Content-Type: text/plain\r\n" + String.format("Content-Length: %d\r\n\r\n", echoPath.length()) + echoPath;
                        output.print(responseBody);
                    } else if (requestPath.length >= 2 && Objects.equals(requestPath[1], "user-agent") && incomingRequest.size() >= 2 && !incomingRequest.get(2).isEmpty()) {
                        String[] userAgentHeader = incomingRequest.get(2).split(":\\s+");
                        if (userAgentHeader.length > 1) {
                            String responseBody = "HTTP/1.1 200 OK\r\n" + "Content-Type: text/plain\r\n" + String.format("Content-Length: %d\r\n\r\n", userAgentHeader[1].length()) + userAgentHeader[1];
                            output.print(responseBody);
                        } else
                            throw new IOException(String.format("Malformed user agent header: %s", incomingRequest.get(2)));
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
