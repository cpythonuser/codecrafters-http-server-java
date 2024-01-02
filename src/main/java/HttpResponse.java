import java.util.HashMap;
import java.util.Map;

public class HttpResponse {
    private final String protocol;

    private final HttpResponseCodes responseCode;

    private final Map<String, String> headers = new HashMap<>();

    private final String body;

    private HttpResponse(Builder builder) {
        this.protocol = builder.protocol;
        this.responseCode = builder.responseCode;
        this.headers.putAll(builder.headers);
        this.body = builder.body;
    }

    public String getProtocol() {
        return protocol;
    }

    public HttpResponseCodes getResponseCode() {
        return responseCode;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public String getBody() {
        return body;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer();
        sb.append(protocol).append(" ");
        sb.append(responseCode.getStatusCode()).append(" ");
        sb.append(responseCode.getStatusText()).append(!headers.isEmpty() ? "\r\n": "\r\n\r\n");
        headers.forEach((k, v) -> sb.append(String.format("%s: %s\r\n", k, v)));
        if (!headers.isEmpty()) {
            sb.append("\r\n");
        }
        if (body != null) {
            sb.append(body);
        }
        return sb.toString();
    }

    public static class Builder {
        private String protocol;

        private HttpResponseCodes responseCode;

        private Map<String, String> headers = new HashMap<>();

        private String body;

        public Builder protocol(String protocol) {
            this.protocol = protocol;
            return this;
        }

        public Builder responseCode(HttpResponseCodes httpResponseCode) {
            this.responseCode = httpResponseCode;
            return this;
        }

        public Builder headers(Map<String, String> headers) {
            this.headers = headers;
            return this;
        }

        public Builder body(String body) {
            this.body = body;
            return this;
        }

        public HttpResponse build() {
            return new HttpResponse(this);
        }
    }
}
