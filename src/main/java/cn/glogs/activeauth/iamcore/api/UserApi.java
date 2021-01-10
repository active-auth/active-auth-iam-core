package cn.glogs.activeauth.iamcore.api;

import cn.glogs.activeauth.iamcore.api.helper.AuthCheckingHelper;
import cn.glogs.activeauth.iamcore.api.payload.AuthCheckingContext;
import cn.glogs.activeauth.iamcore.api.payload.AuthCheckingStatement;
import cn.glogs.activeauth.iamcore.api.payload.RestResultPacker;
import cn.glogs.activeauth.iamcore.config.properties.LocatorConfiguration;
import cn.glogs.activeauth.iamcore.domain.AuthenticationPrincipal;
import cn.glogs.activeauth.iamcore.domain.AuthenticationSession;
import cn.glogs.activeauth.iamcore.domain.AuthorizationPolicyGrant;
import cn.glogs.activeauth.iamcore.domain.password.PasswordHashingStrategy;
import cn.glogs.activeauth.iamcore.exception.HTTP401Exception;
import cn.glogs.activeauth.iamcore.exception.HTTP404Exception;
import cn.glogs.activeauth.iamcore.exception.HTTPException;
import cn.glogs.activeauth.iamcore.exception.business.NotFoundException;
import cn.glogs.activeauth.iamcore.service.AuthenticationPrincipalService;
import cn.glogs.activeauth.iamcore.service.AuthenticationSessionService;
import cn.glogs.activeauth.iamcore.service.AuthorizationPolicyGrantService;
import org.springframework.data.domain.Page;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RequestMapping("/user-center")
@RestController
public class UserApi {
    private final AuthenticationPrincipalService authenticationPrincipalService;
    private final AuthenticationSessionService authenticationSessionService;
    private final AuthorizationPolicyGrantService authorizationPolicyGrantService;
    private final AuthCheckingHelper authCheckingHelper;
    private final LocatorConfiguration locatorConfiguration;

    public UserApi(
            AuthenticationPrincipalService authenticationPrincipalService,
            AuthenticationSessionService authenticationSessionService,
            AuthorizationPolicyGrantService authorizationPolicyGrantService,
            AuthCheckingHelper authCheckingHelper,
            LocatorConfiguration locatorConfiguration
    ) {
        this.authenticationPrincipalService = authenticationPrincipalService;
        this.authenticationSessionService = authenticationSessionService;
        this.authorizationPolicyGrantService = authorizationPolicyGrantService;
        this.authCheckingHelper = authCheckingHelper;
        this.locatorConfiguration = locatorConfiguration;
    }

    @PostMapping("/login")
    public RestResultPacker<AuthenticationSession.Vo> login(@RequestBody @Validated AuthenticationSession.UserLoginForm form) throws HTTPException {
        try {
            return RestResultPacker.success(authenticationSessionService.login(form).vo());
        } catch (NotFoundException e) {
            throw new HTTP404Exception(e);
        } catch (AuthenticationPrincipal.PasswordNotMatchException | AuthenticationPrincipal.PrincipalTypeDoesNotAllowedToLoginException e) {
            throw new HTTP401Exception(e);
        }
    }

    @PostMapping("/register")
    public RestResultPacker<AuthenticationPrincipal.Vo> addPrincipal(@RequestBody @Validated AuthenticationPrincipal.UserRegisterForm form) {
        AuthenticationPrincipal toCreatePrincipal = new AuthenticationPrincipal(
                form.getName(), form.getPassword(),
                true, true,
                true, true,
                AuthenticationPrincipal.PrincipalType.PRINCIPAL, PasswordHashingStrategy.B_CRYPT
        );
        return RestResultPacker.success(authenticationPrincipalService.createPrincipal(toCreatePrincipal).vo(locatorConfiguration));
    }

    @GetMapping("/grants/in")
    public RestResultPacker<Page<AuthorizationPolicyGrant.Vo>> pagingGrantsIn(HttpServletRequest request, @RequestParam int page, @RequestParam int size) throws HTTPException {
        AuthCheckingContext authCheckingContext = authCheckingHelper.myResources(request, AuthCheckingStatement.checks("iam:GetGrant", "iam://users/%s/grants-in"));
        Page<AuthorizationPolicyGrant> grantsPage = authorizationPolicyGrantService.pagingGrantsTo(authCheckingContext.getCurrentSession().getAuthenticationPrincipal(), page, size);
        return RestResultPacker.success(grantsPage.map(owner -> owner.vo(locatorConfiguration)));
    }

}
