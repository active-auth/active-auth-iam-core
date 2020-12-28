package cn.glogs.activeauth.iamcore.api.helper;

import cn.glogs.activeauth.iamcore.api.payload.AuthCheckingStatement;
import cn.glogs.activeauth.iamcore.api.payload.AuthCheckingContext;
import cn.glogs.activeauth.iamcore.config.properties.Configuration;
import cn.glogs.activeauth.iamcore.domain.AuthenticationPrincipal;
import cn.glogs.activeauth.iamcore.domain.AuthenticationPrincipalKeyPair;
import cn.glogs.activeauth.iamcore.domain.AuthenticationSession;
import cn.glogs.activeauth.iamcore.domain.sign.HttpRsaSignature;
import cn.glogs.activeauth.iamcore.domain.sign.HttpSignature;
import cn.glogs.activeauth.iamcore.exception.HTTP400Exception;
import cn.glogs.activeauth.iamcore.exception.HTTP401Exception;
import cn.glogs.activeauth.iamcore.exception.HTTP403Exception;
import cn.glogs.activeauth.iamcore.exception.HTTP404Exception;
import cn.glogs.activeauth.iamcore.exception.business.NotFoundException;
import cn.glogs.activeauth.iamcore.service.AuthenticationPrincipalKeyPairService;
import cn.glogs.activeauth.iamcore.service.AuthenticationPrincipalService;
import cn.glogs.activeauth.iamcore.service.AuthenticationSessionService;
import cn.glogs.activeauth.iamcore.service.AuthorizationService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Component
public class AuthCheckingHelper {

    private final AuthenticationSessionService authenticationSessionService;
    private final AuthenticationPrincipalKeyPairService authenticationPrincipalKeyPairService;
    private final AuthenticationPrincipalService authenticationPrincipalService;
    private final AuthorizationService authorizationService;
    private final Configuration configuration;

    public AuthCheckingHelper(
            AuthenticationSessionService authenticationSessionService,
            AuthenticationPrincipalKeyPairService authenticationPrincipalKeyPairService,
            AuthenticationPrincipalService authenticationPrincipalService,
            AuthorizationService authorizationService,
            Configuration configuration
    ) {
        this.authenticationSessionService = authenticationSessionService;
        this.authenticationPrincipalKeyPairService = authenticationPrincipalKeyPairService;
        this.authenticationPrincipalService = authenticationPrincipalService;
        this.authorizationService = authorizationService;
        this.configuration = configuration;
    }

    private AuthenticationSession auth(HttpServletRequest request) throws HTTP400Exception, HTTP401Exception {
        try {
            String authorizationHeaderName = configuration.getAuthorizationHeaderName();
            String authorizationHeaderValue = Optional.ofNullable(request.getHeader(authorizationHeaderName)).orElseThrow(() -> new AuthenticationSession.SessionRequestNotAuthorizedException("Unauthorized."));
            if (StringUtils.startsWith(authorizationHeaderValue, configuration.getAuthorizationHeaderTokenValuePrefix())) {
                return authenticationSessionService.getMeSession(authorizationHeaderValue);
            } else if (StringUtils.startsWith(authorizationHeaderValue, configuration.getAuthorizationHeaderSignatureValuePrefix())) {
                String timestampHeaderName = configuration.getTimestampHeaderName();
                String timestampHeaderValue = Optional.ofNullable(request.getHeader(timestampHeaderName)).orElseThrow(() -> new AuthenticationSession.SessionRequestBadHeaderException(String.format("Need timestamp header: %s", timestampHeaderName)));

                long currentTimestamp = Calendar.getInstance().getTimeInMillis() / 1000;
                long requestingTimestamp = Long.parseLong(timestampHeaderValue);

                if (currentTimestamp > requestingTimestamp + configuration.getSignatureExpiringSeconds()) {
                    throw new HTTP401Exception(String.format("Signature expired, current timestamp: %s, requesting timestamp: %s.", currentTimestamp, requestingTimestamp));
                }

                HttpSignature signature = new HttpRsaSignature(authorizationHeaderValue);
                AuthenticationPrincipalKeyPair key = authenticationPrincipalKeyPairService.getKeyByKeyId(signature.getSignature().getKeyId());
                Map<String, String> headerMap = new HashMap<>();
                headerMap.put(timestampHeaderName, timestampHeaderValue);
                boolean signatureValid = signature.verifyAnyRequest(headerMap, key.getPubKey());
                if (signatureValid) {
                    return AuthenticationSession.fakeSession(key.getPrincipal());
                } else {
                    throw new HTTP401Exception("Signature not valid, maybe tampered.");
                }
            } else {
                throw new AuthenticationSession.SessionRequestBadHeaderException(String.format("Value format of header %s does not accepted.", configuration.getAuthorizationHeaderName()));
            }
        } catch (NoSuchAlgorithmException | SignatureException | InvalidKeySpecException | IOException e) {
            e.printStackTrace();
            throw new HTTP400Exception(e);
        } catch (AuthenticationSession.SessionRequestBadHeaderException e) {
            throw new HTTP400Exception(e);
        } catch (AuthenticationSession.SessionRequestNotAuthorizedException | NotFoundException | AuthenticationSession.SessionExpiredException | AuthenticationSession.SessionNotFoundException e) {
            throw new HTTP401Exception(e);
        }
    }

    public AuthCheckingContext theirResources(
            HttpServletRequest request,
            AuthCheckingStatement authCheckingStatement,
            Long resourceOwnerPrincipalId
    ) throws HTTP400Exception, HTTP401Exception, HTTP403Exception, HTTP404Exception {
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
    ) throws HTTP400Exception, HTTP401Exception, HTTP403Exception {
        AuthenticationSession currentSession = auth(request);
        return theirResources(currentSession, authCheckingStatement, resourceOwnerPrincipal);
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
    ) throws HTTP400Exception, HTTP401Exception, HTTP403Exception {
        AuthenticationSession currentSession = auth(request);
        return myResources(currentSession, authCheckingStatement);
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
    ) throws HTTP400Exception, HTTP401Exception, HTTP403Exception {
        AuthenticationSession currentSession = auth(request);
        for (AuthCheckingStatement.Statement payloadEntity : authCheckingStatement.getStatements()) {
            boolean access = authorizationService.challenge(currentSession.getAuthenticationPrincipal(), payloadEntity.getAction(), payloadEntity.resourceLocators());
            if (!access) {
                throw new HTTP403Exception("Not allowed.");
            }
        }
        return new AuthCheckingContext(currentSession, null);
    }
}
