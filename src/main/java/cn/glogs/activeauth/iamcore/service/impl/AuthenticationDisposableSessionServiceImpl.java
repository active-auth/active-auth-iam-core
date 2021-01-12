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

    private final AuthenticationDisposableSessionRepository authenticationDisposableSessionRepository;

    public AuthenticationDisposableSessionServiceImpl(AuthenticationDisposableSessionRepository authenticationDisposableSessionRepository) {
        this.authenticationDisposableSessionRepository = authenticationDisposableSessionRepository;
    }

    @Override
    @Transactional
    public AuthenticationDisposableSession create(AuthenticationPrincipal principal, List<String> actions, List<String> resources) {
        AuthenticationDisposableSession toSave = AuthenticationDisposableSession.generate(principal, actions, resources);
        return authenticationDisposableSessionRepository.save(toSave);
    }

    @Override
    @Transactional
    public AuthenticationDisposableSession update(Long id, AuthenticationDisposableSession toUpdate) {
        toUpdate.setId(id);
        return authenticationDisposableSessionRepository.save(toUpdate);
    }

    @Override
    public AuthenticationDisposableSession getByTokenId(String tokenId) throws NotFoundException {
        return authenticationDisposableSessionRepository.findByTokenId(tokenId).orElseThrow(() -> new NotFoundException(String.format("Verification token v_id:%s not found.", tokenId)));
    }

    @Override
    public AuthenticationDisposableSession getByToken(String token) throws NotFoundException {
        return authenticationDisposableSessionRepository.findByToken(token).orElseThrow(() -> new NotFoundException(String.format("Verification token v_token:%s not found.", token)));
    }
}
