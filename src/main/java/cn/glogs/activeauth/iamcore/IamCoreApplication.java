package cn.glogs.activeauth.iamcore;

import cn.glogs.activeauth.iamcore.config.properties.AuthConfiguration;
import cn.glogs.activeauth.iamcore.config.properties.LocatorConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({AuthConfiguration.class, LocatorConfiguration.class})
public class IamCoreApplication {

    public static void main(String[] args) {
        SpringApplication.run(IamCoreApplication.class, args);
    }

}
