package cn.glogs.activeauth.iamcore.service;

import cn.glogs.activeauth.iamcore.domain.AuthenticationPrincipal;

public interface AuthorizationService {
    boolean challenge(AuthenticationPrincipal challenger, String action, String... resources);
}
