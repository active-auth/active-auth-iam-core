package cn.glogs.activeauth.iamcore.domain.sign;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.tomitribe.auth.signatures.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Getter
@AllArgsConstructor
public class HTTPSignatureRsaSha256Signer {

    public static Signature signRequest(String keyId, Map<String, String> toSignedHeaders, String secretKey) throws IOException, InvalidKeySpecException {
        return signRequest(keyId, "ANY", "/any/request/fake-uri", toSignedHeaders, secretKey, false);
    }

    public static Signature signRequest(String keyId, String method, String uri, Map<String, String> toSignedHeaders, String secretKey, boolean withRequestTarget) throws IOException, InvalidKeySpecException {
        List<String> toSignedHeaderNames = new ArrayList<>(toSignedHeaders.keySet());
        if (withRequestTarget) {
            toSignedHeaderNames.add("(request-target)"); // Adding "(request-target)" to signing Method-URI
        }
        Signature signature = new Signature(keyId, Algorithm.RSA_SHA256, null, toSignedHeaderNames);
        InputStream privateKeyInputStream = new ByteArrayInputStream(secretKey.getBytes(StandardCharsets.UTF_8));
        Key key = PEM.readPrivateKey(privateKeyInputStream);
        Signer signer = new Signer(key, signature);
        return signer.sign(method, uri, toSignedHeaders);
    }
}
