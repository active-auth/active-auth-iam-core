package cn.glogs.activeauth.iamcore.service;

import cn.glogs.activeauth.iamcore.domain.AuthenticationPrincipal;
import cn.glogs.activeauth.iamcore.domain.AuthenticationPrincipalSecretKey;
import cn.glogs.activeauth.iamcore.exception.business.NotFoundException;
import cn.glogs.activeauth.iamcore.exception.business.SignatureException;
import org.springframework.data.domain.Page;

public interface AuthenticationPrincipalSecretKeyService {
    AuthenticationPrincipalSecretKey getKeyById(Long keyId) throws NotFoundException;

    AuthenticationPrincipalSecretKey deleteKeyById(Long keyId) throws NotFoundException;

    Page<AuthenticationPrincipalSecretKey> pagingKeysOfOwner(AuthenticationPrincipal principal, int page, int size);

    AuthenticationPrincipalSecretKey generateRSA2048KeyPair(AuthenticationPrincipal principal, AuthenticationPrincipalSecretKey.GenKeyPairForm form) throws SignatureException;

    AuthenticationPrincipalSecretKey getKeyByKeyCode(String keyCode) throws NotFoundException;
}
