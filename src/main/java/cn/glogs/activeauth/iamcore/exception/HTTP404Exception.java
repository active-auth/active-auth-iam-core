package cn.glogs.activeauth.iamcore.exception;

public class HTTP404Exception extends HTTPException {
    public HTTP404Exception(Throwable cause) {
        super(cause.getMessage(), cause);
    }

    public HTTP404Exception(String msg) {
        super(msg);
    }
}
