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
public class HttpRsaSignature implements HttpSignature {

    private final Signature signature;

    public HttpRsaSignature(String signature) {
        this(Signature.fromString(signature));
    }

    public HttpRsaSignature(String keyId, Map<String, String> toSignedHeaders, String secretKey) throws IOException, InvalidKeySpecException {
        this(keyId, "ANY", "/any/request/fake-uri", toSignedHeaders, secretKey, false);
    }

    public HttpRsaSignature(String keyId, String method, String uri, Map<String, String> toSignedHeaders, String secretKey, boolean withRequestTarget) throws IOException, InvalidKeySpecException {
        List<String> toSignedHeaderNames = new ArrayList<>(toSignedHeaders.keySet());
        if (withRequestTarget) {
            toSignedHeaderNames.add("(request-target)"); // Adding "(request-target)" to signing Method-URI
        }
        Signature signature = new Signature(keyId, Algorithm.RSA_SHA256, null, toSignedHeaderNames);
        InputStream privateKeyInputStream = new ByteArrayInputStream(secretKey.getBytes(StandardCharsets.UTF_8));
        Key key = PEM.readPrivateKey(privateKeyInputStream);
        Signer signer = new Signer(key, signature);
        this.signature = signer.sign(method, uri, toSignedHeaders);
    }

    @Override
    public boolean verifyAnyRequest(Map<String, String> toVerifiedHeaders, String publicKey) throws InvalidKeySpecException, IOException, SignatureException, NoSuchAlgorithmException {
        return verify("ANY", "/any/resources/fake-uri/foo/bar", toVerifiedHeaders, publicKey);
    }

    @Override
    public boolean verify(String method, String uri, Map<String, String> toVerifiedHeaders, String publicKey) throws InvalidKeySpecException, IOException, SignatureException, NoSuchAlgorithmException {
        InputStream publicKeyInputStream = new ByteArrayInputStream(publicKey.getBytes(StandardCharsets.UTF_8));
        Key key = PEM.readPublicKey(publicKeyInputStream);
        Verifier verifier = new Verifier(key, signature);
        return verifier.verify(method, uri, toVerifiedHeaders);
    }
}
