package cn.glogs.activeauth.iamcore.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties("cn.glogs.active-auth.iam.mfa")
public class MfaConfiguration {
    private String verificationTokenIdHeader = "X-Verification-Token-ID";
    private String verificationTokenHeader = "X-Verification-Token";
}
