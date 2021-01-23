package cn.glogs.activeauth.iamcore.config.properties;

import com.gitee.starblues.integration.DefaultIntegrationConfiguration;
import lombok.ToString;
import org.pf4j.RuntimeMode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ToString
@ConfigurationProperties(prefix = "plugin")
public class PluginConfiguration extends DefaultIntegrationConfiguration {

    /**
     * Running mode
     * dev environment: development, dev
     * production / deployment environment: deployment, prod
     */
    @Value("${runMode:dev}")
    private String runMode;

    /**
     * Plugin path
     */
    @Value("${pluginPath:plugins}")
    private String pluginPath;

    /**
     * plugin config file path
     */
    @Value("${pluginConfigFilePath:pluginConfigs}")
    private String pluginConfigFilePath;


    @Override
    public RuntimeMode environment() {
        return RuntimeMode.byName(runMode);
    }

    @Override
    public String pluginPath() {
        return pluginPath;
    }

    @Override
    public String pluginConfigFilePath() {
        return pluginConfigFilePath;
    }

    /**
     * 重写上传插件包的临时存储路径。只适用于生产环境
     *
     * @return String
     */
    @Override
    public String uploadTempPath() {
        return "temp";
    }

    /**
     * 重写插件备份路径。只适用于生产环境
     *
     * @return String
     */
    @Override
    public String backupPath() {
        return "backupPlugin";
    }

    /**
     * 重写插件RestController请求的路径前缀
     *
     * @return String
     */
    @Override
    public String pluginRestControllerPathPrefix() {
        return "/api/plugins";
    }

    /**
     * 重写是否启用插件id作为RestController请求的路径前缀。
     * 启动则插件id会作为二级路径前缀。即: /api/plugins/pluginId/**
     *
     * @return String
     */
    @Override
    public boolean enablePluginIdRestControllerPathPrefix() {
        return true;
    }

    public String getRunMode() {
        return runMode;
    }

    public void setRunMode(String runMode) {
        this.runMode = runMode;
    }


    public String getPluginPath() {
        return pluginPath;
    }

    public void setPluginPath(String pluginPath) {
        this.pluginPath = pluginPath;
    }

    public String getPluginConfigFilePath() {
        return pluginConfigFilePath;
    }

    public void setPluginConfigFilePath(String pluginConfigFilePath) {
        this.pluginConfigFilePath = pluginConfigFilePath;
    }
}