package cn.glogs.activeauth.iamcore.repository;

import cn.glogs.activeauth.iamcore.domain.AuthenticationPrincipal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AuthenticationPrincipalRepository extends JpaRepository<AuthenticationPrincipal, Long> {
    Optional<AuthenticationPrincipal> findByName(String name);
}
