package cn.glogs.activeauth.iamcore.api;

import cn.glogs.activeauth.iamcore.api.helper.AuthCheckingHelper;
import cn.glogs.activeauth.iamcore.api.payload.*;
import cn.glogs.activeauth.iamcore.domain.AuthenticationPrincipal;
import cn.glogs.activeauth.iamcore.domain.AuthenticationSession;
import cn.glogs.activeauth.iamcore.domain.AuthorizationPolicy;
import cn.glogs.activeauth.iamcore.domain.AuthorizationPolicyGrant;
import cn.glogs.activeauth.iamcore.exception.*;
import cn.glogs.activeauth.iamcore.exception.business.NotFoundException;
import cn.glogs.activeauth.iamcore.exception.business.PatternException;
import cn.glogs.activeauth.iamcore.service.*;
import io.swagger.v3.oas.annotations.Operation;
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

    @Operation(tags = {"authorization-policy"})
    @PostMapping("/principals/current/policies")
    public RestResultPacker<AuthorizationPolicy.Vo> addPolicy(HttpServletRequest request, @RequestBody @Validated AuthorizationPolicy.Form form) throws HTTPException {
        AuthCheckingContext authCheckingContext = authCheckingHelper.myResources(request, AuthCheckingStatement.checks("iam:AddPolicy", "iam://users/%s/policies"));
        AuthorizationPolicy authorizationPolicy = authorizationPolicyService.addPolicy(authCheckingContext.getResourceOwner(), form);
        return RestResultPacker.success(authorizationPolicy.vo());
    }

    @Operation(tags = {"authorization-policy"})
    @PostMapping("/principals/{principalId}/policies")
    public RestResultPacker<AuthorizationPolicy.Vo> addPolicy(HttpServletRequest request, @PathVariable Long principalId, @RequestBody @Validated AuthorizationPolicy.Form form) throws HTTPException {
        AuthCheckingContext authCheckingContext = authCheckingHelper.theirResources(request, AuthCheckingStatement.checks("iam:AddPolicy", "iam://users/%s/policies"), principalId);
        AuthorizationPolicy authorizationPolicy = authorizationPolicyService.addPolicy(authCheckingContext.getResourceOwner(), form);
        return RestResultPacker.success(authorizationPolicy.vo());
    }

    @Operation(tags = {"authorization-policy"})
    @GetMapping("/principals/current/policies")
    public RestResultPacker<Page<AuthorizationPolicy.Vo>> pagingPolicies(HttpServletRequest request, @RequestParam int page, @RequestParam int size) throws HTTPException {
        AuthCheckingContext authCheckingContext = authCheckingHelper.myResources(request, AuthCheckingStatement.checks("iam:GetPolicy", "iam://users/%s/policies"));
        return RestResultPacker.success(authorizationPolicyService.pagingPolicies(authCheckingContext.getResourceOwner(), page, size).map(AuthorizationPolicy::vo));
    }

    @Operation(tags = {"authorization-policy"})
    @GetMapping("/principals/{principalId}/policies")
    public RestResultPacker<Page<AuthorizationPolicy.Vo>> pagingPolicies(HttpServletRequest request, @PathVariable Long principalId, @RequestParam int page, @RequestParam int size) throws HTTPException {
        AuthCheckingContext authCheckingContext = authCheckingHelper.theirResources(request, AuthCheckingStatement.checks("iam:GetPolicy", "iam://users/%s/policies"), principalId);
        return RestResultPacker.success(authorizationPolicyService.pagingPolicies(authCheckingContext.getResourceOwner(), page, size).map(AuthorizationPolicy::vo));
    }

    private void deletePolicy(AuthCheckingContext authCheckingContext, Long policyId) throws HTTP404Exception {
        try {
            AuthorizationPolicy policy = authorizationPolicyService.getPolicyById(policyId);
            if (authCheckingContext.belongToCurrentSession(policy.getOwner())) {
                authorizationPolicyService.deletePolicy(policyId);
            } else {
                throw new NotFoundException("Cannot find policy " + policyId + " of current owner.");
            }
        } catch (NotFoundException e) {
            throw new HTTP404Exception(e);
        }
    }

    @Operation(tags = {"authorization-policy"})
    @DeleteMapping("/principals/current/policies/{policyId}")
    public RestResultPacker<String> deletePolicy(HttpServletRequest request, @PathVariable Long policyId) throws HTTPException {
        AuthCheckingContext authCheckingContext = authCheckingHelper.myResources(request, AuthCheckingStatement.checks("iam:DeletePolicy", "iam://users/%s/policies/" + policyId).and("iam:DeletePolicy", "iam://users/%s/policies" + policyId));
        deletePolicy(authCheckingContext, policyId);
        return RestResultPacker.success("Deleted!");
    }

    @Operation(tags = {"authorization-policy"})
    @DeleteMapping("/principals/{principalId}/policies/{policyId}")
    public RestResultPacker<String> deletePolicy(HttpServletRequest request, @PathVariable Long principalId, @PathVariable Long policyId) throws HTTPException {
        AuthCheckingContext authCheckingContext = authCheckingHelper.theirResources(request, AuthCheckingStatement.checks("iam:DeletePolicy", "iam://users/%s/policies/" + policyId).and("iam:DeletePolicy", "iam://users/%s/policies" + policyId), principalId);
        deletePolicy(authCheckingContext, policyId);
        return RestResultPacker.success("Deleted!");
    }

    private List<AuthorizationPolicy> getGrantingPolicies(HttpServletRequest request, List<String> grantingPolicyLocators) throws HTTPException {
        try {
            List<AuthorizationPolicy> grantingPolicies = new ArrayList<>();
            // Check if Current User can get those policies.
            AuthenticationSession session = null;
            for (String policyLocator : grantingPolicyLocators) {
                Long policyId = AuthorizationPolicy.idFromLocator(policyLocator);
                Long policyOwnerId = AuthorizationPolicy.ownerIdFromLocator(policyLocator);
                AuthorizationPolicy policy = authorizationPolicyService.getPolicyById(policyId);
                if (session == null) {
                    AuthCheckingContext authCheckingContext = authCheckingHelper.theirResources(request, AuthCheckingStatement.checks("iam:GetPolicy", "iam://users/%s/policies"), policyOwnerId);
                    session = authCheckingContext.getCurrentSession();
                } else {
                    authCheckingHelper.theirResources(session, AuthCheckingStatement.checks("iam:GetPolicy", "iam://users/%s/policies"), policyOwnerId);
                }
                grantingPolicies.add(policy);
            }
            return grantingPolicies;
        } catch (PatternException e) {
            throw new HTTP400Exception(e);
        } catch (NotFoundException e) {
            throw new HTTP404Exception(e);
        }
    }

    private List<AuthorizationPolicyGrant.Vo> grantingPolicies(AuthCheckingContext authCheckingContext, String granteeLocator, List<AuthorizationPolicy> policies) throws HTTPException {
        try {
            AuthenticationPrincipal granter = authCheckingContext.getCurrentSession().getAuthenticationPrincipal();
            AuthenticationPrincipal grantee = authenticationPrincipalService.findPrincipalById(AuthenticationPrincipal.idFromLocator(granteeLocator));
            List<AuthorizationPolicyGrant> policyGrants = authorizationPolicyGrantService.addGrants(granter, grantee, policies);
            List<AuthorizationPolicyGrant.Vo> results = new ArrayList<>();
            policyGrants.forEach(policyGrant -> results.add(policyGrant.vo()));
            return results;
        } catch (PatternException e) {
            throw new HTTP400Exception(e);
        } catch (NotFoundException e) {
            throw new HTTP404Exception(e);
        }
    }

    @Operation(tags = {"authorization-grant"})
    @PostMapping("/principals/current/grants")
    public RestResultPacker<List<AuthorizationPolicyGrant.Vo>> addGrant(HttpServletRequest request, @RequestBody @Validated AuthorizationPolicyGrantingForm form) throws HTTPException {
        List<AuthorizationPolicy> policies = getGrantingPolicies(request, form.getPolicies());
        // Check if Current User can add grants.
        AuthCheckingContext authCheckingContext = authCheckingHelper.myResources(request, AuthCheckingStatement.checks("iam:AddGrant", "iam://users/%s/grants"));
        return RestResultPacker.success(grantingPolicies(authCheckingContext, form.getGrantee(), policies));
    }

    @Operation(tags = {"authorization-grant"})
    @PostMapping("/principals/{principalId}/grants")
    public RestResultPacker<List<AuthorizationPolicyGrant.Vo>> addGrant(HttpServletRequest request, @PathVariable Long principalId, @RequestBody @Validated AuthorizationPolicyGrantingForm form) throws HTTPException {
        List<AuthorizationPolicy> policies = getGrantingPolicies(request, form.getPolicies());
        // Check if Current User can add grants.
        AuthCheckingContext authCheckingContext = authCheckingHelper.theirResources(request, AuthCheckingStatement.checks("iam:AddGrant", "iam://users/%s/grants"), principalId);
        return RestResultPacker.success(grantingPolicies(authCheckingContext, form.getGrantee(), policies));
    }

    @Operation(tags = {"authorization-grant"})
    @GetMapping("/principals/current/grants")
    public RestResultPacker<Page<AuthorizationPolicyGrant.Vo>> pagingGrantsOut(HttpServletRequest request, @RequestParam int page, @RequestParam int size) throws HTTPException {
        AuthCheckingContext authCheckingContext = authCheckingHelper.myResources(request, AuthCheckingStatement.checks("iam:GetGrant", "iam://users/%s/grants"));
        Page<AuthorizationPolicyGrant> grantsPage = authorizationPolicyGrantService.pagingGrantsFrom(authCheckingContext.getCurrentSession().getAuthenticationPrincipal(), page, size);
        return RestResultPacker.success(grantsPage.map(AuthorizationPolicyGrant::vo));
    }

    @Operation(tags = {"authorization-grant"})
    @GetMapping("/principals/{principalId}/grants")
    public RestResultPacker<Page<AuthorizationPolicyGrant.Vo>> pagingGrantsOut(HttpServletRequest request, @PathVariable Long principalId, @RequestParam int page, @RequestParam int size) throws HTTPException {
        AuthCheckingContext authCheckingContext = authCheckingHelper.theirResources(request, AuthCheckingStatement.checks("iam:GetGrant", "iam://users/%s/grants"), principalId);
        Page<AuthorizationPolicyGrant> grantsPage = authorizationPolicyGrantService.pagingGrantsFrom(authCheckingContext.getCurrentSession().getAuthenticationPrincipal(), page, size);
        return RestResultPacker.success(grantsPage.map(AuthorizationPolicyGrant::vo));
    }

    private void deleteGrantOut(AuthCheckingContext authCheckingContext, Long grantId) throws HTTP404Exception {
        try {
            AuthorizationPolicyGrant grant = authorizationPolicyGrantService.getGrantById(grantId);
            if (authCheckingContext.belongToCurrentSession(grant.getGranter())) {
                authorizationPolicyGrantService.deleteGrant(grantId);
            } else {
                throw new NotFoundException("Cannot find grant " + grantId + " of current owner.");
            }
        } catch (NotFoundException e) {
            throw new HTTP404Exception(e);
        }
    }

    @Operation(tags = {"authorization-grant"})
    @DeleteMapping("/principals/current/grants/{grantId}")
    public RestResultPacker<String> deleteGrantOut(HttpServletRequest request, @PathVariable Long grantId) throws HTTPException {
        AuthCheckingContext authCheckingContext = authCheckingHelper.myResources(request, AuthCheckingStatement.checks("iam:DeleteGrant", "iam://users/%s/grants/" + grantId).and("iam:DeleteGrant", "iam://users/%s/grants/" + grantId));
        deleteGrantOut(authCheckingContext, grantId);
        return RestResultPacker.success("Grant Deleted.");
    }

    @Operation(tags = {"authorization-grant"})
    @DeleteMapping("/principals/{principalId}/grants/{grantId}")
    public RestResultPacker<String> deleteGrantOut(HttpServletRequest request, @PathVariable Long principalId, @PathVariable Long grantId) throws HTTPException {
        AuthCheckingContext authCheckingContext = authCheckingHelper.theirResources(request, AuthCheckingStatement.checks("iam:DeleteGrant", "iam://users/%s/grants/" + grantId).and("iam:DeleteGrant", "iam://users/%s/grants/" + grantId), principalId);
        deleteGrantOut(authCheckingContext, grantId);
        return RestResultPacker.success("Grant Deleted.");
    }

    @Operation(tags = {"authorization-grant"})
    @GetMapping("/principals/current/grants/in")
    public RestResultPacker<Page<AuthorizationPolicyGrant.Vo>> pagingGrantsIn(HttpServletRequest request, @RequestParam int page, @RequestParam int size) throws HTTPException {
        AuthCheckingContext authCheckingContext = authCheckingHelper.myResources(request, AuthCheckingStatement.checks("iam:GetGrant", "iam://users/%s/grants-in"));
        Page<AuthorizationPolicyGrant> grantsPage = authorizationPolicyGrantService.pagingGrantsTo(authCheckingContext.getCurrentSession().getAuthenticationPrincipal(), page, size);
        return RestResultPacker.success(grantsPage.map(AuthorizationPolicyGrant::vo));
    }

    @Operation(tags = {"authorization-grant"})
    @GetMapping("/principals/{principalId}/grants/in")
    public RestResultPacker<Page<AuthorizationPolicyGrant.Vo>> pagingGrantsIn(HttpServletRequest request, @PathVariable Long principalId, @RequestParam int page, @RequestParam int size) throws HTTPException {
        AuthCheckingContext authCheckingContext = authCheckingHelper.theirResources(request, AuthCheckingStatement.checks("iam:GetGrant", "iam://users/%s/grants-in"), principalId);
        Page<AuthorizationPolicyGrant> grantsPage = authorizationPolicyGrantService.pagingGrantsTo(authCheckingContext.getCurrentSession().getAuthenticationPrincipal(), page, size);
        return RestResultPacker.success(grantsPage.map(AuthorizationPolicyGrant::vo));
    }

    @Operation(tags = {"authorization-challenging"})
    @PostMapping("/principals/current/authorization-challengings")
    public RestResultPacker<AuthorizationChallengeForm> authorizationChallenging(HttpServletRequest request, @RequestBody @Validated AuthorizationChallengeForm form) throws HTTPException {
        AuthCheckingContext authCheckingContext = authCheckingHelper.myResources(request, AuthCheckingStatement.checks("iam:ChallengeAuth", "iam://users/%s/auth-challengings"));
        boolean accessible = authorizationService.challenge(authCheckingContext.getCurrentSession().getAuthenticationPrincipal(), form.getAction(), form.resourcesArray());
        if (!accessible) {
            throw new HTTP403Exception("Inaccessible!");
        }
        return RestResultPacker.success(form, "Accessible!");
    }

    @Operation(tags = {"authorization-challenging"})
    @PostMapping("/principals/{principalId}/authorization-challengings")
    public RestResultPacker<AuthorizationChallengeForm> authorizationChallenging(HttpServletRequest request, @PathVariable Long principalId, @RequestBody @Validated AuthorizationChallengeForm form) throws HTTPException {
        AuthCheckingContext authCheckingContext = authCheckingHelper.theirResources(request, AuthCheckingStatement.checks("iam:ChallengeAuth", "iam://users/%s/auth-challengings"), principalId);
        boolean accessible = authorizationService.challenge(authCheckingContext.getCurrentSession().getAuthenticationPrincipal(), form.getAction(), form.resourcesArray());
        if (!accessible) {
            throw new HTTP403Exception("Inaccessible!");
        }
        return RestResultPacker.success(form, "Accessible!");
    }

    @Operation(tags = {"authorization-challenging"})
    @PostMapping("/principals/current/authorization-principal-challengings")
    public RestResultPacker<AuthorizationChallengeFormOfPrincipal> authorizationChallengingOfPrincipal(HttpServletRequest request, @RequestBody @Validated AuthorizationChallengeFormOfPrincipal form) throws HTTPException {
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
