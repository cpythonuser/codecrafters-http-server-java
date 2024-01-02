import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class ConnectionHandlerRunnable implements Runnable {
    private final Socket clientSocket;
    private final String fileDirectory;

    public ConnectionHandlerRunnable(Socket socket, String directory) {
        clientSocket = socket;
        fileDirectory = directory;
    }

    /**
     * Runs this operation.
     */
    @Override
    public void run() {
        try {
            System.out.printf("Accepted new connection: %s%n", Thread.currentThread().getName());
            BufferedReader input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            String statusLine;
            List<String> incomingRequest = new ArrayList<>();
            while ((statusLine = input.readLine()) != null && !statusLine.isEmpty()) {
                System.out.println(statusLine);
                incomingRequest.add(statusLine);
            }

            if (!incomingRequest.isEmpty()) {
                HttpRequest httpRequest = new HttpRequest(incomingRequest);
                // Check if request has Content-Length header and read the number of characters specified by the
                // Content-Length header into a character buffer. This is the body of the request.
                if (httpRequest.getMeta().get("Content-Length") != null) {
                    int bytesToRead = Integer.parseInt(httpRequest.getMeta().get("Content-Length"));
                    char[] requestBody = new char[bytesToRead];
                    input.read(requestBody, 0, bytesToRead);
                    httpRequest.setBody(String.valueOf(requestBody));
                    System.out.println(requestBody);
                }
                try (PrintWriter output = new PrintWriter(clientSocket.getOutputStream())) {
                    if (Objects.equals(httpRequest.getPath(), "/")) {
                        // Return 200 for root path
                        output.print("HTTP/1.1 200 OK\r\n\r\n");
                    } else if (httpRequest.getPath().startsWith("/echo/")) {
                        String echoPath = httpRequest.getPath().substring(6);
                        String responseBody = "HTTP/1.1 200 OK\r\n" + "Content-Type: text/plain\r\n" + String.format("Content-Length: %d\r\n\r\n", echoPath.length()) + echoPath;
                        output.print(responseBody);
                    } else if (httpRequest.getPath().startsWith("/user-agent")) {
                        String userAgentHeader = httpRequest.getMeta().get(AppConstants.USER_AGENT);
                        if (userAgentHeader != null) {
                            String responseBody = "HTTP/1.1 200 OK\r\n" + "Content-Type: text/plain\r\n" + String.format("Content-Length: %d\r\n\r\n", userAgentHeader.length()) + userAgentHeader;
                            output.print(responseBody);
                        } else
                            throw new IOException(String.format("Malformed user agent header: %s", incomingRequest.get(2)));
                    } else if (httpRequest.getPath().startsWith("/files/") && Objects.equals(httpRequest.getMethod(), HttpMethod.GET) && fileDirectory != null) {
                        Path filePath = Paths.get(fileDirectory, httpRequest.getPath().substring(7));
//                        byte[] fileBytes;
                        try {
//                            fileBytes = Files.readAllBytes(filePath);
                            String fileContent = Files.readString(filePath);
                            System.out.printf("Found file %s", filePath);
                            String responseBody = "HTTP/1.1 200 OK\r\n" + "Content-Type: application/octet-stream\r\n" + String.format("Content-Length: %d\r\n\r\n", fileContent.length()) + fileContent;
                            output.print(responseBody);
                        } catch (IOException e) {
                            output.print("HTTP/1.1 404 Not Found\r\n\r\n");
                        }
                    } else if (httpRequest.getPath().startsWith("/files/") && Objects.equals(httpRequest.getMethod(), HttpMethod.POST) && fileDirectory != null) {
                        Path filePath = Paths.get(fileDirectory, httpRequest.getPath().substring(7));
                        try {
                            Files.write(filePath, httpRequest.getBody().getBytes());
                            System.out.printf("Created file %s", filePath);
                            output.print("HTTP/1.1 201 Created\r\n\r\n");
                        } catch (IOException e) {
                            output.print("HTTP/1.1 500 Internal Server Error\r\n\r\n");
                        }
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
