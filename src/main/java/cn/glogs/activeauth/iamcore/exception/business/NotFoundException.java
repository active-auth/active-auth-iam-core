package cn.glogs.activeauth.iamcore.exception.business;

public class NotFoundException extends Exception {
    public NotFoundException(String msg) {
        super(msg);
    }

    public NotFoundException(String msg, Object... args) {
        super(String.format(msg, args));
    }
}