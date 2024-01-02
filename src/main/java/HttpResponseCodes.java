public enum HttpResponseCodes {
    OK("200", "OK"),
    CREATED("201", "Created"),
    NOT_FOUND("404", "Not Found"),
    INTERNAL_SERVER_ERROR("500", "Internal Server Error");
    private final String statusCode, statusText;

    public String getStatusCode() {
        return statusCode;
    }

    public String getStatusText() {
        return statusText;
    }

    private HttpResponseCodes(String statusCode, String statusText) {
        this.statusCode = statusCode;
        this.statusText = statusText;
    }
}
