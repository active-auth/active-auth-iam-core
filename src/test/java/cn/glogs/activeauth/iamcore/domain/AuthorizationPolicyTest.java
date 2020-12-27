package cn.glogs.activeauth.iamcore.domain;

import cn.glogs.activeauth.iamcore.exception.business.PatternException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class AuthorizationPolicyTest {

    @Test
    void idFromLocator() throws PatternException {
        Long id = AuthorizationPolicy.idFromLocator("iam://users/111/policies/777");
        Assertions.assertEquals(id, 777);
    }

    @Test
    void ownerIdFromLocator() throws PatternException {
        Long ownerId = AuthorizationPolicy.ownerIdFromLocator("iam://users/111/policies/777");
        Assertions.assertEquals(ownerId, 111);
    }
}