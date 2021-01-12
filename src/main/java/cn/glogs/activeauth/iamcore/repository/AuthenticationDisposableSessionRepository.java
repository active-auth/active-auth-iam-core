package cn.glogs.activeauth.iamcore.repository;

import cn.glogs.activeauth.iamcore.domain.AuthenticationDisposableSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AuthenticationDisposableSessionRepository extends JpaRepository<AuthenticationDisposableSession, Long> {
    Optional<AuthenticationDisposableSession> findByToken(String token);
}
