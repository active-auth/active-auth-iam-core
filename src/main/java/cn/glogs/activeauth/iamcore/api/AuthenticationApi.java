package cn.glogs.activeauth.iamcore.api;

import cn.glogs.activeauth.iamcore.api.payload.RestResultPacker;
import cn.glogs.activeauth.iamcore.domain.AuthenticationPrincipal;
import cn.glogs.activeauth.iamcore.domain.AuthenticationPrincipalKeyPair;
import cn.glogs.activeauth.iamcore.domain.AuthenticationSession;
import cn.glogs.activeauth.iamcore.exception.HTTP401Exception;
import cn.glogs.activeauth.iamcore.exception.HTTP403Exception;
import cn.glogs.activeauth.iamcore.exception.HTTP404Exception;
import cn.glogs.activeauth.iamcore.exception.business.NotFoundException;
import cn.glogs.activeauth.iamcore.service.AuthenticationPrincipalKeyPairService;
import cn.glogs.activeauth.iamcore.service.AuthenticationPrincipalService;
import cn.glogs.activeauth.iamcore.service.AuthenticationSessionService;
import cn.glogs.activeauth.iamcore.service.AuthorizationService;
import org.springframework.data.domain.Page;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
public class AuthenticationApi {

    private final AuthenticationPrincipalService authenticationPrincipalService;

    private final AuthenticationPrincipalKeyPairService authenticationPrincipalKeyPairService;

    private final AuthenticationSessionService authenticationSessionService;

    private final AuthorizationService authorizationService;

    public AuthenticationApi(AuthenticationPrincipalService authenticationPrincipalService, AuthenticationPrincipalKeyPairService authenticationPrincipalKeyPairService, AuthenticationSessionService authenticationSessionService, AuthorizationService authorizationService) {
        this.authenticationPrincipalService = authenticationPrincipalService;
        this.authenticationPrincipalKeyPairService = authenticationPrincipalKeyPairService;
        this.authenticationSessionService = authenticationSessionService;
        this.authorizationService = authorizationService;
    }

    @PostMapping("/principals/none/authentication-ticketings")
    public RestResultPacker<AuthenticationSession.Vo> authenticationTicketing(@RequestBody @Validated AuthenticationSession.CreateSessionForm form) throws HTTP401Exception, HTTP404Exception {
        try {
            return RestResultPacker.success(authenticationSessionService.newSession(form).vo());
        } catch (NotFoundException notFoundException) {
            throw new HTTP404Exception(notFoundException);
        } catch (AuthenticationPrincipal.PasswordNotMatchException passwordNotMatchException) {
            throw new HTTP401Exception(passwordNotMatchException);
        }
    }

    @GetMapping("/principals")
    public RestResultPacker<Page<AuthenticationPrincipal.Vo>> findPrincipalById(HttpServletRequest request, int page, int size) throws HTTP401Exception, HTTP403Exception {
        try {
            AuthenticationSession authenticationSession = authenticationSessionService.getMeSession(request);
            boolean accessible = authorizationService.challenge(authenticationSession.getAuthenticationPrincipal(), "iam:GetPrincipal", List.of("iam://users"));
            if (!accessible) {
                throw new HTTP403Exception("Inaccessible!");
            }
            return RestResultPacker.success(authenticationPrincipalService.pagingPrincipals(page, size).map((AuthenticationPrincipal::vo)));
        } catch (AuthenticationSession.SessionRequestNotAuthorizedException e) {
            throw new HTTP401Exception(e);
        } catch (AuthenticationSession.SessionNotFoundException e) {
            throw new HTTP403Exception(e);
        }
    }

    @PostMapping("/principals")
    public RestResultPacker<AuthenticationPrincipal.Vo> addPrincipal(@RequestBody @Validated AuthenticationPrincipal.CreatePrincipalForm form) {
        return RestResultPacker.success(authenticationPrincipalService.addPrincipal(form.getName(), form.getPassword()).vo());
    }

    @GetMapping("/principals/current")
    public RestResultPacker<AuthenticationPrincipal.Vo> findCurrentPrincipal(HttpServletRequest request) throws HTTP401Exception, HTTP403Exception {
        try {
            AuthenticationSession authenticationSession = authenticationSessionService.getMeSession(request);
            boolean accessible = authorizationService.challenge(authenticationSession.getAuthenticationPrincipal(), "iam:GetPrincipal", List.of(String.format("iam://users/%s/principal", authenticationSession.getAuthenticationPrincipal().getId())));
            if (!accessible) {
                throw new HTTP403Exception("Inaccessible!");
            }
            return RestResultPacker.success(authenticationSession.getAuthenticationPrincipal().vo());
        } catch (AuthenticationSession.SessionRequestNotAuthorizedException e) {
            throw new HTTP401Exception(e);
        } catch (AuthenticationSession.SessionNotFoundException e) {
            throw new HTTP403Exception(e);
        }
    }

    @PostMapping("/principals/current/key-pairs")
    public RestResultPacker<AuthenticationPrincipalKeyPair.Vo> genKeyPair(HttpServletRequest request, @RequestBody AuthenticationPrincipalKeyPair.GenKeyPairForm form) throws HTTP401Exception, HTTP403Exception {
        try {
            AuthenticationSession authenticationSession = authenticationSessionService.getMeSession(request);
            boolean accessible = authorizationService.challenge(authenticationSession.getAuthenticationPrincipal(), "iam:GenKeyPair", List.of(String.format("iam://users/%s/key-pairs", authenticationSession.getAuthenticationPrincipal().getId())));
            if (!accessible) {
                throw new HTTP403Exception("Inaccessible!");
            }
            return RestResultPacker.success(authenticationPrincipalKeyPairService.genKey(authenticationSession.getAuthenticationPrincipal(), form).vo());
        } catch (AuthenticationSession.SessionRequestNotAuthorizedException e) {
            throw new HTTP401Exception(e);
        } catch (AuthenticationSession.SessionNotFoundException e) {
            throw new HTTP403Exception(e);
        }
    }

    @GetMapping("/principals/current/key-pairs")
    public RestResultPacker<Page<AuthenticationPrincipalKeyPair.Vo>> pagingKeyPairs(HttpServletRequest request, @RequestParam int page, @RequestParam int size) throws HTTP401Exception, HTTP403Exception {
        try {
            AuthenticationSession authenticationSession = authenticationSessionService.getMeSession(request);
            boolean accessible = authorizationService.challenge(authenticationSession.getAuthenticationPrincipal(), "iam:GetKeyPair", List.of(String.format("iam://users/%s/key-pairs", authenticationSession.getAuthenticationPrincipal().getId())));
            if (!accessible) {
                throw new HTTP403Exception("Inaccessible!");
            }
            Page<AuthenticationPrincipalKeyPair> keyPairPage = authenticationPrincipalKeyPairService.pagingKeyPairs(authenticationSession.getAuthenticationPrincipal(), page, size);
            return RestResultPacker.success(keyPairPage.map((keyPair) -> keyPair.vo().securePrivateKey()));
        } catch (AuthenticationSession.SessionRequestNotAuthorizedException e) {
            throw new HTTP401Exception(e);
        } catch (AuthenticationSession.SessionNotFoundException e) {
            throw new HTTP403Exception(e);
        }
    }

    @PostMapping("/principals/current/subprincipals")
    public RestResultPacker<AuthenticationPrincipal.Vo> addSubprincipal(HttpServletRequest request, @RequestBody AuthenticationPrincipal.CreatePrincipalForm form) throws HTTP401Exception, HTTP403Exception {
        try {
            AuthenticationSession authenticationSession = authenticationSessionService.getMeSession(request);
            boolean accessible = authorizationService.challenge(authenticationSession.getAuthenticationPrincipal(), "iam:AddSubPrincipal", List.of(String.format("iam://users/%s/subprincipals", authenticationSession.getAuthenticationPrincipal().getId())));
            if (!accessible) {
                throw new HTTP403Exception("Inaccessible!");
            }
            return RestResultPacker.success(authenticationPrincipalService.addSubprincipal(authenticationSession.getAuthenticationPrincipal(), form.getName(), form.getPassword()).vo());
        } catch (AuthenticationSession.SessionRequestNotAuthorizedException e) {
            throw new HTTP401Exception(e);
        } catch (AuthenticationSession.SessionNotFoundException e) {
            throw new HTTP403Exception(e);
        }
    }

    @GetMapping("/principals/current/subprincipals")
    public RestResultPacker<Page<AuthenticationPrincipal.Vo>> pagingSubPrincipals(HttpServletRequest request, @RequestParam int page, @RequestParam int size) throws HTTP401Exception, HTTP403Exception {
        try {
            AuthenticationSession authenticationSession = authenticationSessionService.getMeSession(request);
            boolean accessible = authorizationService.challenge(authenticationSession.getAuthenticationPrincipal(), "iam:GetSubPrincipal", List.of(String.format("iam://users/%s/subprincipals", authenticationSession.getAuthenticationPrincipal().getId())));
            if (!accessible) {
                throw new HTTP403Exception("Inaccessible!");
            }
            return RestResultPacker.success(authenticationPrincipalService.pagingSubprincipals(authenticationSession.getAuthenticationPrincipal(), page, size).map((AuthenticationPrincipal::vo)));
        } catch (AuthenticationSession.SessionRequestNotAuthorizedException e) {
            throw new HTTP401Exception(e);
        } catch (AuthenticationSession.SessionNotFoundException e) {
            throw new HTTP403Exception(e);
        }
    }

    @GetMapping("/principals/{principalId}")
    public RestResultPacker<AuthenticationPrincipal.Vo> findPrincipalById(HttpServletRequest request, @PathVariable Long principalId) throws HTTP401Exception, HTTP403Exception, HTTP404Exception {
        try {
            AuthenticationSession authenticationSession = authenticationSessionService.getMeSession(request);
            boolean accessible = authorizationService.challenge(authenticationSession.getAuthenticationPrincipal(), "iam:GetPrincipal", List.of(String.format("iam://users/%s/principal", principalId)));
            if (!accessible) {
                throw new HTTP403Exception("Inaccessible!");
            }
            return RestResultPacker.success(authenticationPrincipalService.findPrincipalById(principalId).vo());
        } catch (AuthenticationSession.SessionRequestNotAuthorizedException e) {
            throw new HTTP401Exception(e);
        } catch (AuthenticationSession.SessionNotFoundException e) {
            throw new HTTP403Exception(e);
        } catch (NotFoundException e) {
            throw new HTTP404Exception(e);
        }
    }

    @PostMapping("/principals/{principalId}/key-pairs")
    public RestResultPacker<AuthenticationPrincipalKeyPair.Vo> genKeyPair(HttpServletRequest request, @PathVariable Long principalId, @RequestBody AuthenticationPrincipalKeyPair.GenKeyPairForm form) throws HTTP401Exception, HTTP403Exception, HTTP404Exception {
        try {
            AuthenticationSession authenticationSession = authenticationSessionService.getMeSession(request);
            boolean accessible = authorizationService.challenge(authenticationSession.getAuthenticationPrincipal(), "iam:GenKeyPair", List.of(String.format("iam://users/%s/key-pairs", principalId)));
            if (!accessible) {
                throw new HTTP403Exception("Inaccessible!");
            }
            AuthenticationPrincipal principal = authenticationPrincipalService.findPrincipalById(principalId);
            return RestResultPacker.success(authenticationPrincipalKeyPairService.genKey(principal, form).vo());
        } catch (AuthenticationSession.SessionRequestNotAuthorizedException e) {
            throw new HTTP401Exception(e);
        } catch (AuthenticationSession.SessionNotFoundException e) {
            throw new HTTP403Exception(e);
        } catch (NotFoundException e) {
            throw new HTTP404Exception(e);
        }
    }

    @GetMapping("/principals/{principalId}/key-pairs")
    public RestResultPacker<Page<AuthenticationPrincipalKeyPair.Vo>> pagingKeyPairs(HttpServletRequest request, @PathVariable Long principalId, @RequestParam int page, @RequestParam int size) throws HTTP401Exception, HTTP403Exception, HTTP404Exception {
        try {
            AuthenticationSession authenticationSession = authenticationSessionService.getMeSession(request);
            boolean accessible = authorizationService.challenge(authenticationSession.getAuthenticationPrincipal(), "iam:GetKeyPair", List.of(String.format("iam://users/%s/key-pairs", principalId)));
            if (!accessible) {
                throw new HTTP403Exception("Inaccessible!");
            }
            AuthenticationPrincipal principal = authenticationPrincipalService.findPrincipalById(principalId);
            Page<AuthenticationPrincipalKeyPair> keyPairPage = authenticationPrincipalKeyPairService.pagingKeyPairs(principal, page, size);
            return RestResultPacker.success(keyPairPage.map((keyPair) -> keyPair.vo().securePrivateKey()));
        } catch (AuthenticationSession.SessionRequestNotAuthorizedException e) {
            throw new HTTP401Exception(e);
        } catch (AuthenticationSession.SessionNotFoundException e) {
            throw new HTTP403Exception(e);
        } catch (NotFoundException e) {
            throw new HTTP404Exception(e);
        }
    }

    @PostMapping("/principals/{principalId}/subprincipals")
    public RestResultPacker<AuthenticationPrincipal.Vo> addSubprincipal(HttpServletRequest request, @PathVariable Long principalId, @RequestBody AuthenticationPrincipal.CreatePrincipalForm form) throws HTTP401Exception, HTTP403Exception, HTTP404Exception {
        try {
            AuthenticationSession authenticationSession = authenticationSessionService.getMeSession(request);
            AuthenticationPrincipal authenticationPrincipal = authenticationPrincipalService.findPrincipalById(principalId);
            boolean accessible = authorizationService.challenge(authenticationSession.getAuthenticationPrincipal(), "iam:AddSubPrincipal", List.of(String.format("iam://users/%s/subprincipals", principalId)));
            if (!accessible) {
                throw new HTTP403Exception("Inaccessible!");
            }
            return RestResultPacker.success(authenticationPrincipalService.addSubprincipal(authenticationPrincipal, form.getName(), form.getPassword()).vo());
        } catch (AuthenticationSession.SessionRequestNotAuthorizedException e) {
            throw new HTTP401Exception(e);
        } catch (AuthenticationSession.SessionNotFoundException e) {
            throw new HTTP403Exception(e);
        } catch (NotFoundException e) {
            throw new HTTP404Exception(e);
        }
    }

    @GetMapping("/principals/{principalId}/subprincipals")
    public RestResultPacker<Page<AuthenticationPrincipal.Vo>> pagingSubPrincipals(HttpServletRequest request, @PathVariable Long principalId, @RequestParam int page, @RequestParam int size) throws HTTP401Exception, HTTP403Exception, HTTP404Exception {
        try {
            AuthenticationSession authenticationSession = authenticationSessionService.getMeSession(request);
            AuthenticationPrincipal authenticationPrincipal = authenticationPrincipalService.findPrincipalById(principalId);
            boolean accessible = authorizationService.challenge(authenticationSession.getAuthenticationPrincipal(), "iam:GetSubPrincipal", List.of(String.format("iam://users/%s/subprincipals", principalId)));
            if (!accessible) {
                throw new HTTP403Exception("Inaccessible!");
            }
            return RestResultPacker.success(authenticationPrincipalService.pagingSubprincipals(authenticationPrincipal, page, size).map((AuthenticationPrincipal::vo)));
        } catch (AuthenticationSession.SessionRequestNotAuthorizedException e) {
            throw new HTTP401Exception(e);
        } catch (AuthenticationSession.SessionNotFoundException e) {
            throw new HTTP403Exception(e);
        } catch (NotFoundException e) {
            throw new HTTP404Exception(e);
        }
    }
}
