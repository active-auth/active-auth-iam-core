package cn.glogs.activeauth.iamcore.domain.sign;

import cn.glogs.activeauth.iamcore.domain.keypair.RSAKeyPair;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.tomitribe.auth.signatures.Algorithm;
import org.tomitribe.auth.signatures.Signature;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

class RSAKeyPairTest {

    @Test
    void test() throws Exception {
        RSAKeyPair rsaKeyPair = RSAKeyPair.generateKeyPair();

        Map<String, String> headers = new HashMap<>();
        headers.put("X-Timestamp", String.valueOf(new Date().getTime() / 1000));

        Signature signature = HTTPSignatureSigner.signRequest(Algorithm.RSA_SHA3_256, "bookstore", headers, rsaKeyPair.getPriKey());
        Assertions.assertTrue(new HTTPSignatureVerifier(signature).verifyAnyRequest(headers, rsaKeyPair.getPubKey()));
    }
}
