package cn.glogs.activeauth.iamcore.service;

import cn.glogs.activeauth.iamcore.domain.AuthenticationDisposableSession;
import cn.glogs.activeauth.iamcore.domain.AuthenticationPrincipal;
import cn.glogs.activeauth.iamcore.exception.business.NotFoundException;

import java.util.List;

public interface AuthenticationDisposableSessionService {
    AuthenticationDisposableSession create(AuthenticationPrincipal principal, List<String> actions, List<String> resources);

    AuthenticationDisposableSession update(Long id, AuthenticationDisposableSession toUpdate);

    AuthenticationDisposableSession getByTokenId(String tokenId) throws NotFoundException;

    AuthenticationDisposableSession getByToken(String token) throws NotFoundException;
}
