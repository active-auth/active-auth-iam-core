package cn.glogs.activeauth.iamcore.service;

import cn.glogs.activeauth.iamcore.domain.AuthenticationPrincipal;
import cn.glogs.activeauth.iamcore.exception.business.NotFoundException;

public interface AuthenticationMfaService {
    String setMfa(Long principalId, boolean mfaEnable) throws NotFoundException;

    boolean verify(AuthenticationPrincipal principal, String verificationCode);
}
