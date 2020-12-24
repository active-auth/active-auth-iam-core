package cn.glogs.activeauth.iamcore.config.properties;

import cn.glogs.activeauth.iamcore.domain.password.PasswordHashingStrategy;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties("cn.glogs.active-auth")
public class Configuration {
    private PasswordHashingStrategy passwordHashingStrategy = PasswordHashingStrategy.B_CRYPT;
    private int sessionExpiringSeconds = 600;
    private String tokenPrefix = "Bearer";
    private int tokenPrefixSpaceCount = 1;

    public String fullTokenPrefix() {
        return tokenPrefix + " ".repeat(tokenPrefixSpaceCount);
    }

    @Data
    @ConfigurationProperties("cn.glogs.active-auth.lord")
    public static class LordAuthConfiguration {
        private String authorizationHeaderName = "Authorization";
        private String authorizationHeaderValuePrefix = "Bearer";
        private int authorizationHeaderValuePrefixSpaceCount = 1;

        public String fullTokenPrefix() {
            return authorizationHeaderValuePrefix + " ".repeat(authorizationHeaderValuePrefixSpaceCount);
        }
    }
}
