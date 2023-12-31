import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HttpRequest {
    private HttpMethod method;
    private String path;

    private final Map<String, String> meta = new HashMap<>();

    public HttpMethod getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public Map<String, String> getMeta() {
        return meta;
    }

    public HttpRequest(List<String> requestLines) {
        if (requestLines.isEmpty()) {
            throw new IllegalArgumentException("Attempted to parse empty HTTP request");
        }
        for (int i = 0; i < requestLines.size(); i++) {
            if (i == 0) {
                String[] statusLineEntries = requestLines.get(i).split(AppConstants.WHITESPACES_PATTERN);
                method = HttpMethod.valueOf(statusLineEntries[0]);
                path = statusLineEntries[1];
                meta.put(AppConstants.PROTOCOL, statusLineEntries[2]);
            } else {
                String[] metaEntries = requestLines.get(i).split(AppConstants.COLON_SPACES_PATTERN);
                if (metaEntries.length > 1) {
                    meta.put(metaEntries[0], metaEntries[1]);
                }
            }
        }
    }
}
