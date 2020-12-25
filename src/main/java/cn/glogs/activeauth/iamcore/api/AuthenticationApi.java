package cn.glogs.activeauth.iamcore.api;

import cn.glogs.activeauth.iamcore.api.payload.RestResultPacker;
import cn.glogs.activeauth.iamcore.domain.AuthenticationPrincipal;
import cn.glogs.activeauth.iamcore.domain.AuthenticationPrincipalKeyPair;
import cn.glogs.activeauth.iamcore.domain.AuthenticationSession;
import cn.glogs.activeauth.iamcore.exception.HTTP400Exception;
import cn.glogs.activeauth.iamcore.exception.HTTP401Exception;
import cn.glogs.activeauth.iamcore.exception.HTTP403Exception;
import cn.glogs.activeauth.iamcore.exception.HTTP404Exception;
import cn.glogs.activeauth.iamcore.exception.business.NotFoundException;
import cn.glogs.activeauth.iamcore.service.AuthenticationPrincipalKeyPairService;
import cn.glogs.activeauth.iamcore.service.AuthenticationPrincipalService;
import cn.glogs.activeauth.iamcore.service.AuthenticationSessionService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
public class AuthenticationApi {

    private final AuthenticationPrincipalService authenticationPrincipalService;

    private final AuthenticationPrincipalKeyPairService authenticationPrincipalKeyPairService;

    private final AuthenticationSessionService authenticationSessionService;

    public AuthenticationApi(AuthenticationPrincipalService authenticationPrincipalService, AuthenticationPrincipalKeyPairService authenticationPrincipalKeyPairService, AuthenticationSessionService authenticationSessionService) {
        this.authenticationPrincipalService = authenticationPrincipalService;
        this.authenticationPrincipalKeyPairService = authenticationPrincipalKeyPairService;
        this.authenticationSessionService = authenticationSessionService;
    }

    @PostMapping("/authentication-principals")
    public RestResultPacker<AuthenticationPrincipal.Vo> addPrincipal(@RequestBody @Validated AuthenticationPrincipal.CreatePrincipalForm form) {
        return RestResultPacker.success(authenticationPrincipalService.addPrincipal(form.getName(), form.getPassword()).vo());
    }

    @PostMapping("/authentications/ticketing")
    public RestResultPacker<AuthenticationSession.Vo> authenticationTicketing(@RequestBody @Validated AuthenticationSession.CreateSessionForm form) throws HTTP401Exception, HTTP404Exception {
        try {
            return RestResultPacker.success(authenticationSessionService.newSession(form).vo());
        } catch (NotFoundException notFoundException) {
            throw new HTTP404Exception(notFoundException);
        } catch (AuthenticationPrincipal.PasswordNotMatchException passwordNotMatchException) {
            throw new HTTP401Exception(passwordNotMatchException);
        }
    }

    @GetMapping("/authentication-principals/{id}")
    public RestResultPacker<AuthenticationPrincipal.Vo> findPrincipalById(@PathVariable Long id) throws HTTP404Exception {
        try {
            return RestResultPacker.success(authenticationPrincipalService.findPrincipalById(id).vo());
        } catch (NotFoundException e) {
            throw new HTTP404Exception(e);
        }
    }


    @PostMapping("/authentication-principals/current/key-pairs")
    public RestResultPacker<AuthenticationPrincipalKeyPair.Vo> genKeyPair(HttpServletRequest request, @RequestBody AuthenticationPrincipalKeyPair.GenKeyPairForm form) throws HTTP400Exception, HTTP403Exception {
        try {
            AuthenticationSession authenticationSession = authenticationSessionService.getMeSession(request);
            return RestResultPacker.success(authenticationPrincipalKeyPairService.genKey(authenticationSession.getAuthenticationPrincipal(), form).vo());
        } catch (AuthenticationSession.SessionRequestBadHeaderException e) {
            throw new HTTP400Exception(e);
        } catch (AuthenticationSession.SessionNotFoundException e) {
            throw new HTTP403Exception(e);
        }
    }


    @GetMapping("/authentication-principals/current")
    public RestResultPacker<AuthenticationPrincipal.Vo> findCurrentPrincipal(HttpServletRequest request) throws HTTP400Exception, HTTP403Exception {
        try {
            AuthenticationSession authenticationSession = authenticationSessionService.getMeSession(request);
            return RestResultPacker.success(authenticationSession.getAuthenticationPrincipal().vo());
        } catch (AuthenticationSession.SessionRequestBadHeaderException e) {
            throw new HTTP400Exception(e);
        } catch (AuthenticationSession.SessionNotFoundException e) {
            throw new HTTP403Exception(e);
        }
    }
}
