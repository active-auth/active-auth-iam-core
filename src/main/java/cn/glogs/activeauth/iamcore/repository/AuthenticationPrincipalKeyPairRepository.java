package cn.glogs.activeauth.iamcore.repository;

import cn.glogs.activeauth.iamcore.domain.AuthenticationPrincipalSecretKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface AuthenticationPrincipalKeyPairRepository extends JpaRepository<AuthenticationPrincipalSecretKey, Long>, JpaSpecificationExecutor<AuthenticationPrincipalSecretKey> {
    Optional<AuthenticationPrincipalSecretKey> findByKeyId(String keyId);
}
