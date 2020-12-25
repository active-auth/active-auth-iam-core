package cn.glogs.activeauth.iamcore.domain.sign;

import org.tomitribe.auth.signatures.Signature;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.util.Map;

public interface HttpSignature {
    Signature getSignature();

    boolean verifyAnyRequest(Map<String, String> toVerifiedHeaders, String secretKey) throws InvalidKeySpecException, IOException, SignatureException, NoSuchAlgorithmException;

    boolean verify(String method, String uri, Map<String, String> toVerifiedHeaders, String secretKey) throws InvalidKeySpecException, IOException, SignatureException, NoSuchAlgorithmException;
}
