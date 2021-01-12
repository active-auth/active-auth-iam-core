package cn.glogs.activeauth.iamcore.service;

import cn.glogs.activeauth.iamcore.domain.AuthenticationDisposableSession;
import cn.glogs.activeauth.iamcore.domain.AuthenticationPrincipal;
import cn.glogs.activeauth.iamcore.exception.business.NotFoundException;

import java.util.List;

public interface AuthenticationDisposableSessionService {
    AuthenticationDisposableSession create(Long principalId, List<String> actions, List<String> resources) throws NotFoundException;

    AuthenticationDisposableSession create(AuthenticationPrincipal principal, List<String> actions, List<String> resources);

    AuthenticationDisposableSession unseal(String tokenId) throws NotFoundException;

    boolean consume(String token);
}
