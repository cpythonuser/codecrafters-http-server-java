import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
                        HttpResponse httpResponse = new HttpResponse.Builder().protocol("HTTP/1.1").responseCode(HttpResponseCodes.OK).build();
                        output.print(httpResponse);
                    } else if (httpRequest.getPath().startsWith("/echo/")) {
                        String echoPath = httpRequest.getPath().substring(6);
                        HttpResponse httpResponse = new HttpResponse.Builder().protocol("HTTP/1.1").responseCode(HttpResponseCodes.OK)
                                .headers(Map.of("Content-Type", "text/plain", "Content-Length", String.valueOf(echoPath.length())))
                                .body(echoPath)
                                .build();
//                        String responseBody = "HTTP/1.1 200 OK\r\n" + "Content-Type: text/plain\r\n" + String.format("Content-Length: %d\r\n\r\n", echoPath.length()) + echoPath;
                        output.print(httpResponse);
                    } else if (httpRequest.getPath().startsWith("/user-agent")) {
                        String userAgentHeader = httpRequest.getMeta().get(AppConstants.USER_AGENT);
                        if (userAgentHeader != null) {
                            HttpResponse httpResponse = new HttpResponse.Builder().protocol("HTTP/1.1").responseCode(HttpResponseCodes.OK)
                                    .headers(Map.of("Content-Type", "text/plain", "Content-Length", String.valueOf(userAgentHeader.length())))
                                    .body(userAgentHeader)
                                    .build();
//                            String responseBody = "HTTP/1.1 200 OK\r\n" + "Content-Type: text/plain\r\n" + String.format("Content-Length: %d\r\n\r\n", userAgentHeader.length()) + userAgentHeader;
                            output.print(httpResponse);
                        } else
                            throw new IOException(String.format("Malformed user agent header: %s", incomingRequest.get(2)));
                    } else if (httpRequest.getPath().startsWith("/files/") && Objects.equals(httpRequest.getMethod(), HttpMethod.GET) && fileDirectory != null) {
                        Path filePath = Paths.get(fileDirectory, httpRequest.getPath().substring(7));
//                        byte[] fileBytes;
                        try {
//                            fileBytes = Files.readAllBytes(filePath);
                            String fileContent = Files.readString(filePath);
                            System.out.printf("Found file %s", filePath);
                            HttpResponse httpResponse = new HttpResponse.Builder().protocol("HTTP/1.1").responseCode(HttpResponseCodes.OK)
                                    .headers(Map.of("Content-Type", "application/octet-stream", "Content-Length", String.valueOf(fileContent.length())))
                                    .body(fileContent)
                                    .build();
//                            String responseBody = "HTTP/1.1 200 OK\r\n" + "Content-Type: application/octet-stream\r\n" + String.format("Content-Length: %d\r\n\r\n", fileContent.length()) + fileContent;
                            output.print(httpResponse);
                        } catch (IOException e) {
                            output.print(new HttpResponse.Builder().protocol("HTTP/1.1").responseCode(HttpResponseCodes.NOT_FOUND).build());
//                            output.print("HTTP/1.1 404 Not Found\r\n\r\n");
                        }
                    } else if (httpRequest.getPath().startsWith("/files/") && Objects.equals(httpRequest.getMethod(), HttpMethod.POST) && fileDirectory != null) {
                        Path filePath = Paths.get(fileDirectory, httpRequest.getPath().substring(7));
                        try {
                            Files.write(filePath, httpRequest.getBody().getBytes());
                            System.out.printf("Created file %s", filePath);
                            output.print(new HttpResponse.Builder().protocol("HTTP/1.1").responseCode(HttpResponseCodes.CREATED).build());
                        } catch (IOException e) {
                            output.print(new HttpResponse.Builder().protocol("HTTP/1.1").responseCode(HttpResponseCodes.INTERNAL_SERVER_ERROR).build());
                        }
                    } else {
                        output.print(new HttpResponse.Builder().protocol("HTTP/1.1").responseCode(HttpResponseCodes.NOT_FOUND).build());
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
