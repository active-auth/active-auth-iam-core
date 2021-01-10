package cn.glogs.activeauth.iamcore.api;

import cn.glogs.activeauth.iamcore.api.helper.AuthCheckingHelper;
import cn.glogs.activeauth.iamcore.api.payload.AuthCheckingStatement;
import cn.glogs.activeauth.iamcore.api.payload.AuthCheckingContext;
import cn.glogs.activeauth.iamcore.api.payload.RestResultPacker;
import cn.glogs.activeauth.iamcore.config.properties.AuthConfiguration;
import cn.glogs.activeauth.iamcore.config.properties.LocatorConfiguration;
import cn.glogs.activeauth.iamcore.domain.AuthenticationPrincipal;
import cn.glogs.activeauth.iamcore.domain.AuthenticationPrincipalSecretKey;
import cn.glogs.activeauth.iamcore.exception.HTTP403Exception;
import cn.glogs.activeauth.iamcore.exception.HTTP404Exception;
import cn.glogs.activeauth.iamcore.exception.HTTPException;
import cn.glogs.activeauth.iamcore.exception.business.NotFoundException;
import cn.glogs.activeauth.iamcore.exception.business.SignatureException;
import cn.glogs.activeauth.iamcore.service.AuthenticationPrincipalSecretKeyService;
import cn.glogs.activeauth.iamcore.service.AuthenticationPrincipalService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.data.domain.Page;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
public class AuthenticationApi {

    private final AuthenticationPrincipalService authenticationPrincipalService;

    private final AuthenticationPrincipalSecretKeyService authenticationPrincipalSecretKeyService;

    private final AuthCheckingHelper authCheckingHelper;

    private final AuthConfiguration authConfiguration;

    private final LocatorConfiguration locatorConfiguration;

    public AuthenticationApi(
            AuthenticationPrincipalService authenticationPrincipalService,
            AuthenticationPrincipalSecretKeyService authenticationPrincipalSecretKeyService,
            AuthCheckingHelper authCheckingHelper,
            AuthConfiguration authConfiguration,
            LocatorConfiguration locatorConfiguration
    ) {
        this.authenticationPrincipalService = authenticationPrincipalService;
        this.authenticationPrincipalSecretKeyService = authenticationPrincipalSecretKeyService;
        this.authCheckingHelper = authCheckingHelper;
        this.authConfiguration = authConfiguration;
        this.locatorConfiguration = locatorConfiguration;
    }

    @Operation(tags = {"authentication-principal"})
    @PostMapping("/principals")
    public RestResultPacker<AuthenticationPrincipal.Vo> addPrincipal(HttpServletRequest request, @RequestBody @Validated AuthenticationPrincipal.PrincipalForm form) throws HTTPException {
        authCheckingHelper.systemResources(request, AuthCheckingStatement.checks("iam:CreatePrincipal", locatorConfiguration.fullLocator("", "principals")));
        AuthenticationPrincipal toCreatePrincipal = new AuthenticationPrincipal(
                form.getName(), form.getPassword(),
                form.isSessionCreatable(), form.isSignatureCreatable(),
                form.isSessionUsable(), form.isSignatureUsable(),
                AuthenticationPrincipal.PrincipalType.PRINCIPAL,
                authConfiguration.getPasswordHashingStrategy()
        );
        return RestResultPacker.success(authenticationPrincipalService.createPrincipal(toCreatePrincipal).vo(locatorConfiguration));
    }

    @Operation(tags = {"authentication-principal"})
    @GetMapping("/principals")
    public RestResultPacker<Page<AuthenticationPrincipal.Vo>> pagingPrincipals(HttpServletRequest request, int page, int size) throws HTTPException {
        authCheckingHelper.systemResources(request, AuthCheckingStatement.checks("iam:GetPrincipal", locatorConfiguration.fullLocator("", "principals")));
        return RestResultPacker.success(authenticationPrincipalService.pagingPrincipals(page, size).map(owner -> owner.vo(locatorConfiguration)));
    }

    @Operation(tags = {"authentication-principal"})
    @GetMapping("/principals/current")
    public RestResultPacker<AuthenticationPrincipal.Vo> getCurrentPrincipal(HttpServletRequest request) throws HTTPException {
        AuthCheckingContext authCheckingContext = authCheckingHelper.myResources(request, AuthCheckingStatement.checks("iam:GetPrincipal", locatorConfiguration.fullLocator("%s", "principal")));
        return RestResultPacker.success(authCheckingContext.getCurrentSession().getAuthenticationPrincipal().vo(locatorConfiguration));
    }

    @Operation(tags = {"authentication-secret-key"})
    @PostMapping("/principals/current/secret-keys/rsa2048-key-pairs")
    public RestResultPacker<AuthenticationPrincipalSecretKey.Vo> genSecretKey(HttpServletRequest request, @RequestBody AuthenticationPrincipalSecretKey.GenKeyPairForm form) throws HTTPException {
        AuthCheckingContext authCheckingContext = authCheckingHelper.myResources(
                request, AuthCheckingStatement.checks(
                        "iam:GenerateSecretKey", locatorConfiguration.fullLocator("%s", "secret-key")
                ));
        try {
            return RestResultPacker.success(authenticationPrincipalSecretKeyService.generateRSA2048KeyPair(authCheckingContext.getCurrentSession().getAuthenticationPrincipal(), form).vo(locatorConfiguration));
        } catch (SignatureException e) {
            throw new HTTP403Exception(e);
        }
    }

    @Operation(tags = {"authentication-secret-key"})
    @GetMapping("/principals/current/secret-keys")
    public RestResultPacker<Page<AuthenticationPrincipalSecretKey.Vo>> pagingSecretKey(HttpServletRequest request, @RequestParam int page, @RequestParam int size) throws HTTPException {
        AuthCheckingContext authCheckingContext = authCheckingHelper.myResources(request, AuthCheckingStatement.checks("iam:GetSecretKey", locatorConfiguration.fullLocator("%s", "secret-key")));
        Page<AuthenticationPrincipalSecretKey> keyPairPage = authenticationPrincipalSecretKeyService.pagingKeysOfOwner(authCheckingContext.getCurrentSession().getAuthenticationPrincipal(), page, size);
        return RestResultPacker.success(keyPairPage.map((keyPair) -> keyPair.vo(locatorConfiguration).securePrivateKey()));
    }

    @Operation(tags = {"authentication-secret-key"})
    @DeleteMapping("/principals/current/secret-keys/{keyId}")
    public RestResultPacker<String> deleteKey(HttpServletRequest request, @PathVariable Long keyId) throws HTTPException {
        AuthCheckingContext authCheckingContext = authCheckingHelper.myResources(request, AuthCheckingStatement.checks("iam:DeleteSecretKey", locatorConfiguration.fullLocator("%s", "secret-key")));
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

    @Operation(tags = {"authentication-subprincipal"})
    @PostMapping("/principals/current/subprincipals")
    public RestResultPacker<AuthenticationPrincipal.Vo> addSubprincipal(HttpServletRequest request, @RequestBody AuthenticationPrincipal.PrincipalForm form) throws HTTPException {
        AuthCheckingContext authCheckingContext = authCheckingHelper.myResources(request, AuthCheckingStatement.checks("iam:AddSubprincipal", locatorConfiguration.fullLocator("%s", "subprincipal")));
        AuthenticationPrincipal toCreatePrincipal = new AuthenticationPrincipal(
                form.getName(), form.getPassword(),
                form.isSessionCreatable(), form.isSignatureCreatable(),
                form.isSessionUsable(), form.isSignatureUsable(),
                AuthenticationPrincipal.PrincipalType.PRINCIPAL,
                authConfiguration.getPasswordHashingStrategy()
        );
        toCreatePrincipal.setOwner(authCheckingContext.getResourceOwner());
        return RestResultPacker.success(authenticationPrincipalService.createPrincipal(toCreatePrincipal).vo(locatorConfiguration));
    }

    @Operation(tags = {"authentication-subprincipal"})
    @GetMapping("/principals/current/subprincipals")
    public RestResultPacker<Page<AuthenticationPrincipal.Vo>> pagingSubprincipals(HttpServletRequest request, @RequestParam int page, @RequestParam int size) throws HTTPException {
        AuthCheckingContext authCheckingContext = authCheckingHelper.myResources(request, AuthCheckingStatement.checks("iam:GetSubprincipal", locatorConfiguration.fullLocator("%s", "subprincipal")));
        return RestResultPacker.success(authenticationPrincipalService.pagingSubprincipals(authCheckingContext.getCurrentSession().getAuthenticationPrincipal(), page, size).map(owner -> owner.vo(locatorConfiguration)));
    }

    @Operation(tags = {"authentication-subprincipal"})
    @DeleteMapping("/principals/current/subprincipals/{subprincipalId}")
    public RestResultPacker<String> deleteSubprincipal(HttpServletRequest request, @PathVariable Long subprincipalId) throws HTTPException {
        AuthCheckingContext authCheckingContext = authCheckingHelper.myResources(request, AuthCheckingStatement.checks("iam:GetSubprincipal", locatorConfiguration.fullLocator("%s", "subprincipal")));
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

    @Operation(tags = {"authentication-subprincipal"})
    @GetMapping("/principals/current/subprincipals/{subprincipalId}")
    public RestResultPacker<AuthenticationPrincipal.Vo> getSubprincipal(HttpServletRequest request, @PathVariable Long subprincipalId) throws HTTPException {
        AuthCheckingContext authCheckingContext = authCheckingHelper.myResources(request, AuthCheckingStatement.checks("iam:GetSubprincipal", locatorConfiguration.fullLocator("%s", "subprincipal", subprincipalId.toString())));
        try {
            AuthenticationPrincipal subprincipalToFind = authenticationPrincipalService.findPrincipalById(subprincipalId);
            AuthenticationPrincipal challenger = authCheckingContext.getResourceOwner().getOwner();
            if (subprincipalToFind.getOwner() != null && !subprincipalToFind.getOwner().getId().equals(challenger.getId())) {
                throw new NotFoundException(String.format("Cannot find subprincipal %s of principal %s.", subprincipalId, challenger.getId()));
            }
            return RestResultPacker.success(subprincipalToFind.vo(locatorConfiguration));
        } catch (NotFoundException e) {
            throw new HTTP404Exception(e);
        }
    }

    @Operation(tags = {"authentication-principal"})
    @GetMapping("/principals/{principalId}")
    public RestResultPacker<AuthenticationPrincipal.Vo> findPrincipalById(HttpServletRequest request, @PathVariable Long principalId) throws HTTPException {
        try {
            AuthenticationPrincipal principal = authenticationPrincipalService.findPrincipalById(principalId);
            authCheckingHelper.theirResources(request, AuthCheckingStatement.checks("iam:GetPrincipal", locatorConfiguration.fullLocator("%s", "principal")), principal);
            return RestResultPacker.success(principal.vo(locatorConfiguration));
        } catch (NotFoundException e) {
            throw new HTTP404Exception(e);
        }
    }

    @Operation(tags = {"authentication-principal"})
    @DeleteMapping("/principals/{principalId}")
    public RestResultPacker<String> deletePrincipal(HttpServletRequest request, @PathVariable Long principalId) throws HTTPException {
        try {
            AuthenticationPrincipal principal = authenticationPrincipalService.findPrincipalById(principalId);
            AuthCheckingContext authCheckingContext = authCheckingHelper.theirResources(request, AuthCheckingStatement.checks("iam:DeletePrincipal", locatorConfiguration.fullLocator("%s", "principal")), principal);
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

    @Operation(tags = {"authentication-secret-key"})
    @PostMapping("/principals/{principalId}/secret-keys/rsa2048-key-pairs")
    public RestResultPacker<AuthenticationPrincipalSecretKey.Vo> genSecretKey(HttpServletRequest request, @PathVariable Long principalId, @RequestBody AuthenticationPrincipalSecretKey.GenKeyPairForm form) throws HTTPException {
        AuthCheckingContext authCheckingContext = authCheckingHelper.theirResources(request, AuthCheckingStatement.checks("iam:GenerateSecretKey", locatorConfiguration.fullLocator("%s", "secret-key")), principalId);
        try {
            return RestResultPacker.success(authenticationPrincipalSecretKeyService.generateRSA2048KeyPair(authCheckingContext.getResourceOwner(), form).vo(locatorConfiguration));
        } catch (SignatureException e) {
            throw new HTTP403Exception(e);
        }
    }

    @Operation(tags = {"authentication-secret-key"})
    @GetMapping("/principals/{principalId}/secret-keys")
    public RestResultPacker<Page<AuthenticationPrincipalSecretKey.Vo>> pagingSecretKeys(HttpServletRequest request, @PathVariable Long principalId, @RequestParam int page, @RequestParam int size) throws HTTPException {
        AuthCheckingContext authCheckingContext = authCheckingHelper.theirResources(request, AuthCheckingStatement.checks("iam:GetSecretKey", locatorConfiguration.fullLocator("%s", "secret-key")), principalId);
        Page<AuthenticationPrincipalSecretKey> keyPairPage = authenticationPrincipalSecretKeyService.pagingKeysOfOwner(authCheckingContext.getResourceOwner(), page, size);
        return RestResultPacker.success(keyPairPage.map((keyPair) -> keyPair.vo(locatorConfiguration).securePrivateKey()));
    }

    @Operation(tags = {"authentication-secret-key"})
    @DeleteMapping("/principals/{principalId}/secret-keys/{keyId}")
    public RestResultPacker<String> deleteSecretKeys(HttpServletRequest request, @PathVariable Long principalId, @PathVariable Long keyId) throws HTTPException {
        AuthCheckingContext authCheckingContext = authCheckingHelper.theirResources(request, AuthCheckingStatement.checks("iam:DeleteSecretKey", locatorConfiguration.fullLocator("%s", "secret-key", keyId.toString())), principalId);
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

    @Operation(tags = {"authentication-subprincipal"})
    @PostMapping("/principals/{principalId}/subprincipals")
    public RestResultPacker<AuthenticationPrincipal.Vo> addSubprincipal(HttpServletRequest request, @PathVariable Long principalId, @RequestBody AuthenticationPrincipal.PrincipalForm form) throws HTTPException {
        AuthCheckingContext authCheckingContext = authCheckingHelper.theirResources(request, AuthCheckingStatement.checks("iam:AddSubprincipal", locatorConfiguration.fullLocator("%s", "subprincipal")), principalId);
        AuthenticationPrincipal toCreatePrincipal = new AuthenticationPrincipal(
                form.getName(), form.getPassword(),
                form.isSessionCreatable(), form.isSignatureCreatable(),
                form.isSessionUsable(), form.isSignatureUsable(),
                AuthenticationPrincipal.PrincipalType.PRINCIPAL,
                authConfiguration.getPasswordHashingStrategy()
        );
        toCreatePrincipal.setOwner(authCheckingContext.getResourceOwner());
        return RestResultPacker.success(authenticationPrincipalService.createPrincipal(toCreatePrincipal).vo(locatorConfiguration));
    }

    @Operation(tags = {"authentication-subprincipal"})
    @GetMapping("/principals/{principalId}/subprincipals")
    public RestResultPacker<Page<AuthenticationPrincipal.Vo>> pagingSubprincipals(HttpServletRequest request, @PathVariable Long principalId, @RequestParam int page, @RequestParam int size) throws HTTPException {
        AuthCheckingContext authCheckingContext = authCheckingHelper.theirResources(request, AuthCheckingStatement.checks("iam:GetSubprincipal", locatorConfiguration.fullLocator("%s", "subprincipal")), principalId);
        return RestResultPacker.success(authenticationPrincipalService.pagingSubprincipals(authCheckingContext.getResourceOwner(), page, size).map(owner -> owner.vo(locatorConfiguration)));
    }

    @Operation(tags = {"authentication-subprincipal"})
    @GetMapping("/principals/{principalId}/subprincipals/{subprincipalId}")
    public RestResultPacker<AuthenticationPrincipal.Vo> getSubprincipal(HttpServletRequest request, @PathVariable Long principalId, @PathVariable Long subprincipalId) throws HTTPException {
        authCheckingHelper.theirResources(request, AuthCheckingStatement.checks("iam:GetSubprincipal", locatorConfiguration.fullLocator("%s", "subprincipal", subprincipalId.toString())), principalId);
        try {
            AuthenticationPrincipal subprincipalToFind = authenticationPrincipalService.findPrincipalById(subprincipalId);
            if (subprincipalToFind.getOwner() != null && !subprincipalToFind.getOwner().getId().equals(principalId) && subprincipalToFind.typeIs(AuthenticationPrincipal.PrincipalType.PRINCIPAL)) {
                throw new NotFoundException(String.format("Cannot find subprincipal %s of principal %s.", subprincipalId, principalId));
            }
            return RestResultPacker.success(subprincipalToFind.vo(locatorConfiguration));
        } catch (NotFoundException e) {
            throw new HTTP404Exception(e);
        }
    }

    @Operation(tags = {"authentication-subprincipal"})
    @DeleteMapping("/principals/{principalId}/subprincipals/{subprincipalId}")
    public RestResultPacker<String> deleteSubprincipal(HttpServletRequest request, @PathVariable Long principalId, @PathVariable Long subprincipalId) throws HTTPException {
        AuthCheckingContext authCheckingContext = authCheckingHelper.theirResources(request, AuthCheckingStatement.checks("iam:DeleteSubprincipal", locatorConfiguration.fullLocator("%s", "subprincipal", subprincipalId.toString())), principalId);
        try {
            AuthenticationPrincipal subprincipal = authenticationPrincipalService.findPrincipalById(subprincipalId);
            if (authCheckingContext.belongToResourceOwner(subprincipal.getOwner())) {
                if (subprincipal.typeIs(AuthenticationPrincipal.PrincipalType.PRINCIPAL)) {
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

    @Operation(tags = {"authentication-principal-group"})
    @PostMapping("/principals/current/principal-groups")
    public RestResultPacker<AuthenticationPrincipal.Vo> addPrincipalGroup(HttpServletRequest request, @RequestBody AuthenticationPrincipal.PrincipalGroupForm form) throws HTTPException {
        AuthCheckingContext authCheckingContext = authCheckingHelper.myResources(request, AuthCheckingStatement.checks("iam:AddPrincipalGroup", locatorConfiguration.fullLocator("%s", "principal-group")));
        AuthenticationPrincipal toCreatePrincipal = new AuthenticationPrincipal(
                form.getName(), "NO-PASSWORD",
                false, false, false, false,
                AuthenticationPrincipal.PrincipalType.PRINCIPAL_GROUP,
                authConfiguration.getPasswordHashingStrategy()
        );
        toCreatePrincipal.setOwner(authCheckingContext.getResourceOwner());
        return RestResultPacker.success(authenticationPrincipalService.createPrincipal(toCreatePrincipal).vo(locatorConfiguration));
    }

    @Operation(tags = {"authentication-principal-group"})
    @PostMapping("/principals/{principalId}/principal-groups")
    public RestResultPacker<AuthenticationPrincipal.Vo> addPrincipalGroup(HttpServletRequest request, @PathVariable Long principalId, @RequestBody AuthenticationPrincipal.PrincipalGroupForm form) throws HTTPException {
        AuthCheckingContext authCheckingContext = authCheckingHelper.theirResources(request, AuthCheckingStatement.checks("iam:AddPrincipalGroup", locatorConfiguration.fullLocator("%s", "principal-group")), principalId);
        AuthenticationPrincipal toCreatePrincipal = new AuthenticationPrincipal(
                form.getName(), "NO-PASSWORD",
                false, false, false, false,
                AuthenticationPrincipal.PrincipalType.PRINCIPAL_GROUP,
                authConfiguration.getPasswordHashingStrategy()
        );
        toCreatePrincipal.setOwner(authCheckingContext.getResourceOwner());
        return RestResultPacker.success(authenticationPrincipalService.createPrincipal(toCreatePrincipal).vo(locatorConfiguration));
    }

    @Operation(tags = {"authentication-principal-group"})
    @GetMapping("/principals/current/principal-groups")
    public RestResultPacker<Page<AuthenticationPrincipal.Vo>> pagingPrincipalGroups(HttpServletRequest request, @RequestParam int page, @RequestParam int size) throws HTTPException {
        AuthCheckingContext authCheckingContext = authCheckingHelper.myResources(request, AuthCheckingStatement.checks("iam:GetPrincipalGroup", locatorConfiguration.fullLocator("%s", "principal-group")));
        return RestResultPacker.success(authenticationPrincipalService.pagingPrincipalGroups(authCheckingContext.getResourceOwner(), page, size).map(owner -> owner.vo(locatorConfiguration)));
    }

    @Operation(tags = {"authentication-principal-group"})
    @GetMapping("/principals/{principalId}/principal-groups")
    public RestResultPacker<Page<AuthenticationPrincipal.Vo>> pagingPrincipalGroups(HttpServletRequest request, @PathVariable Long principalId, @RequestParam int page, @RequestParam int size) throws HTTPException {
        AuthCheckingContext authCheckingContext = authCheckingHelper.theirResources(request, AuthCheckingStatement.checks("iam:GetPrincipalGroup", locatorConfiguration.fullLocator("%s", "principal-group")), principalId);
        return RestResultPacker.success(authenticationPrincipalService.pagingPrincipalGroups(authCheckingContext.getResourceOwner(), page, size).map(owner -> owner.vo(locatorConfiguration)));
    }

    @Operation(tags = {"authentication-principal-group"})
    @GetMapping("/principals/current/principal-groups/{principalGroupId}")
    public RestResultPacker<AuthenticationPrincipal.Vo> getPrincipalGroup(HttpServletRequest request, @PathVariable Long principalGroupId) throws HTTPException {
        AuthCheckingContext authCheckingContext = authCheckingHelper.myResources(request, AuthCheckingStatement.checks("iam:GetPrincipalGroup", locatorConfiguration.fullLocator("%s", "principal-group", principalGroupId.toString())));
        try {
            AuthenticationPrincipal principalGroupToFind = authenticationPrincipalService.findPrincipalById(principalGroupId);
            if (principalGroupToFind.getOwner() != null && !principalGroupToFind.typeIs(AuthenticationPrincipal.PrincipalType.PRINCIPAL_GROUP)) {
                throw new NotFoundException(String.format("Cannot find principal group %s of principal %s.", principalGroupId, authCheckingContext.getResourceOwner().getId()));
            }
            return RestResultPacker.success(principalGroupToFind.vo(locatorConfiguration));
        } catch (NotFoundException e) {
            throw new HTTP404Exception(e);
        }
    }

    @Operation(tags = {"authentication-principal-group"})
    @GetMapping("/principals/{principalId}/principal-groups/{principalGroupId}")
    public RestResultPacker<AuthenticationPrincipal.Vo> getPrincipalGroup(HttpServletRequest request, @PathVariable Long principalId, @PathVariable Long principalGroupId) throws HTTPException {
        authCheckingHelper.theirResources(request, AuthCheckingStatement.checks("iam:GetPrincipalGroup", locatorConfiguration.fullLocator("%s", "principal-group", principalGroupId.toString())), principalId);
        try {
            AuthenticationPrincipal principalGroupToFind = authenticationPrincipalService.findPrincipalById(principalGroupId);
            if (principalGroupToFind.getOwner() != null && !principalGroupToFind.getOwner().getId().equals(principalId) && !principalGroupToFind.typeIs(AuthenticationPrincipal.PrincipalType.PRINCIPAL_GROUP)) {
                throw new NotFoundException(String.format("Cannot find principal group %s of principal %s.", principalGroupId, principalId));
            }
            return RestResultPacker.success(principalGroupToFind.vo(locatorConfiguration));
        } catch (NotFoundException e) {
            throw new HTTP404Exception(e);
        }
    }

    private void deletePrincipalGroup(AuthCheckingContext authCheckingContext, Long principalGroupId) throws HTTP404Exception {
        try {
            AuthenticationPrincipal subprincipal = authenticationPrincipalService.findPrincipalById(principalGroupId);
            if (authCheckingContext.belongToResourceOwner(subprincipal.getOwner())) {
                if (subprincipal.typeIs(AuthenticationPrincipal.PrincipalType.PRINCIPAL)) {
                    authenticationPrincipalService.deletePrincipalById(principalGroupId);
                } else {
                    throw new NotFoundException("Principal " + principalGroupId + " is not a principal group.");
                }
            } else {
                throw new NotFoundException("Cannot find principal group " + principalGroupId + " of current principal.");
            }
        } catch (NotFoundException e) {
            throw new HTTP404Exception(e);
        }
    }

    @Operation(tags = {"authentication-principal-group"})
    @DeleteMapping("/principals/current/principal-groups/{principalGroupId}")
    public RestResultPacker<String> deletePrincipalGroup(HttpServletRequest request, @PathVariable Long principalGroupId) throws HTTPException {
        AuthCheckingContext authCheckingContext = authCheckingHelper.myResources(request, AuthCheckingStatement.checks("iam:DeletePrincipalGroup", locatorConfiguration.fullLocator("%s", "principal-group", principalGroupId.toString())));
        deletePrincipalGroup(authCheckingContext, principalGroupId);
        return RestResultPacker.success("Principal group deleted.");
    }

    @Operation(tags = {"authentication-principal-group"})
    @DeleteMapping("/principals/{principalId}/principal-groups/{principalGroupId}")
    public RestResultPacker<String> deletePrincipalGroup(HttpServletRequest request, @PathVariable Long principalId, @PathVariable Long principalGroupId) throws HTTPException {
        AuthCheckingContext authCheckingContext = authCheckingHelper.theirResources(request, AuthCheckingStatement.checks("iam:DeletePrincipalGroup", locatorConfiguration.fullLocator("%s", "principal-group", principalGroupId.toString())), principalId);
        deletePrincipalGroup(authCheckingContext, principalGroupId);
        return RestResultPacker.success("Principal group deleted.");
    }

    @Operation(tags = {"authentication-app-domain"})
    @PostMapping("/principals/current/app-domains")
    public RestResultPacker<AuthenticationPrincipal.Vo> addAppDomain(HttpServletRequest request, @RequestBody AuthenticationPrincipal.PrincipalGroupForm form) throws HTTPException {
        AuthCheckingContext authCheckingContext = authCheckingHelper.myResources(request, AuthCheckingStatement.checks("iam:AddAppDomain", locatorConfiguration.fullLocator("%s", "app-domain")));
        AuthenticationPrincipal toCreatePrincipal = new AuthenticationPrincipal(
                form.getName(), "NO-PASSWORD",
                false, true, false, true,
                AuthenticationPrincipal.PrincipalType.APP_DOMAIN,
                authConfiguration.getPasswordHashingStrategy()
        );
        toCreatePrincipal.setOwner(authCheckingContext.getResourceOwner());
        return RestResultPacker.success(authenticationPrincipalService.createPrincipal(toCreatePrincipal).vo(locatorConfiguration));
    }

    @Operation(tags = {"authentication-app-domain"})
    @PostMapping("/principals/{principalId}/app-domains")
    public RestResultPacker<AuthenticationPrincipal.Vo> addAppDomain(HttpServletRequest request, @PathVariable Long principalId, @RequestBody AuthenticationPrincipal.PrincipalGroupForm form) throws HTTPException {
        AuthCheckingContext authCheckingContext = authCheckingHelper.theirResources(request, AuthCheckingStatement.checks("iam:AddAppDomain", locatorConfiguration.fullLocator("%s", "app-domain")), principalId);
        AuthenticationPrincipal toCreatePrincipal = new AuthenticationPrincipal(
                form.getName(), "NO-PASSWORD",
                false, true, false, true,
                AuthenticationPrincipal.PrincipalType.APP_DOMAIN,
                authConfiguration.getPasswordHashingStrategy()
        );
        toCreatePrincipal.setOwner(authCheckingContext.getResourceOwner());
        return RestResultPacker.success(authenticationPrincipalService.createPrincipal(toCreatePrincipal).vo(locatorConfiguration));
    }

    @Operation(tags = {"authentication-app-domain"})
    @GetMapping("/principals/current/app-domains")
    public RestResultPacker<Page<AuthenticationPrincipal.Vo>> pagingAppDomains(HttpServletRequest request, @RequestParam int page, @RequestParam int size) throws HTTPException {
        AuthCheckingContext authCheckingContext = authCheckingHelper.myResources(request, AuthCheckingStatement.checks("iam:GetAppDomain", locatorConfiguration.fullLocator("%s", "app-domain")));
        return RestResultPacker.success(authenticationPrincipalService.pagingAppDomains(authCheckingContext.getResourceOwner(), page, size).map(owner -> owner.vo(locatorConfiguration)));
    }

    @Operation(tags = {"authentication-app-domain"})
    @GetMapping("/principals/{principalId}/app-domains")
    public RestResultPacker<Page<AuthenticationPrincipal.Vo>> pagingAppDomains(HttpServletRequest request, @PathVariable Long principalId, @RequestParam int page, @RequestParam int size) throws HTTPException {
        AuthCheckingContext authCheckingContext = authCheckingHelper.theirResources(request, AuthCheckingStatement.checks("iam:GetAppDomain", locatorConfiguration.fullLocator("%s", "app-domain")), principalId);
        return RestResultPacker.success(authenticationPrincipalService.pagingAppDomains(authCheckingContext.getResourceOwner(), page, size).map(owner -> owner.vo(locatorConfiguration)));
    }

    @Operation(tags = {"authentication-app-domain"})
    @GetMapping("/principals/current/app-domains/{appDomainId}")
    public RestResultPacker<AuthenticationPrincipal.Vo> getAppDomain(HttpServletRequest request, @PathVariable Long appDomainId) throws HTTPException {
        AuthCheckingContext authCheckingContext = authCheckingHelper.myResources(request, AuthCheckingStatement.checks("iam:GetAppDomain", locatorConfiguration.fullLocator("%s", "app-domain", appDomainId.toString())));
        try {
            AuthenticationPrincipal principalGroupToFind = authenticationPrincipalService.findPrincipalById(appDomainId);
            if (principalGroupToFind.getOwner() != null && !principalGroupToFind.typeIs(AuthenticationPrincipal.PrincipalType.APP_DOMAIN)) {
                throw new NotFoundException(String.format("Cannot find app domain %s of principal %s.", appDomainId, authCheckingContext.getResourceOwner().getId()));
            }
            return RestResultPacker.success(principalGroupToFind.vo(locatorConfiguration));
        } catch (NotFoundException e) {
            throw new HTTP404Exception(e);
        }
    }

    @Operation(tags = {"authentication-app-domain"})
    @GetMapping("/principals/{principalId}/app-domains/{appDomainId}")
    public RestResultPacker<AuthenticationPrincipal.Vo> getAppDomain(HttpServletRequest request, @PathVariable Long principalId, @PathVariable Long appDomainId) throws HTTPException {
        authCheckingHelper.theirResources(request, AuthCheckingStatement.checks("iam:GetAppDomain", locatorConfiguration.fullLocator("%s", "app-domain", appDomainId.toString())), principalId);
        try {
            AuthenticationPrincipal principalGroupToFind = authenticationPrincipalService.findPrincipalById(appDomainId);
            if (principalGroupToFind.getOwner() != null && !principalGroupToFind.getOwner().getId().equals(principalId) && !principalGroupToFind.typeIs(AuthenticationPrincipal.PrincipalType.APP_DOMAIN)) {
                throw new NotFoundException(String.format("Cannot find app domain %s of principal %s.", appDomainId, principalId));
            }
            return RestResultPacker.success(principalGroupToFind.vo(locatorConfiguration));
        } catch (NotFoundException e) {
            throw new HTTP404Exception(e);
        }
    }

    private void deleteAppDomain(AuthCheckingContext authCheckingContext, Long appDomainId) throws HTTP404Exception {
        try {
            AuthenticationPrincipal subprincipal = authenticationPrincipalService.findPrincipalById(appDomainId);
            if (authCheckingContext.belongToResourceOwner(subprincipal.getOwner())) {
                if (subprincipal.typeIs(AuthenticationPrincipal.PrincipalType.APP_DOMAIN)) {
                    authenticationPrincipalService.deletePrincipalById(appDomainId);
                } else {
                    throw new NotFoundException("Principal " + appDomainId + " is not an app domain.");
                }
            } else {
                throw new NotFoundException("Cannot find app domain " + appDomainId + " of current principal.");
            }
        } catch (NotFoundException e) {
            throw new HTTP404Exception(e);
        }
    }

    @Operation(tags = {"authentication-app-domain"})
    @DeleteMapping("/principals/current/app-domains/{appDomainId}")
    public RestResultPacker<String> deleteAppDomain(HttpServletRequest request, @PathVariable Long appDomainId) throws HTTPException {
        AuthCheckingContext authCheckingContext = authCheckingHelper.myResources(request, AuthCheckingStatement.checks("iam:DeleteAppDomain", locatorConfiguration.fullLocator("%s", "app-domain", appDomainId.toString())));
        deleteAppDomain(authCheckingContext, appDomainId);
        return RestResultPacker.success("App domain deleted.");
    }

    @Operation(tags = {"authentication-app-domain"})
    @DeleteMapping("/principals/{principalId}/app-domains/{appDomainId}")
    public RestResultPacker<String> deleteAppDomain(HttpServletRequest request, @PathVariable Long principalId, @PathVariable Long appDomainId) throws HTTPException {
        AuthCheckingContext authCheckingContext = authCheckingHelper.theirResources(request, AuthCheckingStatement.checks("iam:DeleteAppDomain", locatorConfiguration.fullLocator("%s", "app-domain", appDomainId.toString())), principalId);
        deleteAppDomain(authCheckingContext, appDomainId);
        return RestResultPacker.success("App domain deleted.");
    }
}
