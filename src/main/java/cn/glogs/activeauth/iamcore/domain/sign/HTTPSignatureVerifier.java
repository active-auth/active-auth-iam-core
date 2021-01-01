package cn.glogs.activeauth.iamcore.domain.sign;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.tomitribe.auth.signatures.PEM;
import org.tomitribe.auth.signatures.Signature;
import org.tomitribe.auth.signatures.Verifier;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.util.Map;

@Data
@AllArgsConstructor
public class HTTPSignatureVerifier {

    private final Signature signature;

    public HTTPSignatureVerifier(String signature) {
        this(Signature.fromString(signature));
    }

    public boolean verifyAnyRequest(Map<String, String> toVerifiedHeaders, String publicKey) throws InvalidKeySpecException, IOException, SignatureException, NoSuchAlgorithmException {
        return verify("ANY", "/any/resources/fake-uri/foo/bar", toVerifiedHeaders, publicKey);
    }

    public boolean verify(String method, String uri, Map<String, String> toVerifiedHeaders, String publicKey) throws InvalidKeySpecException, IOException, SignatureException, NoSuchAlgorithmException {
        InputStream publicKeyInputStream = new ByteArrayInputStream(publicKey.getBytes(StandardCharsets.UTF_8));
        Key key = PEM.readPublicKey(publicKeyInputStream);
        Verifier verifier = new Verifier(key, signature);
        return verifier.verify(method, uri, toVerifiedHeaders);
    }
}
