package cn.glogs.activeauth.iamcore.service;

import cn.glogs.activeauth.iamcore.domain.AuthenticationPrincipal;
import cn.glogs.activeauth.iamcore.domain.AuthorizationPolicy;
import cn.glogs.activeauth.iamcore.domain.AuthorizationPolicyGrant;
import cn.glogs.activeauth.iamcore.exception.business.NotFoundException;
import cn.glogs.activeauth.iamcore.exception.business.PatternException;
import org.springframework.data.domain.Page;

import java.util.List;

public interface AuthorizationPolicyGrantService {
    List<AuthorizationPolicyGrant> addGrants(AuthenticationPrincipal granter, AuthenticationPrincipal grantee, List<AuthorizationPolicy> policies);

    AuthorizationPolicyGrant getGrantById(Long id);

    Page<AuthorizationPolicyGrant> pagingGrantsFrom(AuthenticationPrincipal granter, int page, int size);

    Page<AuthorizationPolicyGrant> pagingGrantsTo(AuthenticationPrincipal granter, int page, int size);

    void deleteGrant(Long grantId);
}
