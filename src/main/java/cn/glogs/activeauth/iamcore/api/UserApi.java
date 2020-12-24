package cn.glogs.activeauth.iamcore.api;

import cn.glogs.activeauth.iamcore.api.payload.RestResultPacker;
import cn.glogs.activeauth.iamcore.domain.AuthenticationPrincipal;
import cn.glogs.activeauth.iamcore.domain.AuthenticationSession;
import cn.glogs.activeauth.iamcore.exception.HTTP400Exception;
import cn.glogs.activeauth.iamcore.exception.HTTP403Exception;
import cn.glogs.activeauth.iamcore.exception.HTTP404Exception;
import cn.glogs.activeauth.iamcore.exception.business.NotFoundException;
import cn.glogs.activeauth.iamcore.service.AuthenticationPrincipalService;
import cn.glogs.activeauth.iamcore.service.AuthenticationSessionService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
public class UserApi {

    private final AuthenticationPrincipalService authenticationPrincipalService;
    private final AuthenticationSessionService authenticationSessionService;

    public UserApi(
            AuthenticationPrincipalService authenticationPrincipalService,
            AuthenticationSessionService authenticationSessionService
    ) {
        this.authenticationPrincipalService = authenticationPrincipalService;
        this.authenticationSessionService = authenticationSessionService;
    }

    @GetMapping("/users/{id}")
    public RestResultPacker<AuthenticationPrincipal.Vo> findUserById(@PathVariable Long id) throws HTTP404Exception {
        try {
            return RestResultPacker.success(authenticationPrincipalService.findPrincipalById(id).vo());
        } catch (NotFoundException e) {
            throw new HTTP404Exception(e);
        }
    }

    @GetMapping("/users/current")
    public RestResultPacker<AuthenticationPrincipal.Vo> findCurrentUser(HttpServletRequest request) throws HTTP400Exception, HTTP403Exception {
        try {
            AuthenticationSession authenticationSession = authenticationSessionService.getMeSession(request);
            return RestResultPacker.success(authenticationSession.getAuthenticationPrincipal().vo().secureSecretKey());
        } catch (AuthenticationSession.SessionRequestBadHeaderException e) {
            throw new HTTP400Exception(e);
        } catch (AuthenticationSession.SessionNotFoundException e) {
            throw new HTTP403Exception(e);
        }
    }
}
