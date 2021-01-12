package cn.glogs.activeauth.iamcore.api.helper;

import cn.glogs.activeauth.iamcore.api.payload.AuthCheckingStatement;
import cn.glogs.activeauth.iamcore.api.payload.SafetyVerifyingContext;
import cn.glogs.activeauth.iamcore.config.properties.MfaConfiguration;
import cn.glogs.activeauth.iamcore.domain.AuthenticationDisposableSession;
import cn.glogs.activeauth.iamcore.domain.AuthenticationPrincipal;
import cn.glogs.activeauth.iamcore.exception.HTTP400Exception;
import cn.glogs.activeauth.iamcore.exception.business.NotFoundException;
import cn.glogs.activeauth.iamcore.exception.business.SafetyVerifyingException;
import cn.glogs.activeauth.iamcore.service.AuthenticationDisposableSessionService;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class SafetyVerifyingHelper {
    private final AuthenticationDisposableSessionService authenticationDisposableSessionService;
    private final MfaConfiguration mfaConfiguration;

    public SafetyVerifyingHelper(AuthenticationDisposableSessionService authenticationDisposableSessionService, MfaConfiguration mfaConfiguration) {
        this.authenticationDisposableSessionService = authenticationDisposableSessionService;
        this.mfaConfiguration = mfaConfiguration;
    }

    private void needNewSession(AuthenticationPrincipal principal, AuthCheckingStatement authCheckingStatement, Object... formatArgs) {
        List<String> actions = new ArrayList<>();
        List<String> resources = new ArrayList<>();
        for (AuthCheckingStatement.Statement statement : authCheckingStatement.getStatements()) {
            actions.add(statement.getAction());
            resources.addAll(List.of(statement.resourceLocators(formatArgs)));
        }
        AuthenticationDisposableSession sealedDisposableSession = authenticationDisposableSessionService.create(principal, actions, resources);
        throw new SafetyVerifyingException(new SafetyVerifyingContext(sealedDisposableSession, principal));
    }

    public void reinforce(
            HttpServletRequest request,
            boolean everytime,
            AuthenticationPrincipal principal,
            AuthCheckingStatement authCheckingStatement,
            Object... formatArgs
    ) throws HTTP400Exception {
        String tokenHeader = mfaConfiguration.getVerificationTokenHeader();
        Optional<String> vTokenOpt = Optional.ofNullable(request.getHeader(tokenHeader));
        Optional<String> debugDangerHeaderOpt = Optional.ofNullable(request.getHeader("X-Danger-Env"));
        if (principal.isMfaEnable() && (debugDangerHeaderOpt.isPresent() || everytime)) {
            // TODO check if safety-verifying is needed. Now: claimed everytime, or debugging with danger env header, or the environment is really dangerous.
            if (vTokenOpt.isPresent()) {
                String vToken = vTokenOpt.get();
                try {
                    AuthenticationDisposableSession disposableSession = authenticationDisposableSessionService.getByToken(vToken);
                    if (!disposableSession.isRuined() && !disposableSession.ifExpired() && disposableSession.allow(authCheckingStatement, formatArgs)) {
                        disposableSession.ruin();
                        authenticationDisposableSessionService.update(disposableSession.getId(), disposableSession);
                    } else {
                        needNewSession(principal, authCheckingStatement, formatArgs);
                    }
                } catch (NotFoundException e) {
                    throw new HTTP400Exception(e);
                }
            } else {
                needNewSession(principal, authCheckingStatement, formatArgs);
            }
        }
    }
}
