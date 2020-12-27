package cn.glogs.activeauth.iamcore.service;

import cn.glogs.activeauth.iamcore.domain.AuthenticationPrincipal;
import cn.glogs.activeauth.iamcore.domain.AuthorizationPolicy;
import cn.glogs.activeauth.iamcore.domain.AuthorizationPolicyGrant;
import cn.glogs.activeauth.iamcore.exception.business.NotFoundException;
import cn.glogs.activeauth.iamcore.exception.business.PatternException;
import org.springframework.data.domain.Page;

import java.util.List;

public interface AuthorizationPolicyService {
    AuthorizationPolicy addPolicy(AuthenticationPrincipal owner, AuthorizationPolicy.Form form);

    AuthorizationPolicy getPolicyById(Long id) throws NotFoundException;

    Page<AuthorizationPolicy> pagingPolicies(AuthenticationPrincipal owner, int page, int size);

    void deletePolicy(Long id);
}
