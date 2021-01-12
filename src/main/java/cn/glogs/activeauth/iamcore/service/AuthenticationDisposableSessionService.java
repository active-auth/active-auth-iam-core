package cn.glogs.activeauth.iamcore.service;

import cn.glogs.activeauth.iamcore.domain.AuthenticationDisposableSession;
import cn.glogs.activeauth.iamcore.domain.AuthenticationPrincipal;

import java.util.List;

public interface AuthenticationDisposableSessionService {
    AuthenticationDisposableSession create(AuthenticationPrincipal principal, List<String> actions, List<String> resources);

    boolean consume(String token);
}
