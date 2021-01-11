package cn.glogs.activeauth.iamcore.config.properties;

import cn.glogs.activeauth.iamcore.domain.password.PasswordHashingStrategy;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties("cn.glogs.active-auth.iam")
public class AuthConfiguration {
    private PasswordHashingStrategy passwordHashingStrategy = PasswordHashingStrategy.B_CRYPT;
    private int tokenExpiringSeconds = 6000;
    private int signatureExpiringSeconds = 90;
    private String timestampHeaderName = "X-Timestamp";
    private String authorizationHeaderName = "Authorization";
    private String authorizationHeaderTokenValuePrefix = "Bearer";
    private int authorizationHeaderTokenValuePrefixSpaceCount = 1;
    private String authorizationHeaderSignatureValuePrefix = "Signature";
    private int authorizationHeaderSignatureValuePrefixSpaceCount = 1;


    public String fullTokenPrefix() {
        return authorizationHeaderTokenValuePrefix + " ".repeat(authorizationHeaderTokenValuePrefixSpaceCount);
    }

    public String fullSignaturePrefix() {
        return authorizationHeaderSignatureValuePrefix + " ".repeat(authorizationHeaderSignatureValuePrefixSpaceCount);
    }
}
