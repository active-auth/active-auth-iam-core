package cn.glogs.activeauth.iamcore.service.impl;

import cn.glogs.activeauth.iamcore.config.properties.AuthConfiguration;
import cn.glogs.activeauth.iamcore.domain.AuthenticationClientEnvironment;
import cn.glogs.activeauth.iamcore.domain.AuthenticationPrincipal;
import cn.glogs.activeauth.iamcore.domain.AuthenticationSession;
import cn.glogs.activeauth.iamcore.domain.environment.ClientEnvironment;
import cn.glogs.activeauth.iamcore.domain.password.PasswordHashingStrategy;
import cn.glogs.activeauth.iamcore.exception.business.NotFoundException;
import cn.glogs.activeauth.iamcore.repository.AuthenticationClientEnvironmentRepository;
import cn.glogs.activeauth.iamcore.repository.AuthenticationPrincipalRepository;
import cn.glogs.activeauth.iamcore.repository.AuthenticationSessionRepository;
import cn.glogs.activeauth.iamcore.service.AuthenticationSessionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
@Slf4j
public class AuthenticationSessionServiceImpl implements AuthenticationSessionService {

    private final int tokenExpiringSeconds;
    private final String fullTokenPrefix;
    private final AuthenticationSessionRepository authenticationSessionRepository;
    private final AuthenticationPrincipalRepository authenticationPrincipalRepository;
    private final AuthenticationClientEnvironmentRepository authenticationClientEnvironmentRepository;
    private final PasswordHashingStrategy passwordHashingStrategy;

    public AuthenticationSessionServiceImpl(
            AuthConfiguration authConfiguration,
            AuthenticationSessionRepository authenticationSessionRepository,
            AuthenticationPrincipalRepository authenticationPrincipalRepository,
            AuthenticationClientEnvironmentRepository authenticationClientEnvironmentRepository) {
        this.tokenExpiringSeconds = authConfiguration.getTokenExpiringSeconds();
        this.fullTokenPrefix = authConfiguration.fullTokenPrefix();
        this.authenticationSessionRepository = authenticationSessionRepository;
        this.authenticationPrincipalRepository = authenticationPrincipalRepository;
        this.passwordHashingStrategy = authConfiguration.getPasswordHashingStrategy();
        this.authenticationClientEnvironmentRepository = authenticationClientEnvironmentRepository;
    }

    @Override
    @Transactional
    public AuthenticationSession login(AuthenticationSession.UserLoginForm form, ClientEnvironment environment) throws NotFoundException, AuthenticationPrincipal.PasswordNotMatchException, AuthenticationPrincipal.PrincipalTypeDoesNotAllowedToLoginException {
        AuthenticationPrincipal authenticationPrincipal = authenticationPrincipalRepository.findByName(form.getName()).orElseThrow(() -> new NotFoundException("Principal Not Found."));
        if (!authenticationPrincipal.passwordVerify(form.getSecret(), passwordHashingStrategy))
            throw new AuthenticationPrincipal.PasswordNotMatchException("Name and password not match!");
        return login(authenticationPrincipal, environment);
    }

    @Override
    @Transactional
    public AuthenticationSession login(AuthenticationPrincipal principal, ClientEnvironment environment) throws AuthenticationPrincipal.PrincipalTypeDoesNotAllowedToLoginException {
        if (!principal.canCreateSession()) {
            throw new AuthenticationPrincipal.PrincipalTypeDoesNotAllowedToLoginException("Principal does not allowed to login!");
        }
        authenticationClientEnvironmentRepository.save(new AuthenticationClientEnvironment(principal, environment));
        AuthenticationSession authenticationSession = AuthenticationSession.newSession(tokenExpiringSeconds, fullTokenPrefix, principal, environment);
        authenticationSessionRepository.save(authenticationSession);
        return authenticationSession;
    }

    @Override
    @Transactional
    public AuthenticationSession getSessionByToken(String token) throws AuthenticationSession.SessionNotFoundException, AuthenticationSession.SessionExpiredException {
        AuthenticationSession authenticationSession = authenticationSessionRepository.findByToken(token).orElseThrow(() -> new AuthenticationSession.SessionNotFoundException("Token not allowed."));
        if (authenticationSession.expired()) {
            throw new AuthenticationSession.SessionExpiredException("Token expired.");
        }
        return authenticationSession;
    }
}
