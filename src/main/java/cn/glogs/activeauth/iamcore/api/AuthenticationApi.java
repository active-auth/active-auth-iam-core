package cn.glogs.activeauth.iamcore.api;

import cn.glogs.activeauth.iamcore.api.helper.AuthCheckingHelper;
import cn.glogs.activeauth.iamcore.api.payload.AuthCheckingStatement;
import cn.glogs.activeauth.iamcore.api.payload.AuthCheckingContext;
import cn.glogs.activeauth.iamcore.api.payload.RestResultPacker;
import cn.glogs.activeauth.iamcore.config.properties.Configuration;
import cn.glogs.activeauth.iamcore.domain.AuthenticationPrincipal;
import cn.glogs.activeauth.iamcore.domain.AuthenticationPrincipalSecretKey;
import cn.glogs.activeauth.iamcore.exception.HTTP403Exception;
import cn.glogs.activeauth.iamcore.exception.HTTP404Exception;
import cn.glogs.activeauth.iamcore.exception.HTTPException;
import cn.glogs.activeauth.iamcore.exception.business.NotFoundException;
import cn.glogs.activeauth.iamcore.exception.business.SignatureException;
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

    private final Configuration configuration;

    public AuthenticationApi(
            AuthenticationPrincipalService authenticationPrincipalService,
            AuthenticationPrincipalSecretKeyService authenticationPrincipalSecretKeyService,
            AuthCheckingHelper authCheckingHelper,
            Configuration configuration
    ) {
        this.authenticationPrincipalService = authenticationPrincipalService;
        this.authenticationPrincipalSecretKeyService = authenticationPrincipalSecretKeyService;
        this.authCheckingHelper = authCheckingHelper;
        this.configuration = configuration;
    }

    @PostMapping("/principals")
    public RestResultPacker<AuthenticationPrincipal.Vo> addPrincipal(HttpServletRequest request, @RequestBody @Validated AuthenticationPrincipal.PrincipalForm form) throws HTTPException {
        authCheckingHelper.systemResources(request, AuthCheckingStatement.checks("iam:CreatePrincipal", "iam://principals"));
        AuthenticationPrincipal toCreatePrincipal = new AuthenticationPrincipal(
                form.getName(), form.getPassword(),
                form.isSessionCreatable(), form.isSignatureCreatable(),
                form.isSessionUsable(), form.isSignatureUsable(),
                AuthenticationPrincipal.PrincipalType.PRINCIPAL,
                configuration.getPasswordHashingStrategy()
        );
        return RestResultPacker.success(authenticationPrincipalService.createPrincipal(toCreatePrincipal).vo());
    }

    @GetMapping("/principals")
    public RestResultPacker<Page<AuthenticationPrincipal.Vo>> pagingPrincipals(HttpServletRequest request, int page, int size) throws HTTPException {
        authCheckingHelper.systemResources(request, AuthCheckingStatement.checks("iam:GetPrincipal", "iam://principals"));
        return RestResultPacker.success(authenticationPrincipalService.pagingPrincipals(page, size).map((AuthenticationPrincipal::vo)));
    }

    @GetMapping("/principals/current")
    public RestResultPacker<AuthenticationPrincipal.Vo> getCurrentPrincipal(HttpServletRequest request) throws HTTPException {
        AuthCheckingContext authCheckingContext = authCheckingHelper.myResources(request, AuthCheckingStatement.checks("iam:GetPrincipal", "iam://users/%s/principal"));
        return RestResultPacker.success(authCheckingContext.getCurrentSession().getAuthenticationPrincipal().vo());
    }

    @PostMapping("/principals/current/secret-keys/rsa2048-key-pairs")
    public RestResultPacker<AuthenticationPrincipalSecretKey.Vo> genSecretKey(HttpServletRequest request, @RequestBody AuthenticationPrincipalSecretKey.GenKeyPairForm form) throws HTTPException {
        AuthCheckingContext authCheckingContext = authCheckingHelper.myResources(request, AuthCheckingStatement.checks("iam:GenerateSecretKey", "iam://users/%s/secret-keys"));
        try {
            return RestResultPacker.success(authenticationPrincipalSecretKeyService.generateRSA2048KeyPair(authCheckingContext.getCurrentSession().getAuthenticationPrincipal(), form).vo());
        } catch (SignatureException e) {
            throw new HTTP403Exception(e);
        }
    }

    @GetMapping("/principals/current/secret-keys")
    public RestResultPacker<Page<AuthenticationPrincipalSecretKey.Vo>> pagingSecretKey(HttpServletRequest request, @RequestParam int page, @RequestParam int size) throws HTTPException {
        AuthCheckingContext authCheckingContext = authCheckingHelper.myResources(request, AuthCheckingStatement.checks("iam:GetSecretKey", "iam://users/%s/secret-keys"));
        Page<AuthenticationPrincipalSecretKey> keyPairPage = authenticationPrincipalSecretKeyService.pagingKeysOfOwner(authCheckingContext.getCurrentSession().getAuthenticationPrincipal(), page, size);
        return RestResultPacker.success(keyPairPage.map((keyPair) -> keyPair.vo().securePrivateKey()));
    }

    @DeleteMapping("/principals/current/secret-keys/{keyId}")
    public RestResultPacker<String> deleteKey(HttpServletRequest request, @PathVariable Long keyId) throws HTTPException {
        AuthCheckingContext authCheckingContext = authCheckingHelper.myResources(request, AuthCheckingStatement.checks("iam:DeleteSecretKey", "iam://users/%s/secret-keys"));
        try {
            AuthenticationPrincipalSecretKey secretKey = authenticationPrincipalSecretKeyService.getKeyById(keyId);
            if (authCheckingContext.belongToCurrentSession(secretKey.getPrincipal())) {
                authenticationPrincipalSecretKeyService.deleteKeyById(keyId);
                return RestResultPacker.success("Secret Key Deleted.");
            } else {
                throw new NotFoundException("SecretKey Not found for current user");
            }
        } catch (NotFoundException e) {
            throw new HTTP404Exception(e);
        }
    }

    @PostMapping("/principals/current/subprincipals")
    public RestResultPacker<AuthenticationPrincipal.Vo> addSubprincipal(HttpServletRequest request, @RequestBody AuthenticationPrincipal.PrincipalForm form) throws HTTPException {
        AuthCheckingContext authCheckingContext = authCheckingHelper.myResources(request, AuthCheckingStatement.checks("iam:AddSubprincipal", "iam://users/%s/subprincipals"));
        AuthenticationPrincipal toCreatePrincipal = new AuthenticationPrincipal(
                form.getName(), form.getPassword(),
                form.isSessionCreatable(), form.isSignatureCreatable(),
                form.isSessionUsable(), form.isSignatureUsable(),
                AuthenticationPrincipal.PrincipalType.PRINCIPAL,
                configuration.getPasswordHashingStrategy()
        );
        toCreatePrincipal.setOwner(authCheckingContext.getResourceOwner());
        return RestResultPacker.success(authenticationPrincipalService.createPrincipal(toCreatePrincipal).vo());
    }

    @GetMapping("/principals/current/subprincipals")
    public RestResultPacker<Page<AuthenticationPrincipal.Vo>> pagingSubprincipals(HttpServletRequest request, @RequestParam int page, @RequestParam int size) throws HTTPException {
        AuthCheckingContext authCheckingContext = authCheckingHelper.myResources(request, AuthCheckingStatement.checks("iam:GetSubprincipal", "iam://users/%s/subprincipals"));
        return RestResultPacker.success(authenticationPrincipalService.pagingSubprincipals(authCheckingContext.getCurrentSession().getAuthenticationPrincipal(), page, size).map((AuthenticationPrincipal::vo)));
    }

    @DeleteMapping("/principals/current/subprincipals/{subprincipalId}")
    public RestResultPacker<String> deleteSubprincipal(HttpServletRequest request, @PathVariable Long subprincipalId) throws HTTPException {
        AuthCheckingContext authCheckingContext = authCheckingHelper.myResources(request, AuthCheckingStatement.checks("iam:GetSubprincipal", "iam://users/%s/subprincipals"));
        try {
            AuthenticationPrincipal subprincipal = authenticationPrincipalService.findPrincipalById(subprincipalId);
            if (authCheckingContext.belongToCurrentSession(subprincipal.getOwner())) {
                if (subprincipal.getPrincipalType() == AuthenticationPrincipal.PrincipalType.PRINCIPAL) {
                    authenticationPrincipalService.deletePrincipalById(subprincipalId);
                    return RestResultPacker.success("Subprincipal deleted.");
                } else {
                    throw new NotFoundException("Principal " + subprincipalId + " is not a subprincipal.");
                }
            } else {
                throw new NotFoundException("Cannot find subprincipal " + subprincipalId + " of current principal.");
            }
        } catch (NotFoundException e) {
            throw new HTTP404Exception(e);
        }
    }

    @GetMapping("/principals/current/subprincipals/{subprincipalId}")
    public RestResultPacker<AuthenticationPrincipal.Vo> getSubprincipal(HttpServletRequest request, @PathVariable Long subprincipalId) throws HTTPException {
        AuthCheckingContext authCheckingContext = authCheckingHelper.myResources(request, AuthCheckingStatement.checks("iam:GetSubprincipal", "iam://users/%s/subprincipals/" + subprincipalId));
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
    public RestResultPacker<AuthenticationPrincipal.Vo> findPrincipalById(HttpServletRequest request, @PathVariable Long principalId) throws HTTPException {
        try {
            AuthenticationPrincipal principal = authenticationPrincipalService.findPrincipalById(principalId);
            authCheckingHelper.theirResources(request, AuthCheckingStatement.checks("iam:GetPrincipal", "iam://users/%s/principal"), principal);
            return RestResultPacker.success(principal.vo());
        } catch (NotFoundException e) {
            throw new HTTP404Exception(e);
        }
    }

    @DeleteMapping("/principals/{principalId}")
    public RestResultPacker<String> deletePrincipal(HttpServletRequest request, @PathVariable Long principalId) throws HTTPException {
        try {
            AuthenticationPrincipal principal = authenticationPrincipalService.findPrincipalById(principalId);
            AuthCheckingContext authCheckingContext = authCheckingHelper.theirResources(request, AuthCheckingStatement.checks("iam:DeletePrincipal", "iam://users/%s/principal"), principal);
            // if principal to delete is current principal
            if (authCheckingContext.belongToCurrentSession(principal)) {
                throw new HTTP403Exception("Cannot delete current logged in principal.");
            }
            authenticationPrincipalService.deletePrincipalById(principalId);
            return RestResultPacker.success("Deleted!");
        } catch (NotFoundException e) {
            throw new HTTP404Exception(e);
        }
    }

    @PostMapping("/principals/{principalId}/secret-keys/rsa2048-key-pairs")
    public RestResultPacker<AuthenticationPrincipalSecretKey.Vo> genSecretKey(HttpServletRequest request, @PathVariable Long principalId, @RequestBody AuthenticationPrincipalSecretKey.GenKeyPairForm form) throws HTTPException {
        AuthCheckingContext authCheckingContext = authCheckingHelper.theirResources(request, AuthCheckingStatement.checks("iam:GenerateSecretKey", "iam://users/%s/secret-keys"), principalId);
        try {
            return RestResultPacker.success(authenticationPrincipalSecretKeyService.generateRSA2048KeyPair(authCheckingContext.getResourceOwner(), form).vo());
        } catch (SignatureException e) {
            throw new HTTP403Exception(e);
        }
    }

    @GetMapping("/principals/{principalId}/secret-keys")
    public RestResultPacker<Page<AuthenticationPrincipalSecretKey.Vo>> pagingSecretKeys(HttpServletRequest request, @PathVariable Long principalId, @RequestParam int page, @RequestParam int size) throws HTTPException {
        AuthCheckingContext authCheckingContext = authCheckingHelper.theirResources(request, AuthCheckingStatement.checks("iam:GetSecretKey", "iam://users/%s/secret-keys"), principalId);
        Page<AuthenticationPrincipalSecretKey> keyPairPage = authenticationPrincipalSecretKeyService.pagingKeysOfOwner(authCheckingContext.getResourceOwner(), page, size);
        return RestResultPacker.success(keyPairPage.map((keyPair) -> keyPair.vo().securePrivateKey()));
    }

    @DeleteMapping("/principals/{principalId}/secret-keys/{keyId}")
    public RestResultPacker<String> deleteSecretKeys(HttpServletRequest request, @PathVariable Long principalId, @PathVariable Long keyId) throws HTTPException {
        AuthCheckingContext authCheckingContext = authCheckingHelper.theirResources(request, AuthCheckingStatement.checks("iam:DeleteSecretKey", "iam://users/%s/secret-keys/" + keyId), principalId);
        try {
            AuthenticationPrincipalSecretKey secretKey = authenticationPrincipalSecretKeyService.getKeyById(keyId);
            if (authCheckingContext.belongToResourceOwner(secretKey.getPrincipal())) {
                authenticationPrincipalSecretKeyService.deleteKeyById(keyId);
                return RestResultPacker.success("Secret Key Deleted.");
            } else {
                throw new NotFoundException("SecretKey Not found for current user");
            }
        } catch (NotFoundException e) {
            throw new HTTP404Exception(e);
        }
    }

    @PostMapping("/principals/{principalId}/subprincipals")
    public RestResultPacker<AuthenticationPrincipal.Vo> addSubprincipal(HttpServletRequest request, @PathVariable Long principalId, @RequestBody AuthenticationPrincipal.PrincipalForm form) throws HTTPException {
        AuthCheckingContext authCheckingContext = authCheckingHelper.theirResources(request, AuthCheckingStatement.checks("iam:AddSubprincipal", "iam://users/%s/subprincipals"), principalId);
        AuthenticationPrincipal toCreatePrincipal = new AuthenticationPrincipal(
                form.getName(), form.getPassword(),
                form.isSessionCreatable(), form.isSignatureCreatable(),
                form.isSessionUsable(), form.isSignatureUsable(),
                AuthenticationPrincipal.PrincipalType.PRINCIPAL,
                configuration.getPasswordHashingStrategy()
        );
        toCreatePrincipal.setOwner(authCheckingContext.getResourceOwner());
        return RestResultPacker.success(authenticationPrincipalService.createPrincipal(toCreatePrincipal).vo());
    }

    @GetMapping("/principals/{principalId}/subprincipals")
    public RestResultPacker<Page<AuthenticationPrincipal.Vo>> pagingSubprincipals(HttpServletRequest request, @PathVariable Long principalId, @RequestParam int page, @RequestParam int size) throws HTTPException {
        AuthCheckingContext authCheckingContext = authCheckingHelper.theirResources(request, AuthCheckingStatement.checks("iam:GetSubprincipal", "iam://users/%s/subprincipals"), principalId);
        return RestResultPacker.success(authenticationPrincipalService.pagingSubprincipals(authCheckingContext.getResourceOwner(), page, size).map((AuthenticationPrincipal::vo)));
    }

    @GetMapping("/principals/{principalId}/subprincipals/{subprincipalId}")
    public RestResultPacker<AuthenticationPrincipal.Vo> getSubprincipal(HttpServletRequest request, @PathVariable Long principalId, @PathVariable Long subprincipalId) throws HTTPException {
        authCheckingHelper.theirResources(request, AuthCheckingStatement.checks("iam:GetSubprincipal", "iam://users/%s/subprincipals/" + subprincipalId), principalId);
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

    @DeleteMapping("/principals/{principalId}/subprincipals/{subprincipalId}")
    public RestResultPacker<String> deleteSubprincipal(HttpServletRequest request, @PathVariable Long principalId, @PathVariable Long subprincipalId) throws HTTPException {
        AuthCheckingContext authCheckingContext = authCheckingHelper.theirResources(request, AuthCheckingStatement.checks("iam:DeleteSubprincipal", "iam://users/%s/subprincipals/" + subprincipalId), principalId);
        try {
            AuthenticationPrincipal subprincipal = authenticationPrincipalService.findPrincipalById(subprincipalId);
            if (authCheckingContext.belongToResourceOwner(subprincipal.getOwner())) {
                if (subprincipal.getPrincipalType() == AuthenticationPrincipal.PrincipalType.PRINCIPAL) {
                    authenticationPrincipalService.deletePrincipalById(subprincipalId);
                    return RestResultPacker.success("Subprincipal deleted.");
                } else {
                    throw new NotFoundException("Principal " + subprincipalId + " is not a subprincipal.");
                }
            } else {
                throw new NotFoundException("Cannot find subprincipal " + subprincipalId + " of current principal.");
            }
        } catch (NotFoundException e) {
            throw new HTTP404Exception(e);
        }
    }
}
