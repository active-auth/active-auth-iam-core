package cn.glogs.activeauth.iamcore.domain;

import cn.glogs.activeauth.iamcore.config.properties.LocatorConfiguration;

public interface IamResource {
    String resourceLocator(LocatorConfiguration locatorConfiguration);
}
