package cn.glogs.activeauth.iamcore.service;

import cn.glogs.activeauth.iamcore.domain.AuthenticationPrincipal;
import cn.glogs.activeauth.iamcore.domain.AuthorizationPolicy;
import cn.glogs.activeauth.iamcore.domain.AuthorizationPolicyGrant;
import cn.glogs.activeauth.iamcore.exception.business.NotFoundException;
import cn.glogs.activeauth.iamcore.exception.business.PatternException;

import java.util.List;

public interface AuthorizationService {
    AuthorizationPolicy addPolicy(AuthenticationPrincipal owner, AuthorizationPolicy.Form form);

    AuthorizationPolicy getPolicyByLocator(String locator) throws PatternException, NotFoundException;

    List<AuthorizationPolicyGrant> grantPolicies(AuthenticationPrincipal granter, AuthenticationPrincipal grantee, List<AuthorizationPolicy> policies);

    boolean challenge(AuthenticationPrincipal challenger, String action, List<String> resources);
}
