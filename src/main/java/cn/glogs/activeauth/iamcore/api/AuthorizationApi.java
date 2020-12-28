package cn.glogs.activeauth.iamcore.api;

import cn.glogs.activeauth.iamcore.api.helper.AuthCheckingHelper;
import cn.glogs.activeauth.iamcore.api.payload.*;
import cn.glogs.activeauth.iamcore.domain.AuthenticationPrincipal;
import cn.glogs.activeauth.iamcore.domain.AuthenticationSession;
import cn.glogs.activeauth.iamcore.domain.AuthorizationPolicy;
import cn.glogs.activeauth.iamcore.domain.AuthorizationPolicyGrant;
import cn.glogs.activeauth.iamcore.exception.HTTP400Exception;
import cn.glogs.activeauth.iamcore.exception.HTTP401Exception;
import cn.glogs.activeauth.iamcore.exception.HTTP403Exception;
import cn.glogs.activeauth.iamcore.exception.HTTP404Exception;
import cn.glogs.activeauth.iamcore.exception.business.NotFoundException;
import cn.glogs.activeauth.iamcore.exception.business.PatternException;
import cn.glogs.activeauth.iamcore.service.*;
import org.springframework.data.domain.Page;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

@RestController
public class AuthorizationApi {

    private final AuthenticationPrincipalService authenticationPrincipalService;
    private final AuthorizationService authorizationService;
    private final AuthorizationPolicyService authorizationPolicyService;
    private final AuthorizationPolicyGrantService authorizationPolicyGrantService;
    private final AuthCheckingHelper authCheckingHelper;

    public AuthorizationApi(AuthenticationPrincipalService authenticationPrincipalService, AuthorizationService authorizationService, AuthorizationPolicyService authorizationPolicyService, AuthorizationPolicyGrantService authorizationPolicyGrantService, AuthCheckingHelper authCheckingHelper) {
        this.authenticationPrincipalService = authenticationPrincipalService;
        this.authorizationService = authorizationService;
        this.authorizationPolicyService = authorizationPolicyService;
        this.authorizationPolicyGrantService = authorizationPolicyGrantService;
        this.authCheckingHelper = authCheckingHelper;
    }

    @PostMapping("/principals/current/policies")
    public RestResultPacker<AuthorizationPolicy.Vo> addPolicy(HttpServletRequest request, @RequestBody @Validated AuthorizationPolicy.Form form) throws HTTP400Exception, HTTP401Exception, HTTP403Exception {
        AuthCheckingContext authCheckingContext = authCheckingHelper.myResources(request, AuthCheckingStatement.checks("iam:AddPolicy", "iam://users/%s/policies"));
        AuthorizationPolicy authorizationPolicy = authorizationPolicyService.addPolicy(authCheckingContext.getResourceOwner(), form);
        return RestResultPacker.success(authorizationPolicy.vo());
    }

    @GetMapping("/principals/current/policies")
    public RestResultPacker<Page<AuthorizationPolicy.Vo>> pagingPolicies(HttpServletRequest request, @RequestParam int page, @RequestParam int size) throws HTTP400Exception, HTTP401Exception, HTTP403Exception {
        AuthCheckingContext authCheckingContext = authCheckingHelper.myResources(request, AuthCheckingStatement.checks("iam:GetPolicy", "iam://users/%s/policies"));
        return RestResultPacker.success(authorizationPolicyService.pagingPolicies(authCheckingContext.getResourceOwner(), page, size).map(AuthorizationPolicy::vo));
    }

    @DeleteMapping("/principals/current/policies/{policyId}")
    public RestResultPacker<AuthorizationPolicy.Vo> deletePolicy(HttpServletRequest request, @PathVariable Long policyId) throws HTTP400Exception, HTTP401Exception, HTTP403Exception, HTTP404Exception {
        authCheckingHelper.myResources(request, AuthCheckingStatement.checks("iam:GetPolicy", "iam://users/%s/policies/" + policyId).and("iam:DeletePolicy", "iam://users/%s/policies" + policyId));
        try {
            AuthorizationPolicy policy = authorizationPolicyService.getPolicyById(policyId);
            return RestResultPacker.success(policy.vo());
        } catch (NotFoundException e) {
            throw new HTTP404Exception(e);
        }
    }

    @PostMapping("/principals/current/grants")
    public RestResultPacker<List<AuthorizationPolicyGrant.Vo>> addGrant(HttpServletRequest request, @RequestBody @Validated AuthorizationPolicyGrantingForm form) throws HTTP400Exception, HTTP401Exception, HTTP403Exception, HTTP404Exception {
        try {
            List<AuthorizationPolicy> policies = new ArrayList<>();

            // Check if Current User can get those policies.
            AuthenticationSession session = null;
            for (String policyLocator : form.getPolicies()) {
                Long policyId = AuthorizationPolicy.idFromLocator(policyLocator);
                Long policyOwnerId = AuthorizationPolicy.ownerIdFromLocator(policyLocator);
                AuthorizationPolicy policy = authorizationPolicyService.getPolicyById(policyId);
                if (session == null) {
                    AuthCheckingContext authCheckingContext = authCheckingHelper.theirResources(request, AuthCheckingStatement.checks("iam:GetPolicy", "iam://users/%s/policies"), policyOwnerId);
                    session = authCheckingContext.getCurrentSession();
                } else {
                    authCheckingHelper.theirResources(session, AuthCheckingStatement.checks("iam:GetPolicy", "iam://users/%s/policies"), policyOwnerId);
                }
                policies.add(policy);
            }
            // Check if Current User can add grants.
            AuthCheckingContext authCheckingContext = authCheckingHelper.myResources(request, AuthCheckingStatement.checks("iam:AddGrant", "iam://users/%s/grants"));
            AuthenticationPrincipal granter = authCheckingContext.getCurrentSession().getAuthenticationPrincipal();
            AuthenticationPrincipal grantee = authenticationPrincipalService.findPrincipalById(AuthenticationPrincipal.idFromLocator(form.getGrantee()));
            List<AuthorizationPolicyGrant> policyGrants = authorizationPolicyGrantService.addGrants(granter, grantee, policies);
            List<AuthorizationPolicyGrant.Vo> results = new ArrayList<>();
            policyGrants.forEach(policyGrant -> results.add(policyGrant.vo()));
            return RestResultPacker.success(results);
        } catch (PatternException e) {
            throw new HTTP400Exception(e);
        } catch (NotFoundException e) {
            throw new HTTP404Exception(e);
        }
    }

    @GetMapping("/principals/current/grants")
    public RestResultPacker<Page<AuthorizationPolicyGrant.Vo>> pagingGrantsOut(HttpServletRequest request, @RequestParam int page, @RequestParam int size) throws HTTP400Exception, HTTP401Exception, HTTP403Exception {
        AuthCheckingContext authCheckingContext = authCheckingHelper.myResources(request, AuthCheckingStatement.checks("iam:GetGrant", "iam://users/%s/grants"));
        Page<AuthorizationPolicyGrant> grantsPage = authorizationPolicyGrantService.pagingGrantsFrom(authCheckingContext.getCurrentSession().getAuthenticationPrincipal(), page, size);
        return RestResultPacker.success(grantsPage.map(AuthorizationPolicyGrant::vo));
    }

    @DeleteMapping("/principals/current/grants/{grantId}")
    public RestResultPacker<AuthorizationPolicyGrant.Vo> deleteGrantsOut(HttpServletRequest request, @PathVariable Long grantId) throws HTTP400Exception, HTTP401Exception, HTTP403Exception, HTTP404Exception {
        authCheckingHelper.myResources(request, AuthCheckingStatement.checks("iam:GetGrant", "iam://users/%s/grants/" + grantId).and("iam:DeleteGrant", "iam://users/%s/grants/" + grantId));
        authorizationPolicyGrantService.deleteGrant(grantId);
        AuthorizationPolicyGrant grant = authorizationPolicyGrantService.getGrantById(grantId);
        return RestResultPacker.success(grant.vo());
    }

    @GetMapping("/principals/current/grants-in")
    public RestResultPacker<Page<AuthorizationPolicyGrant.Vo>> pagingGrantsIn(HttpServletRequest request, @RequestParam int page, @RequestParam int size) throws HTTP400Exception, HTTP401Exception, HTTP403Exception {
        AuthCheckingContext authCheckingContext = authCheckingHelper.myResources(request, AuthCheckingStatement.checks("iam:GetGrant", "iam://users/%s/grants-in"));
        Page<AuthorizationPolicyGrant> grantsPage = authorizationPolicyGrantService.pagingGrantsTo(authCheckingContext.getCurrentSession().getAuthenticationPrincipal(), page, size);
        return RestResultPacker.success(grantsPage.map(AuthorizationPolicyGrant::vo));
    }

    @PostMapping("/principals/current/authorization-challengings")
    public RestResultPacker<AuthorizationChallengeForm> authorizationChallenging(HttpServletRequest request, @RequestBody @Validated AuthorizationChallengeForm form) throws HTTP400Exception, HTTP401Exception, HTTP403Exception {
        AuthCheckingContext authCheckingContext = authCheckingHelper.myResources(request, AuthCheckingStatement.checks("iam:ChallengeAuth", "iam://users/%s/auth-challengings"));
        boolean accessible = authorizationService.challenge(authCheckingContext.getCurrentSession().getAuthenticationPrincipal(), form.getAction(), form.resourcesArray());
        if (!accessible) {
            throw new HTTP403Exception("Inaccessible!");
        }
        return RestResultPacker.success(form, "Accessible!");
    }

    @PostMapping("/principals/current/authorization-principal-challengings")
    public RestResultPacker<AuthorizationChallengeFormOfPrincipal> authorizationChallengingOfPrincipal(HttpServletRequest request, @RequestBody @Validated AuthorizationChallengeFormOfPrincipal form) throws HTTP400Exception, HTTP401Exception, HTTP403Exception, HTTP404Exception {
        try {
            AuthCheckingContext authCheckingContext = authCheckingHelper.theirResources(request, AuthCheckingStatement.checks("iam:ChallengeAuth", "iam://users/%s/auth-challengings"), AuthenticationPrincipal.idFromLocator(form.getPrincipal()));
            boolean accessible = authorizationService.challenge(authCheckingContext.getResourceOwner(), form.getAction(), form.resourcesArray());
            if (!accessible) {
                throw new HTTP403Exception("Inaccessible!");
            }
        } catch (PatternException e) {
            throw new HTTP400Exception(e);
        }
        return RestResultPacker.success(form, "Accessible!");
    }
}
