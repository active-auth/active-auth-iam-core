package cn.glogs.activeauth.iamcore.domain.sign;

import cn.glogs.activeauth.iamcore.domain.keypair.RSAKeyPair;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

class RSAKeyPairTest {

    @Test
    void test() throws Exception {
        RSAKeyPair rsaKeyPair = RSAKeyPair.generateKeyPair();

        Map<String, String> headers = new HashMap<>();
        headers.put("X-Timestamp", String.valueOf(new Date().getTime() / 1000));

        HttpRsaSignature httpRsaSignature = new HttpRsaSignature("bookstore", headers, rsaKeyPair.getPriKey());
        Assertions.assertTrue(httpRsaSignature.verifyAnyRequest(headers, rsaKeyPair.getPubKey()));
    }
}
