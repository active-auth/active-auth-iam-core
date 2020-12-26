package cn.glogs.activeauth.iamcore.service.impl;

import cn.glogs.activeauth.iamcore.domain.AuthenticationPrincipal;
import cn.glogs.activeauth.iamcore.domain.AuthenticationPrincipalKeyPair;
import cn.glogs.activeauth.iamcore.domain.keypair.KeyPair;
import cn.glogs.activeauth.iamcore.domain.keypair.RSAKeyPair;
import cn.glogs.activeauth.iamcore.repository.AuthenticationPrincipalKeyPairRepository;
import cn.glogs.activeauth.iamcore.service.AuthenticationPrincipalKeyPairService;
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
public class AuthenticationPrincipalKeyPairServiceImpl implements AuthenticationPrincipalKeyPairService {

    private final AuthenticationPrincipalKeyPairRepository authenticationPrincipalKeyPairRepository;

    public AuthenticationPrincipalKeyPairServiceImpl(AuthenticationPrincipalKeyPairRepository authenticationPrincipalKeyPairRepository) {
        this.authenticationPrincipalKeyPairRepository = authenticationPrincipalKeyPairRepository;
    }

    @Override
    @Transactional
    public AuthenticationPrincipalKeyPair genKey(AuthenticationPrincipal principal, AuthenticationPrincipalKeyPair.GenKeyPairForm form) {
        AuthenticationPrincipalKeyPair principalKeyPair = new AuthenticationPrincipalKeyPair();
        try {
            KeyPair keyPair = RSAKeyPair.generateKeyPair();
            principalKeyPair.setKeyId(UUID.randomUUID().toString());
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
    public Page<AuthenticationPrincipalKeyPair> pagingKeyPairs(AuthenticationPrincipal owner, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size);
        return authenticationPrincipalKeyPairRepository.findAll((Specification<AuthenticationPrincipalKeyPair>) (root, query, criteriaBuilder) -> {
            Path<AuthenticationPrincipal> principalField = root.get("principal");
            return criteriaBuilder.equal(principalField, owner);
        }, pageRequest);
    }
}
