package cn.glogs.activeauth.iamcore.service.impl;

import cn.glogs.activeauth.iamcore.config.properties.Configuration;
import cn.glogs.activeauth.iamcore.domain.AuthenticationPrincipal;
import cn.glogs.activeauth.iamcore.domain.AuthenticationSession;
import cn.glogs.activeauth.iamcore.domain.password.PasswordHashingStrategy;
import cn.glogs.activeauth.iamcore.exception.business.NotFoundException;
import cn.glogs.activeauth.iamcore.repository.AuthenticationPrincipalRepository;
import cn.glogs.activeauth.iamcore.repository.AuthenticationSessionRepository;
import cn.glogs.activeauth.iamcore.service.AuthenticationSessionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

@Service
public class AuthenticationSessionServiceImpl implements AuthenticationSessionService {

    private final int expireSeconds;

    private final String fullTokenPrefix;

    private final AuthenticationSessionRepository authenticationSessionRepository;

    private final AuthenticationPrincipalRepository authenticationPrincipalRepository;

    private final PasswordHashingStrategy passwordHashingStrategy;

    private final Configuration.LordAuthConfiguration lordAuthConfiguration;

    public AuthenticationSessionServiceImpl(
            Configuration configuration,
            AuthenticationSessionRepository authenticationSessionRepository,
            AuthenticationPrincipalRepository authenticationPrincipalRepository,
            Configuration.LordAuthConfiguration lordAuthConfiguration
    ) {
        this.expireSeconds = configuration.getSessionExpiringSeconds();
        this.fullTokenPrefix = configuration.fullTokenPrefix();
        this.authenticationSessionRepository = authenticationSessionRepository;
        this.authenticationPrincipalRepository = authenticationPrincipalRepository;
        this.passwordHashingStrategy = configuration.getPasswordHashingStrategy();
        this.lordAuthConfiguration = lordAuthConfiguration;
    }

    @Override
    @Transactional
    public AuthenticationSession newSession(AuthenticationSession.CreateSessionForm form) throws NotFoundException, AuthenticationPrincipal.PasswordNotMatchException {
        AuthenticationPrincipal authenticationPrincipal = authenticationPrincipalRepository.findByName(form.getName()).orElseThrow(() -> new NotFoundException("没有找到用户。"));
        if (!authenticationPrincipal.passwordVerify(form.getSecret(), passwordHashingStrategy))
            throw new AuthenticationPrincipal.PasswordNotMatchException("密码不匹配。");
        AuthenticationSession authenticationSession = AuthenticationSession.newSession(expireSeconds, fullTokenPrefix, authenticationPrincipal);
        authenticationSessionRepository.save(authenticationSession);
        return authenticationSession;
    }

    @Override
    @Transactional
    public AuthenticationSession getMeSession(HttpServletRequest request) throws AuthenticationSession.SessionRequestBadHeaderException, AuthenticationSession.SessionNotFoundException {
        String authorizationHeaderValue = Optional.ofNullable(request.getHeader(lordAuthConfiguration.getAuthorizationHeaderName())).orElseThrow(() -> new AuthenticationSession.SessionRequestBadHeaderException("认证头部不符合要求。"));
        return authenticationSessionRepository.findByToken(authorizationHeaderValue).orElseThrow(() -> new AuthenticationSession.SessionNotFoundException("会话未找到。"));
    }
}
