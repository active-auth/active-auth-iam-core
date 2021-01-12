package cn.glogs.activeauth.iamcore.service.impl;

import cn.glogs.activeauth.iamcore.domain.AuthenticationDisposableSession;
import cn.glogs.activeauth.iamcore.domain.AuthenticationPrincipal;
import cn.glogs.activeauth.iamcore.repository.AuthenticationDisposableSessionRepository;
import cn.glogs.activeauth.iamcore.service.AuthenticationDisposableSessionService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AuthenticationDisposableSessionServiceImpl implements AuthenticationDisposableSessionService {

    private final AuthenticationDisposableSessionRepository authenticationDisposableSessionRepository;

    public AuthenticationDisposableSessionServiceImpl(AuthenticationDisposableSessionRepository authenticationDisposableSessionRepository) {
        this.authenticationDisposableSessionRepository = authenticationDisposableSessionRepository;
    }

    @Override
    public AuthenticationDisposableSession create(AuthenticationPrincipal principal, List<String> actions, List<String> resources) {
        AuthenticationDisposableSession toSave = AuthenticationDisposableSession.generate(principal, actions, resources);
        return authenticationDisposableSessionRepository.save(toSave);
    }

    @Override
    public boolean consume(String token) {
        Optional<AuthenticationDisposableSession> disposableSessionOpt = authenticationDisposableSessionRepository.findByToken(token);
        if (disposableSessionOpt.isPresent()) {
            AuthenticationDisposableSession disposableSession = disposableSessionOpt.get();
            if (disposableSession.isRuined()) {
                return false;
            } else {
                disposableSession.ruin();
                authenticationDisposableSessionRepository.save(disposableSession);
                return true;
            }
        } else {
            return false;
        }
    }
}
