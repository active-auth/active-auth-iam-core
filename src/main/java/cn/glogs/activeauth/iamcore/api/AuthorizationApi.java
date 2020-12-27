package cn.glogs.activeauth.iamcore.api;

import cn.glogs.activeauth.iamcore.api.payload.AuthorizationChallengeForm;
import cn.glogs.activeauth.iamcore.api.payload.AuthorizationChallengeFormOfPrincipal;
import cn.glogs.activeauth.iamcore.api.payload.AuthorizationPolicyGrantingForm;
import cn.glogs.activeauth.iamcore.api.payload.RestResultPacker;
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
    private final AuthenticationSessionService authenticationSessionService;
    private final AuthorizationService authorizationService;
    private final AuthorizationPolicyService authorizationPolicyService;
    private final AuthorizationPolicyGrantService authorizationPolicyGrantService;

    public AuthorizationApi(AuthenticationPrincipalService authenticationPrincipalService, AuthenticationSessionService authenticationSessionService, AuthorizationService authorizationService, AuthorizationPolicyService authorizationPolicyService, AuthorizationPolicyGrantService authorizationPolicyGrantService) {
        this.authenticationPrincipalService = authenticationPrincipalService;
        this.authenticationSessionService = authenticationSessionService;
        this.authorizationService = authorizationService;
        this.authorizationPolicyService = authorizationPolicyService;
        this.authorizationPolicyGrantService = authorizationPolicyGrantService;
    }

    @PostMapping("/principals/current/policies")
    public RestResultPacker<AuthorizationPolicy.Vo> addPolicy(HttpServletRequest request, @RequestBody @Validated AuthorizationPolicy.Form form) throws HTTP401Exception, HTTP403Exception {
        try {
            AuthenticationSession authenticationSession = authenticationSessionService.getMeSession(request);
            AuthorizationPolicy authorizationPolicy = authorizationPolicyService.addPolicy(authenticationSession.getAuthenticationPrincipal(), form);
            return RestResultPacker.success(authorizationPolicy.vo());
        } catch (AuthenticationSession.SessionRequestNotAuthorizedException e) {
            throw new HTTP401Exception(e);
        } catch (AuthenticationSession.SessionNotFoundException e) {
            throw new HTTP403Exception(e);
        }
    }

    @GetMapping("/principals/current/policies")
    public RestResultPacker<Page<AuthorizationPolicy.Vo>> pagingPolicies(HttpServletRequest request, @RequestParam int page, @RequestParam int size) throws HTTP401Exception, HTTP403Exception {
        try {
            AuthenticationPrincipal currentPrincipal = authenticationSessionService.getMeSession(request).getAuthenticationPrincipal();
            return RestResultPacker.success(authorizationPolicyService.pagingPolicies(currentPrincipal, page, size).map(AuthorizationPolicy::vo));
        } catch (AuthenticationSession.SessionRequestNotAuthorizedException e) {
            throw new HTTP401Exception(e);
        } catch (AuthenticationSession.SessionNotFoundException e) {
            throw new HTTP403Exception(e);
        }
    }

    @DeleteMapping("/principals/current/policies/{policyId}")
    public RestResultPacker<AuthorizationPolicy.Vo> deletePolicy(HttpServletRequest request, @PathVariable Long policyId) throws HTTP401Exception, HTTP403Exception, HTTP404Exception {
        try {
            AuthenticationPrincipal currentPrincipal = authenticationSessionService.getMeSession(request).getAuthenticationPrincipal();
            AuthorizationPolicy policy = authorizationPolicyService.getPolicyById(policyId);
            boolean gettable = authorizationService.challenge(currentPrincipal, "iam:GetPolicy", String.format("iam://users/%s/policies/%s", currentPrincipal.getId(), policyId));
            boolean deletable = authorizationService.challenge(currentPrincipal, "iam:DeletePolicy", String.format("iam://users/%s/policies/%s", currentPrincipal.getId(), policyId));
            if (gettable && deletable) {
                authorizationPolicyService.deletePolicy(policyId);
                return RestResultPacker.success(policy.vo());
            } else {
                throw new HTTP403Exception("Inaccessible!");
            }
        } catch (AuthenticationSession.SessionRequestNotAuthorizedException e) {
            throw new HTTP401Exception(e);
        } catch (AuthenticationSession.SessionNotFoundException e) {
            throw new HTTP403Exception(e);
        } catch (NotFoundException e) {
            throw new HTTP404Exception(e);
        }
    }

    @PostMapping("/principals/current/grants")
    public RestResultPacker<List<AuthorizationPolicyGrant.Vo>> addGrant(HttpServletRequest request, @RequestBody @Validated AuthorizationPolicyGrantingForm form) throws HTTP400Exception, HTTP401Exception, HTTP403Exception, HTTP404Exception {
        try {
            AuthenticationPrincipal granter = authenticationSessionService.getMeSession(request).getAuthenticationPrincipal();
            AuthenticationPrincipal grantee = authenticationPrincipalService.findPrincipalByLocator(form.getGrantee());
            List<AuthorizationPolicy> policies = new ArrayList<>();
            for (String policyLocator : form.getPolicies()) {
                policies.add(authorizationPolicyService.getPolicyById(AuthorizationPolicy.idFromLocator(policyLocator)));
            }
            List<AuthorizationPolicyGrant> policyGrants = authorizationPolicyGrantService.addGrants(granter, grantee, policies);
            List<AuthorizationPolicyGrant.Vo> results = new ArrayList<>();
            policyGrants.forEach(policyGrant -> results.add(policyGrant.vo()));
            return RestResultPacker.success(results);
        } catch (PatternException e) {
            throw new HTTP400Exception(e);
        } catch (AuthenticationSession.SessionRequestNotAuthorizedException e) {
            throw new HTTP401Exception(e);
        } catch (AuthenticationSession.SessionNotFoundException e) {
            throw new HTTP403Exception(e);
        } catch (NotFoundException e) {
            throw new HTTP404Exception(e);
        }
    }

    @GetMapping("/principals/current/grants")
    public RestResultPacker<Page<AuthorizationPolicyGrant.Vo>> pagingGrantsOut(HttpServletRequest request, @RequestParam int page, @RequestParam int size) throws HTTP401Exception, HTTP403Exception {
        try {
            AuthenticationPrincipal currentPrincipal = authenticationSessionService.getMeSession(request).getAuthenticationPrincipal();
            authorizationService.challenge(currentPrincipal, "iam:GetGrants", String.format("iam://users/%s/grants", currentPrincipal.getId()));
            Page<AuthorizationPolicyGrant> grantsPage = authorizationPolicyGrantService.pagingGrantsFrom(currentPrincipal, page, size);
            return RestResultPacker.success(grantsPage.map(AuthorizationPolicyGrant::vo));
        } catch (AuthenticationSession.SessionRequestNotAuthorizedException e) {
            throw new HTTP401Exception(e);
        } catch (AuthenticationSession.SessionNotFoundException e) {
            throw new HTTP403Exception(e);
        }
    }

    @DeleteMapping("/principals/current/grants/{grantId}")
    public RestResultPacker<AuthorizationPolicyGrant.Vo> deleteGrantsOut(HttpServletRequest request, @PathVariable Long grantId) throws HTTP401Exception, HTTP403Exception, HTTP404Exception {
        try {
            AuthenticationPrincipal currentPrincipal = authenticationSessionService.getMeSession(request).getAuthenticationPrincipal();
            AuthorizationPolicyGrant grant = authorizationPolicyGrantService.getGrantById(grantId);
            boolean gettable = authorizationService.challenge(currentPrincipal, "iam:GetGrant", String.format("iam://users/%s/grants/%s", currentPrincipal.getId(), grantId));
            boolean deletable = authorizationService.challenge(currentPrincipal, "iam:DeleteGrant", String.format("iam://users/%s/grants/%s", currentPrincipal.getId(), grantId));
            if (gettable && deletable) {
                authorizationPolicyGrantService.deleteGrant(grantId);
                return RestResultPacker.success(grant.vo());
            } else {
                throw new HTTP403Exception("Inaccessible!");
            }
        } catch (AuthenticationSession.SessionRequestNotAuthorizedException e) {
            throw new HTTP401Exception(e);
        } catch (AuthenticationSession.SessionNotFoundException e) {
            throw new HTTP403Exception(e);
        }
    }

    @GetMapping("/principals/current/grants-in")
    public RestResultPacker<Page<AuthorizationPolicyGrant.Vo>> pagingGrantsIn(HttpServletRequest request, @RequestParam int page, @RequestParam int size) throws HTTP401Exception, HTTP403Exception {
        try {
            AuthenticationPrincipal currentPrincipal = authenticationSessionService.getMeSession(request).getAuthenticationPrincipal();
            authorizationService.challenge(currentPrincipal, "iam:GetGrants", String.format("iam://users/%s/grants-in", currentPrincipal.getId()));
            Page<AuthorizationPolicyGrant> grantsPage = authorizationPolicyGrantService.pagingGrantsTo(currentPrincipal, page, size);
            return RestResultPacker.success(grantsPage.map(AuthorizationPolicyGrant::vo));
        } catch (AuthenticationSession.SessionRequestNotAuthorizedException e) {
            throw new HTTP401Exception(e);
        } catch (AuthenticationSession.SessionNotFoundException e) {
            throw new HTTP403Exception(e);
        }
    }

    @PostMapping("/principals/current/authorization-challengings")
    public RestResultPacker<AuthorizationChallengeForm> authorizationChallenging(HttpServletRequest request, @RequestBody @Validated AuthorizationChallengeForm form) throws HTTP401Exception, HTTP403Exception {
        try {
            AuthenticationSession currentAuthenticationSession = authenticationSessionService.getMeSession(request);
            boolean accessible = authorizationService.challenge(currentAuthenticationSession.getAuthenticationPrincipal(), form.getAction(), form.resourcesArray());
            if (!accessible) {
                throw new HTTP403Exception("Inaccessible!");
            }
        } catch (AuthenticationSession.SessionRequestNotAuthorizedException e) {
            throw new HTTP401Exception(e);
        } catch (AuthenticationSession.SessionNotFoundException e) {
            throw new HTTP403Exception(e);
        }
        return RestResultPacker.success(form, "Accessible!");
    }

    @PostMapping("/principals/current/authorization-principal-challengings")
    public RestResultPacker<AuthorizationChallengeFormOfPrincipal> authorizationChallengingOfPrincipal(HttpServletRequest request, @RequestBody @Validated AuthorizationChallengeFormOfPrincipal form) throws HTTP400Exception, HTTP401Exception, HTTP403Exception, HTTP404Exception {
        try {
            AuthenticationSession currentAuthenticationSession = authenticationSessionService.getMeSession(request);
            AuthenticationPrincipal targetPrincipal = authenticationPrincipalService.findPrincipalByLocator(form.getPrincipal());
            boolean canChallenge = authorizationService.challenge(currentAuthenticationSession.getAuthenticationPrincipal(), "iam:ChallengeAuth", String.format("iam://users/%s/auth-challengings", targetPrincipal.getId()));
            if (!canChallenge) {
                throw new HTTP403Exception("Inaccessible: cannot challenge!");
            }
            boolean accessible = authorizationService.challenge(currentAuthenticationSession.getAuthenticationPrincipal(), form.getAction(), form.resourcesArray());
            if (!accessible) {
                throw new HTTP403Exception("Inaccessible!");
            }
        } catch (PatternException e) {
            throw new HTTP400Exception(e);
        } catch (AuthenticationSession.SessionRequestNotAuthorizedException e) {
            throw new HTTP401Exception(e);
        } catch (AuthenticationSession.SessionNotFoundException e) {
            throw new HTTP403Exception(e);
        } catch (NotFoundException e) {
            throw new HTTP404Exception(e);
        }
        return RestResultPacker.success(form, "Accessible!");
    }
}
