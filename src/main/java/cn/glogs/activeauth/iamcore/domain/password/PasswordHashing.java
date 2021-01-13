package cn.glogs.activeauth.iamcore.domain.password;

public interface PasswordHashing {

    void setSalt(String salt);

    String hashing(CharSequence password);

    boolean check(CharSequence password, String hashed);
}
