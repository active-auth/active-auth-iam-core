package cn.glogs.activeauth.iamcore;

import cn.glogs.activeauth.iamcore.config.properties.Configuration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({Configuration.class, Configuration.LordAuthConfiguration.class})
public class IamCoreApplication {

    public static void main(String[] args) {
        SpringApplication.run(IamCoreApplication.class, args);
    }

}
