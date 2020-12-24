package cn.glogs.activeauth.iamcore.api;

import cn.glogs.activeauth.iamcore.api.payload.RestResultPacker;
import cn.glogs.activeauth.iamcore.domain.AuthenticationPrincipal;
import cn.glogs.activeauth.iamcore.domain.AuthenticationSession;
import cn.glogs.activeauth.iamcore.exception.HTTP401Exception;
import cn.glogs.activeauth.iamcore.exception.HTTP404Exception;
import cn.glogs.activeauth.iamcore.exception.business.NotFoundException;
import cn.glogs.activeauth.iamcore.service.AuthenticationPrincipalService;
import cn.glogs.activeauth.iamcore.service.AuthenticationSessionService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthenticationApi {

    private final AuthenticationPrincipalService authenticationPrincipalService;

    private final AuthenticationSessionService authenticationSessionService;

    public AuthenticationApi(AuthenticationPrincipalService authenticationPrincipalService, AuthenticationSessionService authenticationSessionService) {
        this.authenticationPrincipalService = authenticationPrincipalService;
        this.authenticationSessionService = authenticationSessionService;
    }

    @PostMapping("/authentication-principals")
    public RestResultPacker<AuthenticationPrincipal.Vo> addPrincipal(@RequestBody AuthenticationPrincipal.CreatePrincipalForm form) {
        return RestResultPacker.success(authenticationPrincipalService.addPrincipal(form.getName(), form.getPassword()).vo());
    }

    @PostMapping("/authentications/ticketing")
    public RestResultPacker<AuthenticationSession.Vo> authenticationTicketing(@RequestBody AuthenticationSession.CreateSessionForm form) throws HTTP401Exception, HTTP404Exception {
        try {
            return RestResultPacker.success(authenticationSessionService.newSession(form).vo());
        } catch (NotFoundException notFoundException) {
            throw new HTTP404Exception(notFoundException);
        } catch (AuthenticationPrincipal.PasswordNotMatchException passwordNotMatchException) {
            throw new HTTP401Exception(passwordNotMatchException);
        }
    }
}
