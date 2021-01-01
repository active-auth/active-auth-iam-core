package cn.glogs.activeauth.iamcore.api;

import cn.glogs.activeauth.iamcore.api.helper.AuthCheckingHelper;
import cn.glogs.activeauth.iamcore.api.payload.AuthCheckingStatement;
import cn.glogs.activeauth.iamcore.api.payload.AuthCheckingContext;
import cn.glogs.activeauth.iamcore.api.payload.RestResultPacker;
import cn.glogs.activeauth.iamcore.domain.AuthenticationPrincipal;
import cn.glogs.activeauth.iamcore.domain.AuthenticationPrincipalKeyPair;
import cn.glogs.activeauth.iamcore.domain.password.PasswordHashingStrategy;
import cn.glogs.activeauth.iamcore.exception.HTTP400Exception;
import cn.glogs.activeauth.iamcore.exception.HTTP401Exception;
import cn.glogs.activeauth.iamcore.exception.HTTP403Exception;
import cn.glogs.activeauth.iamcore.exception.HTTP404Exception;
import cn.glogs.activeauth.iamcore.exception.business.NotFoundException;
import cn.glogs.activeauth.iamcore.service.AuthenticationPrincipalKeyPairService;
import cn.glogs.activeauth.iamcore.service.AuthenticationPrincipalService;
import org.springframework.data.domain.Page;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
public class AuthenticationApi {

    private final AuthenticationPrincipalService authenticationPrincipalService;

    private final AuthenticationPrincipalKeyPairService authenticationPrincipalKeyPairService;

    private final AuthCheckingHelper authCheckingHelper;

    public AuthenticationApi(AuthenticationPrincipalService authenticationPrincipalService, AuthenticationPrincipalKeyPairService authenticationPrincipalKeyPairService, AuthCheckingHelper authCheckingHelper) {
        this.authenticationPrincipalService = authenticationPrincipalService;
        this.authenticationPrincipalKeyPairService = authenticationPrincipalKeyPairService;
        this.authCheckingHelper = authCheckingHelper;
    }

    @PostMapping("/principals")
    public RestResultPacker<AuthenticationPrincipal.Vo> addPrincipal(HttpServletRequest request, @RequestBody @Validated AuthenticationPrincipal.PrincipalForm form) throws HTTP401Exception, HTTP403Exception, HTTP400Exception {
        authCheckingHelper.systemResources(request, AuthCheckingStatement.checks("iam:CreatePrincipal", "iam://principals"));
        AuthenticationPrincipal toCreatePrincipal = AuthenticationPrincipal.createPrincipal(form.getName(), form.getPassword(), PasswordHashingStrategy.B_CRYPT, AuthenticationPrincipal.PrincipalType.PRINCIPAL);
        return RestResultPacker.success(authenticationPrincipalService.createPrincipal(toCreatePrincipal).vo());
    }

    @GetMapping("/principals")
    public RestResultPacker<Page<AuthenticationPrincipal.Vo>> pagingPrincipals(HttpServletRequest request, int page, int size) throws HTTP400Exception, HTTP401Exception, HTTP403Exception {
        authCheckingHelper.systemResources(request, AuthCheckingStatement.checks("iam:GetPrincipal", "iam://principals"));
        return RestResultPacker.success(authenticationPrincipalService.pagingPrincipals(page, size).map((AuthenticationPrincipal::vo)));
    }

    @GetMapping("/principals/current")
    public RestResultPacker<AuthenticationPrincipal.Vo> getCurrentPrincipal(HttpServletRequest request) throws HTTP400Exception, HTTP401Exception, HTTP403Exception {
        AuthCheckingContext authCheckingContext = authCheckingHelper.myResources(request, AuthCheckingStatement.checks("iam:GetPrincipal", "iam://users/%s/principal"));
        return RestResultPacker.success(authCheckingContext.getCurrentSession().getAuthenticationPrincipal().vo());
    }

    @PostMapping("/principals/current/key-pairs")
    public RestResultPacker<AuthenticationPrincipalKeyPair.Vo> genKeyPair(HttpServletRequest request, @RequestBody AuthenticationPrincipalKeyPair.GenKeyPairForm form) throws HTTP400Exception, HTTP401Exception, HTTP403Exception {
        AuthCheckingContext authCheckingContext = authCheckingHelper.myResources(request, AuthCheckingStatement.checks("iam:GenerateKeyPair", "iam://users/%s/key-pairs"));
        return RestResultPacker.success(authenticationPrincipalKeyPairService.genKey(authCheckingContext.getCurrentSession().getAuthenticationPrincipal(), form).vo());
    }

    @GetMapping("/principals/current/key-pairs")
    public RestResultPacker<Page<AuthenticationPrincipalKeyPair.Vo>> pagingKeyPairs(HttpServletRequest request, @RequestParam int page, @RequestParam int size) throws HTTP400Exception, HTTP401Exception, HTTP403Exception {
        AuthCheckingContext authCheckingContext = authCheckingHelper.myResources(request, AuthCheckingStatement.checks("iam:GetKeyPair", "iam://users/%s/key-pairs"));
        Page<AuthenticationPrincipalKeyPair> keyPairPage = authenticationPrincipalKeyPairService.pagingKeyPairs(authCheckingContext.getCurrentSession().getAuthenticationPrincipal(), page, size);
        return RestResultPacker.success(keyPairPage.map((keyPair) -> keyPair.vo().securePrivateKey()));
    }

    @PostMapping("/principals/current/subprincipals")
    public RestResultPacker<AuthenticationPrincipal.Vo> addSubprincipal(HttpServletRequest request, @RequestBody AuthenticationPrincipal.UserRegisterForm form) throws HTTP400Exception, HTTP401Exception, HTTP403Exception {
        AuthCheckingContext authCheckingContext = authCheckingHelper.myResources(request, AuthCheckingStatement.checks("iam:AddSubPrincipal", "iam://users/%s/subprincipals"));
        return RestResultPacker.success(authenticationPrincipalService.createSubprincipal(authCheckingContext.getCurrentSession().getAuthenticationPrincipal(), form.getName(), form.getPassword()).vo());
    }

    @GetMapping("/principals/current/subprincipals")
    public RestResultPacker<Page<AuthenticationPrincipal.Vo>> pagingSubPrincipals(HttpServletRequest request, @RequestParam int page, @RequestParam int size) throws HTTP400Exception, HTTP401Exception, HTTP403Exception {
        AuthCheckingContext authCheckingContext = authCheckingHelper.myResources(request, AuthCheckingStatement.checks("iam:GetSubPrincipal", "iam://users/%s/subprincipals"));
        return RestResultPacker.success(authenticationPrincipalService.pagingSubprincipals(authCheckingContext.getCurrentSession().getAuthenticationPrincipal(), page, size).map((AuthenticationPrincipal::vo)));
    }

    @GetMapping("/principals/{principalId}")
    public RestResultPacker<AuthenticationPrincipal.Vo> findPrincipalById(HttpServletRequest request, @PathVariable Long principalId) throws HTTP400Exception, HTTP401Exception, HTTP403Exception, HTTP404Exception {
        try {
            AuthenticationPrincipal principal = authenticationPrincipalService.findPrincipalById(principalId);
            authCheckingHelper.theirResources(request, AuthCheckingStatement.checks("iam:GetPrincipal", "iam://users/%s/principal"), principal);
            return RestResultPacker.success(principal.vo());
        } catch (NotFoundException e) {
            throw new HTTP404Exception(e);
        }
    }

    @PostMapping("/principals/{principalId}/key-pairs")
    public RestResultPacker<AuthenticationPrincipalKeyPair.Vo> genKeyPair(HttpServletRequest request, @PathVariable Long principalId, @RequestBody AuthenticationPrincipalKeyPair.GenKeyPairForm form) throws HTTP400Exception, HTTP401Exception, HTTP403Exception, HTTP404Exception {
        AuthCheckingContext authCheckingContext = authCheckingHelper.theirResources(request, AuthCheckingStatement.checks("iam:GenerateKeyPair", "iam://users/%s/key-pairs"), principalId);
        return RestResultPacker.success(authenticationPrincipalKeyPairService.genKey(authCheckingContext.getResourceOwner(), form).vo());
    }

    @GetMapping("/principals/{principalId}/key-pairs")
    public RestResultPacker<Page<AuthenticationPrincipalKeyPair.Vo>> pagingKeyPairs(HttpServletRequest request, @PathVariable Long principalId, @RequestParam int page, @RequestParam int size) throws HTTP400Exception, HTTP401Exception, HTTP403Exception, HTTP404Exception {
        AuthCheckingContext authCheckingContext = authCheckingHelper.theirResources(request, AuthCheckingStatement.checks("iam:GetKeyPair", "iam://users/%s/key-pairs"), principalId);
        Page<AuthenticationPrincipalKeyPair> keyPairPage = authenticationPrincipalKeyPairService.pagingKeyPairs(authCheckingContext.getResourceOwner(), page, size);
        return RestResultPacker.success(keyPairPage.map((keyPair) -> keyPair.vo().securePrivateKey()));
    }

    @PostMapping("/principals/{principalId}/subprincipals")
    public RestResultPacker<AuthenticationPrincipal.Vo> addSubprincipal(HttpServletRequest request, @PathVariable Long principalId, @RequestBody AuthenticationPrincipal.UserRegisterForm form) throws HTTP400Exception, HTTP401Exception, HTTP403Exception, HTTP404Exception {
        AuthCheckingContext authCheckingContext = authCheckingHelper.theirResources(request, AuthCheckingStatement.checks("iam:AddSubPrincipal", "iam://users/%s/subprincipals"), principalId);
        return RestResultPacker.success(authenticationPrincipalService.createSubprincipal(authCheckingContext.getResourceOwner(), form.getName(), form.getPassword()).vo());
    }

    @GetMapping("/principals/{principalId}/subprincipals")
    public RestResultPacker<Page<AuthenticationPrincipal.Vo>> pagingSubPrincipals(HttpServletRequest request, @PathVariable Long principalId, @RequestParam int page, @RequestParam int size) throws HTTP400Exception, HTTP401Exception, HTTP403Exception, HTTP404Exception {
        AuthCheckingContext authCheckingContext = authCheckingHelper.theirResources(request, AuthCheckingStatement.checks("iam:GetSubPrincipal", "iam://users/%s/subprincipals"), principalId);
        return RestResultPacker.success(authenticationPrincipalService.pagingSubprincipals(authCheckingContext.getResourceOwner(), page, size).map((AuthenticationPrincipal::vo)));
    }
}
