package cn.glogs.activeauth.iamcore.exception;

public class HTTP403Exception extends HTTPException {
    public HTTP403Exception(Throwable cause) {
        super(cause.getMessage(), cause);
    }

    public HTTP403Exception(String msg) {
        super(msg);
    }
}
