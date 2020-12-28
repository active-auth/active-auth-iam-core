package cn.glogs.activeauth.iamcore.repository;

import cn.glogs.activeauth.iamcore.domain.AuthenticationPrincipalKeyPair;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface AuthenticationPrincipalKeyPairRepository extends JpaRepository<AuthenticationPrincipalKeyPair, Long>, JpaSpecificationExecutor<AuthenticationPrincipalKeyPair> {
    Optional<AuthenticationPrincipalKeyPair> findByKeyId(String keyId);
}
