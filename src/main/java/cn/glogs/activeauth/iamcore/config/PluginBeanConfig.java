package cn.glogs.activeauth.iamcore.config;

import com.gitee.starblues.integration.AutoIntegrationConfiguration;
import com.gitee.starblues.integration.application.AutoPluginApplication;
import com.gitee.starblues.integration.application.PluginApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(AutoIntegrationConfiguration.class)
public class PluginBeanConfig {

    @Bean
    public PluginApplication pluginApplication() {
        // 实例化自动初始化插件的PluginApplication
        return new AutoPluginApplication();
    }

}
