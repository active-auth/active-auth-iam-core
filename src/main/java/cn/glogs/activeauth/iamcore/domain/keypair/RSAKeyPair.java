package cn.glogs.activeauth.iamcore.domain.keypair;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.security.Key;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RSAKeyPair implements KeyPair {

    private String pubKey;
    private String priKey;

    private static final Base64.Decoder decoder = Base64.getDecoder();
    private static final Base64.Encoder encoder = Base64.getEncoder();
    private static final int KEYSIZE = 2048;

    private static String decorating(byte[] keyBytes, String startLine, String endLine) {
        char[] publicKeyCharArr = new String(keyBytes).toCharArray();
        StringBuilder publicKeySb = new StringBuilder();
        publicKeySb.append(startLine).append("\n");
        for (int i = 0; i < publicKeyCharArr.length; i++) {
            publicKeySb.append(publicKeyCharArr[i]);
            if ((i + 1) % 64 == 0) {
                publicKeySb.append("\n");
            }
        }
        publicKeySb.append("\n").append(endLine);
        return publicKeySb.toString();
    }

    private static String publicKey(byte[] pubKeyBytes) {
        return decorating(pubKeyBytes, "-----BEGIN PUBLIC KEY-----", "-----END PUBLIC KEY-----");
    }

    private static String privateKey(byte[] priKeyBytes) {
        return decorating(priKeyBytes, "-----BEGIN PRIVATE KEY-----", "-----END PRIVATE KEY-----");
    }

    public static RSAKeyPair generateKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(KEYSIZE);
        java.security.KeyPair keyPair = keyPairGenerator.genKeyPair();
        Key pub = keyPair.getPublic();
        Key pri = keyPair.getPrivate();
        return new RSAKeyPair(publicKey(encoder.encode(pub.getEncoded())), privateKey(encoder.encode(pri.getEncoded())));
    }
}
