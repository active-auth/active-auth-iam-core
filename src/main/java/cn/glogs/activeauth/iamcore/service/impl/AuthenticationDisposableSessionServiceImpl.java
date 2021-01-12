package cn.glogs.activeauth.iamcore.service.impl;

import cn.glogs.activeauth.iamcore.domain.AuthenticationDisposableSession;
import cn.glogs.activeauth.iamcore.domain.AuthenticationPrincipal;
import cn.glogs.activeauth.iamcore.exception.business.NotFoundException;
import cn.glogs.activeauth.iamcore.repository.AuthenticationDisposableSessionRepository;
import cn.glogs.activeauth.iamcore.repository.AuthenticationPrincipalRepository;
import cn.glogs.activeauth.iamcore.service.AuthenticationDisposableSessionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class AuthenticationDisposableSessionServiceImpl implements AuthenticationDisposableSessionService {

    private final AuthenticationPrincipalRepository authenticationPrincipalRepository;
    private final AuthenticationDisposableSessionRepository authenticationDisposableSessionRepository;

    public AuthenticationDisposableSessionServiceImpl(AuthenticationPrincipalRepository authenticationPrincipalRepository, AuthenticationDisposableSessionRepository authenticationDisposableSessionRepository) {
        this.authenticationPrincipalRepository = authenticationPrincipalRepository;
        this.authenticationDisposableSessionRepository = authenticationDisposableSessionRepository;
    }

    @Override
    @Transactional
    public AuthenticationDisposableSession create(Long principalId, List<String> actions, List<String> resources) throws NotFoundException {
        AuthenticationPrincipal principal = authenticationPrincipalRepository.findById(principalId).orElseThrow(() -> new NotFoundException(String.format("Principal %s not found.", principalId)));
        AuthenticationDisposableSession toSave = AuthenticationDisposableSession.generate(principal, actions, resources);
        return authenticationDisposableSessionRepository.save(toSave);
    }

    @Override
    @Transactional
    public AuthenticationDisposableSession create(AuthenticationPrincipal principal, List<String> actions, List<String> resources) {
        AuthenticationDisposableSession toSave = AuthenticationDisposableSession.generate(principal, actions, resources);
        return authenticationDisposableSessionRepository.save(toSave);
    }

    @Override
    @Transactional
    public AuthenticationDisposableSession unseal(String tokenId) throws NotFoundException {
        AuthenticationDisposableSession disposableSession = authenticationDisposableSessionRepository.findByTokenId(tokenId).orElseThrow(() -> new NotFoundException(String.format("Verification %s not found.", tokenId)));
        disposableSession.unseal();
        authenticationDisposableSessionRepository.save(disposableSession);
        return disposableSession;
    }

    @Override
    @Transactional
    public boolean consume(String token) {
        Optional<AuthenticationDisposableSession> disposableSessionOpt = authenticationDisposableSessionRepository.findByToken(token);
        if (disposableSessionOpt.isPresent()) {
            AuthenticationDisposableSession disposableSession = disposableSessionOpt.get();
            if (disposableSession.isRuined() || disposableSession.isUnsealed()) {
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
