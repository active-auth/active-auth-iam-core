package cn.glogs.activeauth.iamcore.api;

import cn.glogs.activeauth.iamcore.api.payload.RestResultPacker;
import cn.glogs.activeauth.iamcore.domain.AuthenticationPrincipal;
import cn.glogs.activeauth.iamcore.domain.AuthenticationSession;
import cn.glogs.activeauth.iamcore.domain.password.PasswordHashingStrategy;
import cn.glogs.activeauth.iamcore.exception.HTTP401Exception;
import cn.glogs.activeauth.iamcore.exception.HTTP404Exception;
import cn.glogs.activeauth.iamcore.exception.business.NotFoundException;
import cn.glogs.activeauth.iamcore.service.AuthenticationPrincipalService;
import cn.glogs.activeauth.iamcore.service.AuthenticationSessionService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/user-center")
@RestController
public class UserApi {
    private final AuthenticationPrincipalService authenticationPrincipalService;
    private final AuthenticationSessionService authenticationSessionService;

    public UserApi(AuthenticationPrincipalService authenticationPrincipalService, AuthenticationSessionService authenticationSessionService) {
        this.authenticationPrincipalService = authenticationPrincipalService;
        this.authenticationSessionService = authenticationSessionService;
    }

    @PostMapping("/login")
    public RestResultPacker<AuthenticationSession.Vo> login(@RequestBody @Validated AuthenticationSession.UserLoginForm form) throws HTTP401Exception, HTTP404Exception {
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
        AuthenticationPrincipal toCreatePrincipal = AuthenticationPrincipal.createPrincipal(form.getName(), form.getPassword(), PasswordHashingStrategy.B_CRYPT, AuthenticationPrincipal.PrincipalType.PRINCIPAL);
        return RestResultPacker.success(authenticationPrincipalService.createPrincipal(toCreatePrincipal).vo());
    }
}
