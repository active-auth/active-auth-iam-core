package cn.glogs.activeauth.iamcore.repository;

import cn.glogs.activeauth.iamcore.domain.AuthenticationClientEnvironment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthenticationClientEnvironmentRepository extends JpaRepository<AuthenticationClientEnvironment, Long> {
}
