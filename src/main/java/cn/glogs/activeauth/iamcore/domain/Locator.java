package cn.glogs.activeauth.iamcore.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Locator {
    private String prefix;
    private String partition;
    private String service;
    private String region;
    private String accountId;
    private String resourcePath;

    public String toString() {
        return String.join(":", prefix, partition, service, region, accountId, resourcePath);
    }
}
