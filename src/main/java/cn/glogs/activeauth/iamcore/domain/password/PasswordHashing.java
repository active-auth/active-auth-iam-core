package cn.glogs.activeauth.iamcore.domain.password;

public interface PasswordHashing {

    String hashing(String password);

    boolean check(String password, String hashed);
}
