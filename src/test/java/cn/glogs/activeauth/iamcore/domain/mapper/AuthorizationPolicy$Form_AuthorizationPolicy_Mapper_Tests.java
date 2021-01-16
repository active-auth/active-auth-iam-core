package cn.glogs.activeauth.iamcore.domain.mapper;

import cn.glogs.activeauth.iamcore.domain.AuthorizationPolicy;
import org.dozer.DozerBeanMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static cn.glogs.activeauth.iamcore.domain.AuthorizationPolicy.PolicyEffect.ALLOW;

@SpringBootTest
public class AuthorizationPolicy$Form_AuthorizationPolicy_Mapper_Tests {

    private final AuthorizationPolicy$Form_AuthorizationPolicy_Mapper mapper = Mappers.getMapper(AuthorizationPolicy$Form_AuthorizationPolicy_Mapper.class);

    @Autowired
    private DozerBeanMapper dozerBeanMapper;

    @Test
    void dozerMappingTest() {
        AuthorizationPolicy.Form form = new AuthorizationPolicy.Form(
                "test policy", ALLOW,
                List.of("sample:Action1", "sample:Action2"),
                List.of("arn:cloudapp:bookshelf::31:bought-book/*", "arn:cloudapp:bookshelf::31:shoppping-cart/*")
        );
        AuthorizationPolicy policy = dozerBeanMapper.map(form, AuthorizationPolicy.class);
        Assertions.assertEquals(form.getName(), policy.getName());
        Assertions.assertEquals(form.getEffect(), policy.getEffect());
        Assertions.assertArrayEquals(form.getActions().toArray(), policy.getActions().toArray());
        Assertions.assertArrayEquals(form.getResources().toArray(), policy.getResources().toArray());
    }

    @Test
    void mapstructMappingTest() {
        AuthorizationPolicy.Form form = new AuthorizationPolicy.Form(
                "test policy", ALLOW,
                List.of("sample:Action1", "sample:Action2"),
                List.of("arn:cloudapp:bookshelf::31:bought-book/*", "arn:cloudapp:bookshelf::31:shoppping-cart/*")
        );
        AuthorizationPolicy policy = mapper.sourceToDestination(form);
        Assertions.assertEquals(form.getName(), policy.getName());
        Assertions.assertEquals(form.getEffect(), policy.getEffect());
        Assertions.assertArrayEquals(form.getActions().toArray(), policy.getActions().toArray());
        Assertions.assertArrayEquals(form.getResources().toArray(), policy.getResources().toArray());
    }
}
