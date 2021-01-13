package cn.glogs.activeauth.iamcore.domain.password;

import com.password4j.Password;

import java.util.HashMap;
import java.util.Map;

public enum PasswordHashingStrategy {
    S_CRYPT,
    B_CRYPT,
    PBKDF2;

    private static final Map<PasswordHashingStrategy, PasswordHashing> passwordHashingMap = new HashMap<>();

    static {
        passwordHashingMap.put(S_CRYPT, new PasswordHashing() {
            private String salt;

            @Override
            public void setSalt(String salt) {
                this.salt = salt;
            }

            @Override
            public String hashing(CharSequence password) {
                return Password.hash(password).addSalt(salt).withSCrypt().getResult();
            }

            @Override
            public boolean check(CharSequence password, String hashed) {
                return Password.check(password, hashed).addSalt(salt).withSCrypt();
            }
        });
        passwordHashingMap.put(B_CRYPT, new PasswordHashing() {

            private String salt;

            @Override
            public void setSalt(String salt) {
                this.salt = Password.hash(salt).withBCrypt().getResult();
            }

            @Override
            public String hashing(CharSequence password) {
                return Password.hash(password).addSalt(salt).withBCrypt().getResult();
            }

            @Override
            public boolean check(CharSequence password, String hashed) {
                return Password.check(password, hashed).addSalt(salt).withBCrypt();
            }
        });
        passwordHashingMap.put(PBKDF2, new PasswordHashing() {

            private String salt;

            @Override
            public void setSalt(String salt) {
                this.salt = salt;
            }

            @Override
            public String hashing(CharSequence password) {
                return Password.hash(password).addSalt(salt).withPBKDF2().getResult();
            }

            @Override
            public boolean check(CharSequence password, String hashed) {
                return Password.check(password, hashed).addSalt(salt).withPBKDF2();
            }
        });
    }

    public PasswordHashing getHashing() {
        return passwordHashingMap.get(this);
    }

    public PasswordHashing getHashing(String salt) {
        PasswordHashing hashing = passwordHashingMap.get(this);
        hashing.setSalt(salt);
        return hashing;
    }

}
