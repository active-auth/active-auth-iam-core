package cn.glogs.activeauth.iamcore.service;

import cn.glogs.activeauth.iamcore.domain.AuthenticationPrincipal;

public interface AuthorizationService {
    boolean challenge(AuthenticationPrincipal challenger, String action, String... resources);

    boolean challenge(boolean checkForChain, AuthenticationPrincipal challenger, String action, String... resources);

    boolean challengeFather(AuthenticationPrincipal authenticationPrincipal, String action, String... resources);
}
