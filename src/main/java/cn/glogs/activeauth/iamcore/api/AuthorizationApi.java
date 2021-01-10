package cn.glogs.activeauth.iamcore.api;

import cn.glogs.activeauth.iamcore.api.helper.AuthCheckingHelper;
import cn.glogs.activeauth.iamcore.api.payload.*;
import cn.glogs.activeauth.iamcore.config.properties.LocatorConfiguration;
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
    private final LocatorConfiguration locatorConfiguration;

    public AuthorizationApi(
            AuthenticationPrincipalService authenticationPrincipalService,
            AuthorizationService authorizationService,
            AuthorizationPolicyService authorizationPolicyService,
            AuthorizationPolicyGrantService authorizationPolicyGrantService,
            AuthCheckingHelper authCheckingHelper,
            LocatorConfiguration locatorConfiguration
    ) {
        this.authenticationPrincipalService = authenticationPrincipalService;
        this.authorizationService = authorizationService;
        this.authorizationPolicyService = authorizationPolicyService;
        this.authorizationPolicyGrantService = authorizationPolicyGrantService;
        this.authCheckingHelper = authCheckingHelper;
        this.locatorConfiguration = locatorConfiguration;
    }

    @Operation(tags = {"authorization-policy"})
    @PostMapping("/principals/current/policies")
    public RestResultPacker<AuthorizationPolicy.Vo> addPolicy(HttpServletRequest request, @RequestBody @Validated AuthorizationPolicy.Form form) throws HTTPException {
        AuthCheckingContext authCheckingContext = authCheckingHelper.myResources(
                request, AuthCheckingStatement.checks(
                        "iam:AddPolicy", locatorConfiguration.fullLocator("{}", "policy")
                ));
        AuthorizationPolicy authorizationPolicy = authorizationPolicyService.addPolicy(authCheckingContext.getResourceOwner(), form);
        return RestResultPacker.success(authorizationPolicy.vo(locatorConfiguration));
    }

    @Operation(tags = {"authorization-policy"})
    @PostMapping("/principals/{principalId}/policies")
    public RestResultPacker<AuthorizationPolicy.Vo> addPolicy(HttpServletRequest request, @PathVariable Long principalId, @RequestBody @Validated AuthorizationPolicy.Form form) throws HTTPException {
        AuthCheckingContext authCheckingContext = authCheckingHelper.theirResources(
                request, AuthCheckingStatement.checks(
                        "iam:AddPolicy", locatorConfiguration.fullLocator("{}", "policy")
                ), principalId);
        AuthorizationPolicy authorizationPolicy = authorizationPolicyService.addPolicy(authCheckingContext.getResourceOwner(), form);
        return RestResultPacker.success(authorizationPolicy.vo(locatorConfiguration));
    }

    @Operation(tags = {"authorization-policy"})
    @GetMapping("/principals/current/policies")
    public RestResultPacker<Page<AuthorizationPolicy.Vo>> pagingPolicies(HttpServletRequest request, @RequestParam int page, @RequestParam int size) throws HTTPException {
        AuthCheckingContext authCheckingContext = authCheckingHelper.myResources(
                request, AuthCheckingStatement.checks(
                        "iam:GetPolicy", locatorConfiguration.fullLocator("{}", "policy")
                ));
        return RestResultPacker.success(authorizationPolicyService.pagingPolicies(authCheckingContext.getResourceOwner(), page, size).map(owner -> owner.vo(locatorConfiguration)));
    }

    @Operation(tags = {"authorization-policy"})
    @GetMapping("/principals/{principalId}/policies")
    public RestResultPacker<Page<AuthorizationPolicy.Vo>> pagingPolicies(HttpServletRequest request, @PathVariable Long principalId, @RequestParam int page, @RequestParam int size) throws HTTPException {
        AuthCheckingContext authCheckingContext = authCheckingHelper.theirResources(
                request, AuthCheckingStatement.checks(
                        "iam:GetPolicy", locatorConfiguration.fullLocator("{}", "policy")
                ), principalId);
        return RestResultPacker.success(authorizationPolicyService.pagingPolicies(authCheckingContext.getResourceOwner(), page, size).map(owner -> owner.vo(locatorConfiguration)));
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
        AuthCheckingContext authCheckingContext = authCheckingHelper.myResources(
                request, AuthCheckingStatement.checks(
                        "iam:DeletePolicy", locatorConfiguration.fullLocator("{}", "policy", policyId.toString())
                ).and(
                        "iam:DeletePolicy", locatorConfiguration.fullLocator("{}", "policy", policyId.toString())
                ));
        deletePolicy(authCheckingContext, policyId);
        return RestResultPacker.success("Deleted!");
    }

    @Operation(tags = {"authorization-policy"})
    @DeleteMapping("/principals/{principalId}/policies/{policyId}")
    public RestResultPacker<String> deletePolicy(HttpServletRequest request, @PathVariable Long principalId, @PathVariable Long policyId) throws HTTPException {
        AuthCheckingContext authCheckingContext = authCheckingHelper.theirResources(
                request, AuthCheckingStatement.checks(
                        "iam:DeletePolicy", locatorConfiguration.fullLocator("{}", "policy", policyId.toString())
                ).and(
                        "iam:DeletePolicy", locatorConfiguration.fullLocator("{}", "policy", policyId.toString())
                ), principalId);
        deletePolicy(authCheckingContext, policyId);
        return RestResultPacker.success("Deleted!");
    }

    private List<AuthorizationPolicy> getGrantingPolicies(HttpServletRequest request, List<String> grantingPolicyLocators) throws HTTPException {
        try {
            List<AuthorizationPolicy> grantingPolicies = new ArrayList<>();
            // Check if Current User can get those policies.
            AuthenticationSession session = null;
            for (String policyLocator : grantingPolicyLocators) {
                Long policyId = AuthorizationPolicy.idFromLocator(locatorConfiguration, policyLocator);
                Long policyOwnerId = AuthorizationPolicy.ownerIdFromLocator(locatorConfiguration, policyLocator);
                AuthorizationPolicy policy = authorizationPolicyService.getPolicyById(policyId);
                if (session == null) {
                    AuthCheckingContext authCheckingContext = authCheckingHelper.theirResources(
                            request, AuthCheckingStatement.checks(
                                    "iam:GetPolicy", locatorConfiguration.fullLocator("{}", "policy")
                            ), policyOwnerId);
                    session = authCheckingContext.getCurrentSession();
                } else {
                    authCheckingHelper.theirResources(
                            session, AuthCheckingStatement.checks(
                                    "iam:GetPolicy", locatorConfiguration.fullLocator("{}", "policy")
                            ), policyOwnerId);
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
            AuthenticationPrincipal grantee = authenticationPrincipalService.findPrincipalById(AuthenticationPrincipal.idFromLocator(locatorConfiguration, granteeLocator));
            List<AuthorizationPolicyGrant> policyGrants = authorizationPolicyGrantService.addGrants(granter, grantee, policies);
            List<AuthorizationPolicyGrant.Vo> results = new ArrayList<>();
            policyGrants.forEach(policyGrant -> results.add(policyGrant.vo(locatorConfiguration)));
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
        AuthCheckingContext authCheckingContext = authCheckingHelper.myResources(
                request, AuthCheckingStatement.checks(
                        "iam:AddGrant", locatorConfiguration.fullLocator("{}", "grant")
                ));
        return RestResultPacker.success(grantingPolicies(authCheckingContext, form.getGrantee(), policies));
    }

    @Operation(tags = {"authorization-grant"})
    @PostMapping("/principals/{principalId}/grants")
    public RestResultPacker<List<AuthorizationPolicyGrant.Vo>> addGrant(HttpServletRequest request, @PathVariable Long principalId, @RequestBody @Validated AuthorizationPolicyGrantingForm form) throws HTTPException {
        List<AuthorizationPolicy> policies = getGrantingPolicies(request, form.getPolicies());
        // Check if Current User can add grants.
        AuthCheckingContext authCheckingContext = authCheckingHelper.theirResources(
                request, AuthCheckingStatement.checks(
                        "iam:AddGrant", locatorConfiguration.fullLocator("{}", "grant")
                ), principalId);
        return RestResultPacker.success(grantingPolicies(authCheckingContext, form.getGrantee(), policies));
    }

    @Operation(tags = {"authorization-grant"})
    @GetMapping("/principals/current/grants")
    public RestResultPacker<Page<AuthorizationPolicyGrant.Vo>> pagingGrantsOut(HttpServletRequest request, @RequestParam int page, @RequestParam int size) throws HTTPException {
        AuthCheckingContext authCheckingContext = authCheckingHelper.myResources(
                request, AuthCheckingStatement.checks(
                        "iam:GetGrant", locatorConfiguration.fullLocator("{}", "grant")
                ));
        Page<AuthorizationPolicyGrant> grantsPage = authorizationPolicyGrantService.pagingGrantsFrom(authCheckingContext.getCurrentSession().getAuthenticationPrincipal(), page, size);
        return RestResultPacker.success(grantsPage.map(owner -> owner.vo(locatorConfiguration)));
    }

    @Operation(tags = {"authorization-grant"})
    @GetMapping("/principals/{principalId}/grants")
    public RestResultPacker<Page<AuthorizationPolicyGrant.Vo>> pagingGrantsOut(HttpServletRequest request, @PathVariable Long principalId, @RequestParam int page, @RequestParam int size) throws HTTPException {
        AuthCheckingContext authCheckingContext = authCheckingHelper.theirResources(
                request, AuthCheckingStatement.checks(
                        "iam:GetGrant", locatorConfiguration.fullLocator("{}", "grant")
                ), principalId);
        Page<AuthorizationPolicyGrant> grantsPage = authorizationPolicyGrantService.pagingGrantsFrom(authCheckingContext.getCurrentSession().getAuthenticationPrincipal(), page, size);
        return RestResultPacker.success(grantsPage.map(owner -> owner.vo(locatorConfiguration)));
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
        AuthCheckingContext authCheckingContext = authCheckingHelper.myResources(
                request, AuthCheckingStatement.checks(
                        "iam:DeleteGrant", locatorConfiguration.fullLocator("{}", "grant", grantId.toString())
                ).and(
                        "iam:DeleteGrant", locatorConfiguration.fullLocator("{}", "grant", grantId.toString())
                ));
        deleteGrantOut(authCheckingContext, grantId);
        return RestResultPacker.success("Grant Deleted.");
    }

    @Operation(tags = {"authorization-grant"})
    @DeleteMapping("/principals/{principalId}/grants/{grantId}")
    public RestResultPacker<String> deleteGrantOut(HttpServletRequest request, @PathVariable Long principalId, @PathVariable Long grantId) throws HTTPException {
        AuthCheckingContext authCheckingContext = authCheckingHelper.theirResources(
                request, AuthCheckingStatement.checks(
                        "iam:DeleteGrant", locatorConfiguration.fullLocator("{}", "grant", grantId.toString())
                ).and(
                        "iam:DeleteGrant", locatorConfiguration.fullLocator("{}", "grant", grantId.toString())
                ), principalId);
        deleteGrantOut(authCheckingContext, grantId);
        return RestResultPacker.success("Grant Deleted.");
    }

    @Operation(tags = {"authorization-grant"})
    @GetMapping("/principals/current/grants/in")
    public RestResultPacker<Page<AuthorizationPolicyGrant.Vo>> pagingGrantsIn(HttpServletRequest request, @RequestParam int page, @RequestParam int size) throws HTTPException {
        AuthCheckingContext authCheckingContext = authCheckingHelper.myResources(
                request, AuthCheckingStatement.checks(
                        "iam:GetGrant", locatorConfiguration.fullLocator("{}", "grant-in")
                ));
        Page<AuthorizationPolicyGrant> grantsPage = authorizationPolicyGrantService.pagingGrantsTo(authCheckingContext.getCurrentSession().getAuthenticationPrincipal(), page, size);
        return RestResultPacker.success(grantsPage.map(owner -> owner.vo(locatorConfiguration)));
    }

    @Operation(tags = {"authorization-grant"})
    @GetMapping("/principals/{principalId}/grants/in")
    public RestResultPacker<Page<AuthorizationPolicyGrant.Vo>> pagingGrantsIn(HttpServletRequest request, @PathVariable Long principalId, @RequestParam int page, @RequestParam int size) throws HTTPException {
        AuthCheckingContext authCheckingContext = authCheckingHelper.theirResources(
                request, AuthCheckingStatement.checks(
                        "iam:GetGrant", locatorConfiguration.fullLocator("{}", "grant-in")
                ), principalId);
        Page<AuthorizationPolicyGrant> grantsPage = authorizationPolicyGrantService.pagingGrantsTo(authCheckingContext.getCurrentSession().getAuthenticationPrincipal(), page, size);
        return RestResultPacker.success(grantsPage.map(owner -> owner.vo(locatorConfiguration)));
    }

    @Operation(tags = {"authorization-challenging"})
    @PostMapping("/principals/current/authorization-challengings")
    public RestResultPacker<AuthorizationChallengeForm> authorizationChallenging(HttpServletRequest request, @RequestBody @Validated AuthorizationChallengeForm form) throws HTTPException {
        AuthCheckingContext authCheckingContext = authCheckingHelper.myResources(
                request, AuthCheckingStatement.checks(
                        "iam:ChallengeAuth", locatorConfiguration.fullLocator("{}", "auth-challenging")
                ));
        boolean accessible = authorizationService.challenge(authCheckingContext.getCurrentSession().getAuthenticationPrincipal(), form.getAction(), form.resourcesArray());
        if (!accessible) {
            throw new HTTP403Exception("Inaccessible!");
        }
        return RestResultPacker.success(form, "Accessible!");
    }

    @Operation(tags = {"authorization-challenging"})
    @PostMapping("/principals/{principalId}/authorization-challengings")
    public RestResultPacker<AuthorizationChallengeForm> authorizationChallenging(HttpServletRequest request, @PathVariable Long principalId, @RequestBody @Validated AuthorizationChallengeForm form) throws HTTPException {
        AuthCheckingContext authCheckingContext = authCheckingHelper.theirResources(
                request, AuthCheckingStatement.checks(
                        "iam:ChallengeAuth", locatorConfiguration.fullLocator("{}", "auth-challenging")
                ), principalId);
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
            AuthCheckingContext authCheckingContext = authCheckingHelper.theirResources(
                    request, AuthCheckingStatement.checks(
                            "iam:ChallengeAuth", locatorConfiguration.fullLocator("{}", "auth-challenging")
                    ), AuthenticationPrincipal.idFromLocator(locatorConfiguration, form.getPrincipal()));
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
