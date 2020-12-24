package cn.glogs.activeauth.iamcore.repository;

import cn.glogs.activeauth.iamcore.domain.AuthenticationSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AuthenticationSessionRepository extends JpaRepository<AuthenticationSession, Long> {
    Optional<AuthenticationSession> findByToken(String token);
}
