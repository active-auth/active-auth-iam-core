package cn.glogs.activeauth.iamcore.repository;

import cn.glogs.activeauth.iamcore.domain.AuthenticationPrincipal;
import cn.glogs.activeauth.iamcore.domain.AuthenticationPrincipalKeyPair;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuthenticationPrincipalKeyPairRepository extends JpaRepository<AuthenticationPrincipalKeyPair, Long> {
    List<AuthenticationPrincipalKeyPair> findByPrincipal(AuthenticationPrincipal principal);
}
