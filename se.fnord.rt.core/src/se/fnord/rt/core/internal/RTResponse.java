package se.fnord.rt.core.internal;

final class RTResponse {
    private final int code;
    final String message;
    private final String body;
    private final String version;

    public RTResponse(int code, String message, String version, String body) {
        this.code = code;
        this.message = message;
        this.version = version;
        this.body = body;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public String getBody() {
        return body;
    }

    public String getVersion() {
        return version;
    }
}