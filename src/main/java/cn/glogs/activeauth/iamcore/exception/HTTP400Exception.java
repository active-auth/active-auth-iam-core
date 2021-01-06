package cn.glogs.activeauth.iamcore.exception;

public class HTTP400Exception extends HTTPException {
    public HTTP400Exception(Throwable cause) {
        super(cause.getMessage(), cause);
    }

    public HTTP400Exception(String message) {
        super(message);
    }
}
