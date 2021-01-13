package cn.glogs.activeauth.iamcore.service;

import cn.glogs.activeauth.iamcore.domain.AuthenticationPrincipal;

public interface AuthenticationMfaService {
    String setMfa(AuthenticationPrincipal principal, boolean mfaEnable);

    boolean verify(AuthenticationPrincipal principal, String verificationCode);
}
