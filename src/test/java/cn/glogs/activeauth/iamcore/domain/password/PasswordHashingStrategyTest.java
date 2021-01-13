package cn.glogs.activeauth.iamcore.domain.password;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class PasswordHashingStrategyTest {

    private static final String toHashPassword = "Winston Churchill";
    private static final String toCheckPassword_right = "Winston Churchill";
    private static final String toCheckPassword_wrong = "Boris Johnson";

    @Test
    void testSCrypt() {
        String hashed = PasswordHashingStrategy.S_CRYPT.getHashing().hashing(toHashPassword);
        System.out.println("hashed = " + hashed);
        Assertions.assertTrue(PasswordHashingStrategy.S_CRYPT.getHashing().check(toCheckPassword_right, hashed));
        Assertions.assertFalse(PasswordHashingStrategy.S_CRYPT.getHashing().check(toCheckPassword_wrong, hashed));
    }

    @Test
    void testBCrypt() {
        String hashed = PasswordHashingStrategy.B_CRYPT.getHashing("somesalt").hashing(toHashPassword);
        System.out.println("hashed = " + hashed);
        Assertions.assertTrue(PasswordHashingStrategy.B_CRYPT.getHashing().check(toCheckPassword_right, hashed));
        Assertions.assertFalse(PasswordHashingStrategy.B_CRYPT.getHashing().check(toCheckPassword_wrong, hashed));
    }

    @Test
    void testPBKDF2() {
        String hashed = PasswordHashingStrategy.PBKDF2.getHashing().hashing(toHashPassword);
        System.out.println("hashed = " + hashed);
        Assertions.assertTrue(PasswordHashingStrategy.PBKDF2.getHashing().check(toCheckPassword_right, hashed));
        Assertions.assertFalse(PasswordHashingStrategy.PBKDF2.getHashing().check(toCheckPassword_wrong, hashed));
    }

}