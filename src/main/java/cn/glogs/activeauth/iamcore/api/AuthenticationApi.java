package cn.glogs.activeauth.iamcore.api;

import cn.glogs.activeauth.iamcore.api.helper.AuthCheckingHelper;
import cn.glogs.activeauth.iamcore.api.payload.AuthCheckingStatement;
import cn.glogs.activeauth.iamcore.api.payload.AuthCheckingContext;
import cn.glogs.activeauth.iamcore.api.payload.RestResultPacker;
import cn.glogs.activeauth.iamcore.domain.AuthenticationPrincipal;
import cn.glogs.activeauth.iamcore.domain.AuthenticationPrincipalSecretKey;
import cn.glogs.activeauth.iamcore.domain.password.PasswordHashingStrategy;
import cn.glogs.activeauth.iamcore.exception.HTTP400Exception;
import cn.glogs.activeauth.iamcore.exception.HTTP401Exception;
import cn.glogs.activeauth.iamcore.exception.HTTP403Exception;
import cn.glogs.activeauth.iamcore.exception.HTTP404Exception;
import cn.glogs.activeauth.iamcore.exception.business.NotFoundException;
import cn.glogs.activeauth.iamcore.service.AuthenticationPrincipalSecretKeyService;
import cn.glogs.activeauth.iamcore.service.AuthenticationPrincipalService;
import org.springframework.data.domain.Page;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
public class AuthenticationApi {

    private final AuthenticationPrincipalService authenticationPrincipalService;

    private final AuthenticationPrincipalSecretKeyService authenticationPrincipalSecretKeyService;

    private final AuthCheckingHelper authCheckingHelper;

    public AuthenticationApi(AuthenticationPrincipalService authenticationPrincipalService, AuthenticationPrincipalSecretKeyService authenticationPrincipalSecretKeyService, AuthCheckingHelper authCheckingHelper) {
        this.authenticationPrincipalService = authenticationPrincipalService;
        this.authenticationPrincipalSecretKeyService = authenticationPrincipalSecretKeyService;
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

    @PostMapping("/principals/current/secret-keys")
    public RestResultPacker<AuthenticationPrincipalSecretKey.Vo> genKeyPair(HttpServletRequest request, @RequestBody AuthenticationPrincipalSecretKey.GenKeyPairForm form) throws HTTP400Exception, HTTP401Exception, HTTP403Exception {
        AuthCheckingContext authCheckingContext = authCheckingHelper.myResources(request, AuthCheckingStatement.checks("iam:GenerateKeyPair", "iam://users/%s/key-pairs"));
        return RestResultPacker.success(authenticationPrincipalSecretKeyService.generateKey(authCheckingContext.getCurrentSession().getAuthenticationPrincipal(), form).vo());
    }

    @GetMapping("/principals/current/secret-keys")
    public RestResultPacker<Page<AuthenticationPrincipalSecretKey.Vo>> pagingKeyPairs(HttpServletRequest request, @RequestParam int page, @RequestParam int size) throws HTTP400Exception, HTTP401Exception, HTTP403Exception {
        AuthCheckingContext authCheckingContext = authCheckingHelper.myResources(request, AuthCheckingStatement.checks("iam:GetKeyPair", "iam://users/%s/key-pairs"));
        Page<AuthenticationPrincipalSecretKey> keyPairPage = authenticationPrincipalSecretKeyService.pagingKeysOfOwner(authCheckingContext.getCurrentSession().getAuthenticationPrincipal(), page, size);
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

    @GetMapping("/principals/current/subprincipals/{subprincipalId}")
    public RestResultPacker<AuthenticationPrincipal.Vo> getSubprincipal(HttpServletRequest request, @PathVariable Long subprincipalId) throws HTTP403Exception, HTTP401Exception, HTTP400Exception, HTTP404Exception {
        AuthCheckingContext authCheckingContext = authCheckingHelper.myResources(request, AuthCheckingStatement.checks("iam:GetSubPrincipal", "iam://users/%s/subprincipals/" + subprincipalId));
        try {
            AuthenticationPrincipal subprincipalToFind = authenticationPrincipalService.findPrincipalById(subprincipalId);
            AuthenticationPrincipal challenger = authCheckingContext.getResourceOwner().getOwner();
            if (subprincipalToFind.getOwner() != null && !subprincipalToFind.getOwner().getId().equals(challenger.getId())) {
                throw new NotFoundException(String.format("Cannot find subprincipal %s of principal %s.", subprincipalId, challenger.getId()));
            }
            return RestResultPacker.success(subprincipalToFind.vo());
        } catch (NotFoundException e) {
            throw new HTTP404Exception(e);
        }
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
    public RestResultPacker<AuthenticationPrincipalSecretKey.Vo> genKeyPair(HttpServletRequest request, @PathVariable Long principalId, @RequestBody AuthenticationPrincipalSecretKey.GenKeyPairForm form) throws HTTP400Exception, HTTP401Exception, HTTP403Exception, HTTP404Exception {
        AuthCheckingContext authCheckingContext = authCheckingHelper.theirResources(request, AuthCheckingStatement.checks("iam:GenerateKeyPair", "iam://users/%s/key-pairs"), principalId);
        return RestResultPacker.success(authenticationPrincipalSecretKeyService.generateKey(authCheckingContext.getResourceOwner(), form).vo());
    }

    @GetMapping("/principals/{principalId}/key-pairs")
    public RestResultPacker<Page<AuthenticationPrincipalSecretKey.Vo>> pagingKeyPairs(HttpServletRequest request, @PathVariable Long principalId, @RequestParam int page, @RequestParam int size) throws HTTP400Exception, HTTP401Exception, HTTP403Exception, HTTP404Exception {
        AuthCheckingContext authCheckingContext = authCheckingHelper.theirResources(request, AuthCheckingStatement.checks("iam:GetKeyPair", "iam://users/%s/key-pairs"), principalId);
        Page<AuthenticationPrincipalSecretKey> keyPairPage = authenticationPrincipalSecretKeyService.pagingKeysOfOwner(authCheckingContext.getResourceOwner(), page, size);
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

    @GetMapping("/principals/{principalId}/subprincipals/{subprincipalId}")
    public RestResultPacker<AuthenticationPrincipal.Vo> getSubprincipal(HttpServletRequest request, @PathVariable Long principalId, @PathVariable Long subprincipalId) throws HTTP403Exception, HTTP401Exception, HTTP400Exception, HTTP404Exception {
        authCheckingHelper.theirResources(request, AuthCheckingStatement.checks("iam:GetSubPrincipal", "iam://users/%s/subprincipals/" + subprincipalId), principalId);
        try {
            AuthenticationPrincipal subprincipalToFind = authenticationPrincipalService.findPrincipalById(subprincipalId);
            if (subprincipalToFind.getOwner() != null && !subprincipalToFind.getOwner().getId().equals(principalId)) {
                throw new NotFoundException(String.format("Cannot find subprincipal %s of principal %s.", subprincipalId, principalId));
            }
            return RestResultPacker.success(subprincipalToFind.vo());
        } catch (NotFoundException e) {
            throw new HTTP404Exception(e);
        }
    }
}
