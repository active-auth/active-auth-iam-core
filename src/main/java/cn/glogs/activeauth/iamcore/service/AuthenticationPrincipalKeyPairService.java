package cn.glogs.activeauth.iamcore.service;

import cn.glogs.activeauth.iamcore.domain.AuthenticationPrincipal;
import cn.glogs.activeauth.iamcore.domain.AuthenticationPrincipalKeyPair;
import cn.glogs.activeauth.iamcore.exception.business.NotFoundException;
import org.springframework.data.domain.Page;

public interface AuthenticationPrincipalKeyPairService {
    AuthenticationPrincipalKeyPair genKey(AuthenticationPrincipal principal, AuthenticationPrincipalKeyPair.GenKeyPairForm form);

    Page<AuthenticationPrincipalKeyPair> pagingKeyPairs(AuthenticationPrincipal principal, int page, int size);

    AuthenticationPrincipalKeyPair getKeyByKeyId(String keyId) throws NotFoundException;
}
