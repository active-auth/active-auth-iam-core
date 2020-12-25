package cn.glogs.activeauth.iamcore.api;

import cn.glogs.activeauth.iamcore.api.payload.AuthorizationChallengingForm;
import cn.glogs.activeauth.iamcore.api.payload.AuthorizationPolicyGrantingForm;
import cn.glogs.activeauth.iamcore.api.payload.RestResultPacker;
import cn.glogs.activeauth.iamcore.domain.AuthenticationPrincipal;
import cn.glogs.activeauth.iamcore.domain.AuthenticationSession;
import cn.glogs.activeauth.iamcore.domain.AuthorizationPolicy;
import cn.glogs.activeauth.iamcore.domain.AuthorizationPolicyGrant;
import cn.glogs.activeauth.iamcore.domain.*;
import cn.glogs.activeauth.iamcore.exception.HTTP400Exception;
import cn.glogs.activeauth.iamcore.exception.HTTP403Exception;
import cn.glogs.activeauth.iamcore.exception.HTTP404Exception;
import cn.glogs.activeauth.iamcore.exception.business.NotFoundException;
import cn.glogs.activeauth.iamcore.exception.business.PatternException;
import cn.glogs.activeauth.iamcore.service.AuthenticationPrincipalKeyPairService;
import cn.glogs.activeauth.iamcore.service.AuthenticationPrincipalService;
import cn.glogs.activeauth.iamcore.service.AuthenticationSessionService;
import cn.glogs.activeauth.iamcore.service.AuthorizationService;
import lombok.SneakyThrows;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

@RestController
public class AuthorizationApi {

    private final AuthenticationPrincipalService authenticationPrincipalService;

    private final AuthenticationSessionService authenticationSessionService;

    private final AuthorizationService authorizationService;

    public AuthorizationApi(AuthenticationPrincipalService authenticationPrincipalService, AuthenticationSessionService authenticationSessionService, AuthorizationService authorizationService) {
        this.authenticationPrincipalService = authenticationPrincipalService;
        this.authenticationSessionService = authenticationSessionService;
        this.authorizationService = authorizationService;
    }

    @SneakyThrows
    @PostMapping("/authorization-policy-grants")
    public RestResultPacker<List<AuthorizationPolicyGrant.Vo>> grant(HttpServletRequest request, @RequestBody @Validated AuthorizationPolicyGrantingForm form) {
        try {
            AuthenticationPrincipal granter = authenticationSessionService.getMeSession(request).getAuthenticationPrincipal();
            AuthenticationPrincipal grantee = authenticationPrincipalService.findPrincipalByLocator(form.getGrantee());
            List<AuthorizationPolicy> policies = new ArrayList<>();
            for (String policyLocator : form.getPolicies()) {
                policies.add(authorizationService.getPolicyByLocator(policyLocator));
            }
            List<AuthorizationPolicyGrant> policyGrants = authorizationService.grantPolicies(granter, grantee, policies);
            List<AuthorizationPolicyGrant.Vo> results = new ArrayList<>();
            policyGrants.forEach(policyGrant -> results.add(policyGrant.vo()));
            return RestResultPacker.success(results);
        } catch (AuthenticationSession.SessionRequestBadHeaderException | PatternException e) {
            throw new HTTP400Exception(e);
        } catch (AuthenticationSession.SessionNotFoundException e) {
            throw new HTTP403Exception(e);
        } catch (NotFoundException e) {
            throw new HTTP404Exception(e);
        }
    }

    @PostMapping("/authorization-policies")
    public RestResultPacker<AuthorizationPolicy.Vo> addPolicy(HttpServletRequest request, @RequestBody @Validated AuthorizationPolicy.Form form) throws HTTP400Exception, HTTP403Exception {
        try {
            AuthenticationSession authenticationSession = authenticationSessionService.getMeSession(request);
            AuthorizationPolicy authorizationPolicy = authorizationService.addPolicy(authenticationSession.getAuthenticationPrincipal(), form);
            return RestResultPacker.success(authorizationPolicy.vo());
        } catch (AuthenticationSession.SessionRequestBadHeaderException e) {
            throw new HTTP400Exception(e);
        } catch (AuthenticationSession.SessionNotFoundException e) {
            throw new HTTP403Exception(e);
        }
    }

    @PostMapping("/authorizations/challenging")
    public RestResultPacker<AuthorizationChallengingForm> authorizationChallenging(HttpServletRequest request, @RequestBody @Validated AuthorizationChallengingForm form) throws HTTP400Exception, HTTP403Exception {
        try {
            AuthenticationSession currentAuthenticationSession = authenticationSessionService.getMeSession(request);
            boolean accessible = authorizationService.challenge(currentAuthenticationSession.getAuthenticationPrincipal(), form.getAction(), form.getResources());
            if (!accessible) {
                throw new HTTP403Exception("Inaccessible!");
            }
        } catch (AuthenticationSession.SessionRequestBadHeaderException e) {
            throw new HTTP400Exception(e);
        } catch (AuthenticationSession.SessionNotFoundException e) {
            throw new HTTP403Exception(e);
        }
        return RestResultPacker.success(form, "Accessible!");
    }
}
