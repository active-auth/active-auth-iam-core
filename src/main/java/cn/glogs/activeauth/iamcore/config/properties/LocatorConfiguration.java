package cn.glogs.activeauth.iamcore.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties("cn.glogs.active-auth.iam.locator")
public class LocatorConfiguration {
    private String prefix = "arn";
    private String partition = "cloudapp";
    private String service = "iam";

    public String fullLocator(String ownerId, String... resourcePaths) {
        String resourcePath = String.join("/", resourcePaths);
        return String.join(":", prefix, partition, service, "", ownerId, resourcePath);
    }

    public String fullPattern(String... resourcePaths) {
        String resourcePath = String.join("/", resourcePaths);
        return "^" + String.join(":", prefix, partition, service, "", "(\\d+)", resourcePath) + "$";
    }
}
