package cn.glogs.activeauth.iamcore.service.impl;

import cn.glogs.activeauth.iamcore.domain.AuthenticationPrincipal;
import cn.glogs.activeauth.iamcore.domain.AuthenticationPrincipalSecretKey;
import cn.glogs.activeauth.iamcore.domain.keypair.KeyPair;
import cn.glogs.activeauth.iamcore.domain.keypair.RSAKeyPair;
import cn.glogs.activeauth.iamcore.exception.business.NotFoundException;
import cn.glogs.activeauth.iamcore.repository.AuthenticationPrincipalKeyPairRepository;
import cn.glogs.activeauth.iamcore.service.AuthenticationPrincipalSecretKeyService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.Path;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.UUID;

@Service
public class AuthenticationPrincipalSecretKeyServiceImpl implements AuthenticationPrincipalSecretKeyService {

    private final AuthenticationPrincipalKeyPairRepository authenticationPrincipalKeyPairRepository;

    public AuthenticationPrincipalSecretKeyServiceImpl(AuthenticationPrincipalKeyPairRepository authenticationPrincipalKeyPairRepository) {
        this.authenticationPrincipalKeyPairRepository = authenticationPrincipalKeyPairRepository;
    }

    @Override
    public AuthenticationPrincipalSecretKey getKeyById(Long keyId) throws NotFoundException {
        return authenticationPrincipalKeyPairRepository.findById(keyId).orElseThrow(() -> new NotFoundException("Keypair not found."));
    }

    @Override
    public AuthenticationPrincipalSecretKey deleteKeyById(Long keyId) throws NotFoundException {
        AuthenticationPrincipalSecretKey toDelete = authenticationPrincipalKeyPairRepository.findById(keyId).orElseThrow(() -> new NotFoundException("Keypair not found."));
        authenticationPrincipalKeyPairRepository.delete(toDelete);
        return toDelete;
    }

    @Override
    public Page<AuthenticationPrincipalSecretKey> pagingKeysOfOwner(AuthenticationPrincipal owner, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size);
        return authenticationPrincipalKeyPairRepository.findAll((Specification<AuthenticationPrincipalSecretKey>) (root, query, criteriaBuilder) -> {
            Path<AuthenticationPrincipal> principalField = root.get("principal");
            return criteriaBuilder.equal(principalField, owner);
        }, pageRequest);
    }

    @Override
    @Transactional
    public AuthenticationPrincipalSecretKey generateKey(AuthenticationPrincipal principal, AuthenticationPrincipalSecretKey.GenKeyPairForm form) {
        AuthenticationPrincipalSecretKey principalKeyPair = new AuthenticationPrincipalSecretKey();
        try {
            KeyPair keyPair = RSAKeyPair.generateKeyPair();
            principalKeyPair.setKeyCode(UUID.randomUUID().toString());
            principalKeyPair.setDescription(form.getDescription());
            principalKeyPair.setPubKey(keyPair.getPubKey());
            principalKeyPair.setPriKey(keyPair.getPriKey());
            principalKeyPair.setEnabled(true);
            principalKeyPair.setCreateTime(new Date());
            principalKeyPair.setPrincipal(principal);
            return authenticationPrincipalKeyPairRepository.save(principalKeyPair);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return principalKeyPair;
    }

    @Override
    public AuthenticationPrincipalSecretKey getKeyByKeyCode(String keyCode) throws NotFoundException {
        return authenticationPrincipalKeyPairRepository.findByKeyCode(keyCode).orElseThrow(() -> new NotFoundException("SecretKey not found."));
    }
}
