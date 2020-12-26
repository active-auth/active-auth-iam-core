package cn.glogs.activeauth.iamcore.repository;

import cn.glogs.activeauth.iamcore.domain.AuthenticationPrincipal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface AuthenticationPrincipalRepository extends JpaRepository<AuthenticationPrincipal, Long>, JpaSpecificationExecutor<AuthenticationPrincipal> {
    Optional<AuthenticationPrincipal> findByName(String name);
}
