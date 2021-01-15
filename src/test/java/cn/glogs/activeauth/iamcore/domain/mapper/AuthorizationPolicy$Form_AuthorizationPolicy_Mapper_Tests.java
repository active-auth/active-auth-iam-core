package cn.glogs.activeauth.iamcore.domain.mapper;

import cn.glogs.activeauth.iamcore.domain.AuthorizationPolicy;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.List;

import static cn.glogs.activeauth.iamcore.domain.AuthorizationPolicy.PolicyEffect.ALLOW;

public class AuthorizationPolicy$Form_AuthorizationPolicy_Mapper_Tests {

    private final AuthorizationPolicy$Form_AuthorizationPolicy_Mapper mapper = Mappers.getMapper(AuthorizationPolicy$Form_AuthorizationPolicy_Mapper.class);

    @Test
    void mappingTest() {
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
