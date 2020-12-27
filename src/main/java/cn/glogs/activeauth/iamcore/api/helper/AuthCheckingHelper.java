package cn.glogs.activeauth.iamcore.api.helper;

import cn.glogs.activeauth.iamcore.api.payload.AuthCheckingStatement;
import cn.glogs.activeauth.iamcore.api.payload.AuthCheckingContext;
import cn.glogs.activeauth.iamcore.domain.AuthenticationPrincipal;
import cn.glogs.activeauth.iamcore.domain.AuthenticationSession;
import cn.glogs.activeauth.iamcore.exception.HTTP401Exception;
import cn.glogs.activeauth.iamcore.exception.HTTP403Exception;
import cn.glogs.activeauth.iamcore.exception.HTTP404Exception;
import cn.glogs.activeauth.iamcore.exception.business.NotFoundException;
import cn.glogs.activeauth.iamcore.service.AuthenticationPrincipalService;
import cn.glogs.activeauth.iamcore.service.AuthenticationSessionService;
import cn.glogs.activeauth.iamcore.service.AuthorizationService;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

@Component
public class AuthCheckingHelper {

    private final AuthenticationSessionService authenticationSessionService;
    private final AuthenticationPrincipalService authenticationPrincipalService;
    private final AuthorizationService authorizationService;

    public AuthCheckingHelper(AuthenticationSessionService authenticationSessionService, AuthenticationPrincipalService authenticationPrincipalService, AuthorizationService authorizationService) {
        this.authenticationSessionService = authenticationSessionService;
        this.authenticationPrincipalService = authenticationPrincipalService;
        this.authorizationService = authorizationService;
    }

    public AuthCheckingContext theirResources(
            HttpServletRequest request,
            AuthCheckingStatement authCheckingStatement,
            Long resourceOwnerPrincipalId
    ) throws HTTP401Exception, HTTP403Exception, HTTP404Exception {
        try {
            AuthenticationPrincipal resourceOwnerPrincipal = authenticationPrincipalService.findPrincipalById(resourceOwnerPrincipalId);
            return theirResources(request, authCheckingStatement, resourceOwnerPrincipal);
        } catch (NotFoundException e) {
            throw new HTTP404Exception(e);
        }
    }

    public AuthCheckingContext theirResources(
            HttpServletRequest request,
            AuthCheckingStatement authCheckingStatement,
            AuthenticationPrincipal resourceOwnerPrincipal
    ) throws HTTP401Exception, HTTP403Exception {
        try {
            AuthenticationSession currentSession = authenticationSessionService.getMeSession(request);
            return theirResources(currentSession, authCheckingStatement, resourceOwnerPrincipal);
        } catch (AuthenticationSession.SessionRequestNotAuthorizedException e) {
            throw new HTTP401Exception(e);
        } catch (AuthenticationSession.SessionNotFoundException e) {
            throw new HTTP403Exception(e);
        }
    }
    public AuthCheckingContext theirResources(
            AuthenticationSession currentSession,
            AuthCheckingStatement authCheckingStatement,
            Long resourceOwnerPrincipalId
    ) throws HTTP403Exception, HTTP404Exception {
        try {
            AuthenticationPrincipal resourceOwnerPrincipal = authenticationPrincipalService.findPrincipalById(resourceOwnerPrincipalId);
            return theirResources(currentSession, authCheckingStatement, resourceOwnerPrincipal);
        } catch (NotFoundException e) {
            throw new HTTP404Exception(e);
        }
    }

    public AuthCheckingContext theirResources(
            AuthenticationSession currentSession,
            AuthCheckingStatement authCheckingStatement,
            AuthenticationPrincipal resourceOwnerPrincipal
    ) throws HTTP403Exception {
        for (AuthCheckingStatement.Statement payloadEntity : authCheckingStatement.getStatements()) {
            boolean access = authorizationService.challenge(currentSession.getAuthenticationPrincipal(), payloadEntity.getAction(), payloadEntity.resourceLocators(resourceOwnerPrincipal.getId()));
            if (!access) {
                throw new HTTP403Exception("Not allowed.");
            }
        }
        return new AuthCheckingContext(currentSession, resourceOwnerPrincipal);
    }

    public AuthCheckingContext myResources(
            HttpServletRequest request,
            AuthCheckingStatement authCheckingStatement
    ) throws HTTP401Exception, HTTP403Exception {
        try {
            AuthenticationSession currentSession = authenticationSessionService.getMeSession(request);
            return myResources(currentSession, authCheckingStatement);
        } catch (AuthenticationSession.SessionRequestNotAuthorizedException e) {
            throw new HTTP401Exception(e);
        } catch (AuthenticationSession.SessionNotFoundException e) {
            throw new HTTP403Exception(e);
        }
    }

    public AuthCheckingContext myResources(
            AuthenticationSession currentSession,
            AuthCheckingStatement authCheckingStatement
    ) throws HTTP403Exception {
        for (AuthCheckingStatement.Statement payloadEntity : authCheckingStatement.getStatements()) {
            boolean access = authorizationService.challenge(currentSession.getAuthenticationPrincipal(), payloadEntity.getAction(), payloadEntity.resourceLocators(currentSession.getAuthenticationPrincipal().getId()));
            if (!access) {
                throw new HTTP403Exception("Not allowed.");
            }
        }
        return new AuthCheckingContext(currentSession, currentSession.getAuthenticationPrincipal());
    }

    public AuthCheckingContext systemResources(
            HttpServletRequest request,
            AuthCheckingStatement authCheckingStatement
    ) throws HTTP401Exception, HTTP403Exception {
        try {
            AuthenticationSession currentSession = authenticationSessionService.getMeSession(request);
            for (AuthCheckingStatement.Statement payloadEntity : authCheckingStatement.getStatements()) {
                boolean access = authorizationService.challenge(currentSession.getAuthenticationPrincipal(), payloadEntity.getAction(), payloadEntity.resourceLocators());
                if (!access) {
                    throw new HTTP403Exception("Not allowed.");
                }
            }
            return new AuthCheckingContext(currentSession, null);
        } catch (AuthenticationSession.SessionRequestNotAuthorizedException e) {
            throw new HTTP401Exception(e);
        } catch (AuthenticationSession.SessionNotFoundException e) {
            throw new HTTP403Exception(e);
        }
    }
}
