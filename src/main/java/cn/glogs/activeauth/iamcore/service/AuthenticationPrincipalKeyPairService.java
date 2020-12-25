package cn.glogs.activeauth.iamcore.service;

import cn.glogs.activeauth.iamcore.domain.AuthenticationPrincipal;
import cn.glogs.activeauth.iamcore.domain.AuthenticationPrincipalKeyPair;

public interface AuthenticationPrincipalKeyPairService {
    AuthenticationPrincipalKeyPair genKey(AuthenticationPrincipal principal, AuthenticationPrincipalKeyPair.GenKeyPairForm form);
}
