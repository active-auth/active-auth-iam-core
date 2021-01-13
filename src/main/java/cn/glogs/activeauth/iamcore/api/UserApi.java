package cn.glogs.activeauth.iamcore.api;

import cn.glogs.activeauth.iamcore.api.helper.AuthCheckingHelper;
import cn.glogs.activeauth.iamcore.api.helper.SafetyVerifyingHelper;
import cn.glogs.activeauth.iamcore.api.payload.AuthCheckingContext;
import cn.glogs.activeauth.iamcore.api.payload.AuthCheckingStatement;
import cn.glogs.activeauth.iamcore.api.payload.RestResultPacker;
import cn.glogs.activeauth.iamcore.config.properties.AuthConfiguration;
import cn.glogs.activeauth.iamcore.config.properties.LocatorConfiguration;
import cn.glogs.activeauth.iamcore.config.properties.MfaConfiguration;
import cn.glogs.activeauth.iamcore.domain.AuthenticationDisposableSession;
import cn.glogs.activeauth.iamcore.domain.AuthenticationPrincipal;
import cn.glogs.activeauth.iamcore.domain.AuthenticationSession;
import cn.glogs.activeauth.iamcore.domain.AuthorizationPolicyGrant;
import cn.glogs.activeauth.iamcore.exception.HTTP401Exception;
import cn.glogs.activeauth.iamcore.exception.HTTP403Exception;
import cn.glogs.activeauth.iamcore.exception.HTTP404Exception;
import cn.glogs.activeauth.iamcore.exception.HTTPException;
import cn.glogs.activeauth.iamcore.exception.business.NotFoundException;
import cn.glogs.activeauth.iamcore.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

import java.util.Optional;

import static cn.glogs.activeauth.iamcore.config.SpringDoc.VERIFICATION_TOKEN_ID_$REF;

@RequestMapping("/user-center")
@RestController
public class UserApi {
    private final AuthenticationPrincipalService authenticationPrincipalService;
    private final AuthenticationSessionService authenticationSessionService;
    private final AuthenticationMfaService authenticationMfaService;
    private final AuthenticationDisposableSessionService authenticationDisposableSessionService;
    private final AuthorizationPolicyGrantService authorizationPolicyGrantService;
    private final AuthCheckingHelper authCheckingHelper;
    private final SafetyVerifyingHelper safetyVerifyingHelper;
    private final AuthConfiguration authConfiguration;
    private final LocatorConfiguration locatorConfiguration;
    private final MfaConfiguration mfaConfiguration;

    public UserApi(
            AuthenticationPrincipalService authenticationPrincipalService,
            AuthenticationSessionService authenticationSessionService,
            AuthenticationMfaService authenticationMfaService,
            AuthenticationDisposableSessionService authenticationDisposableSessionService,
            AuthorizationPolicyGrantService authorizationPolicyGrantService,
            AuthCheckingHelper authCheckingHelper,
            SafetyVerifyingHelper safetyVerifyingHelper,
            AuthConfiguration authConfiguration,
            LocatorConfiguration locatorConfiguration,
            MfaConfiguration mfaConfiguration) {
        this.authenticationPrincipalService = authenticationPrincipalService;
        this.authenticationSessionService = authenticationSessionService;
        this.authenticationMfaService = authenticationMfaService;
        this.authenticationDisposableSessionService = authenticationDisposableSessionService;
        this.authorizationPolicyGrantService = authorizationPolicyGrantService;
        this.authCheckingHelper = authCheckingHelper;
        this.safetyVerifyingHelper = safetyVerifyingHelper;
        this.authConfiguration = authConfiguration;
        this.locatorConfiguration = locatorConfiguration;
        this.mfaConfiguration = mfaConfiguration;
    }

    @PostMapping("/user/login")
    public RestResultPacker<AuthenticationSession.Vo> login(HttpServletRequest request, @RequestBody @Validated AuthenticationSession.UserLoginForm form) throws HTTPException {
        try {
            AuthenticationPrincipal principal = authenticationPrincipalService.findPrincipalByName(form.getName());
            safetyVerifyingHelper.reinforce(
                    request, true, principal,
                    AuthCheckingStatement.checks(
                            "iam:login", locatorConfiguration.fullLocator("%s", "login")
                    ), principal.getId());
            return RestResultPacker.success(authenticationSessionService.login(form).vo());
        } catch (NotFoundException e) {
            throw new HTTP404Exception(e);
        } catch (AuthenticationPrincipal.PasswordNotMatchException | AuthenticationPrincipal.PrincipalTypeDoesNotAllowedToLoginException e) {
            throw new HTTP401Exception(e);
        }
    }

    @PostMapping("/user/register")
    public RestResultPacker<AuthenticationPrincipal.Vo> addPrincipal(@RequestBody @Validated AuthenticationPrincipal.UserRegisterForm form) {
        AuthenticationPrincipal toCreatePrincipal = new AuthenticationPrincipal(
                form.getName(), form.getPassword(),
                AuthenticationPrincipal.PrincipalType.PRINCIPAL, authConfiguration.getPasswordHashingStrategy()
        );
        return RestResultPacker.success(authenticationPrincipalService.createPrincipal(toCreatePrincipal).vo(locatorConfiguration));
    }

    @GetMapping("/grants/in")
    public RestResultPacker<Page<AuthorizationPolicyGrant.Vo>> pagingGrantsIn(HttpServletRequest request, @RequestParam int page, @RequestParam int size) throws HTTPException {
        AuthCheckingContext authCheckingContext = authCheckingHelper.myResources(request, AuthCheckingStatement.checks("iam:GetGrant", locatorConfiguration.fullLocator("%s", "grants-in")));
        Page<AuthorizationPolicyGrant> grantsPage = authorizationPolicyGrantService.pagingGrantsTo(authCheckingContext.getCurrentSession().getAuthenticationPrincipal(), page, size);
        return RestResultPacker.success(grantsPage.map(owner -> owner.vo(locatorConfiguration)));
    }

    @PostMapping("/mfa/status")
    public RestResultPacker<String> switchMfaStatus(HttpServletRequest request, @RequestParam boolean mfaEnable, @RequestParam(required = false) String verificationCode) throws HTTPException {
        AuthCheckingContext authCheckingContext = authCheckingHelper.myResources(
                request, AuthCheckingStatement.checks(
                        "iam:SwitchMfa", locatorConfiguration.fullLocator("%s", "mfa")
                ));
        if (mfaEnable) {
            // When changing MFA, verify it first.
            if (authCheckingContext.getResourceOwner().isMfaEnable() && !authenticationMfaService.verify(authCheckingContext.getResourceOwner(), verificationCode))
                throw new HTTP403Exception("Wrong MFA code on changing MFA verification.");
        } else {
            // When disabling MFA, verify it first.
            if (StringUtils.isBlank(verificationCode))
                throw new HTTP403Exception("MFA code is required when disabling MFA verification.");
            if (!authenticationMfaService.verify(authCheckingContext.getResourceOwner(), verificationCode))
                throw new HTTP403Exception("Wrong MFA code on disabling MFA verification.");
        }
        return RestResultPacker.success(authenticationMfaService.setMfa(authCheckingContext.getResourceOwner(), mfaEnable));
    }

    @Operation(parameters = {
            @Parameter(ref = VERIFICATION_TOKEN_ID_$REF)
    })
    @PostMapping("/mfa/verify")
    public RestResultPacker<Object> verifyMfaPassword(HttpServletRequest request, @RequestParam String verificationCode) throws HTTPException {
        String idHeader = mfaConfiguration.getVerificationTokenIdHeader();
        Optional<String> vTokenOpt = Optional.ofNullable(request.getHeader(idHeader));
        if (vTokenOpt.isPresent()) {
            try {
                AuthenticationDisposableSession disposableSession = authenticationDisposableSessionService.getByTokenId(vTokenOpt.get());
                disposableSession.unseal();
                if (authenticationMfaService.verify(disposableSession.getPrincipal(), verificationCode)) {
                    return RestResultPacker.success(authenticationDisposableSessionService.update(disposableSession.getId(), disposableSession).vo());
                } else {
                    return RestResultPacker.failure("Invalid verification code.");
                }
            } catch (NotFoundException e) {
                throw new HTTP401Exception(e);
            }
        } else {
            AuthCheckingContext authCheckingContext = authCheckingHelper.myResources(
                    request, AuthCheckingStatement.checks(
                            "iam:VerifyMfa", locatorConfiguration.fullLocator("%s", "mfa")
                    ));
            if (authenticationMfaService.verify(authCheckingContext.getResourceOwner(), verificationCode)) {
                return RestResultPacker.success("OK");
            } else {
                return RestResultPacker.failure("Invalid verification code.");
            }
        }
    }
}
