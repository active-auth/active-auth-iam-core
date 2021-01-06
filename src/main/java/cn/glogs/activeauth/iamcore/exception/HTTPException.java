package cn.glogs.activeauth.iamcore.exception;

public class HTTPException extends Exception {
    public HTTPException(String message) {
        super(message);
    }

    public HTTPException(String message, Throwable cause) {
        super(message, cause);
    }
}
