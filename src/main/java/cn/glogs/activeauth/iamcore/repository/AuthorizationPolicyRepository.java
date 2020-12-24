package cn.glogs.activeauth.iamcore.repository;

import cn.glogs.activeauth.iamcore.domain.AuthorizationPolicy;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthorizationPolicyRepository extends JpaRepository<AuthorizationPolicy, Long> {
}
