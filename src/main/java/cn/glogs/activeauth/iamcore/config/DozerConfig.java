package cn.glogs.activeauth.iamcore.config;

import org.dozer.DozerBeanMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;
import java.util.List;

/**
 * Dozer mapping config
 * https://github.com/DozerMapper/dozer
 *
 * @author Okeyja Teung
 * @since 2021-01-14 21:08 +08:00
 */
@Configuration
public class DozerConfig {

    /**
     * Dozer config bean
     *
     * @return bean:DozerBeanMapper
     */
    @Bean(name = "org.dozer.Mapper")
    public DozerBeanMapper dozer() {
        List<String> mappingFiles = Collections.singletonList("dozer/AuthorizationPolicy-AuthorizationPolicy$Form.xml");
        DozerBeanMapper dozerBean = new DozerBeanMapper();
        dozerBean.setMappingFiles(mappingFiles);
        return dozerBean;
    }
}
