package cn.glogs.activeauth.iamcore.repository;

import cn.glogs.activeauth.iamcore.domain.AuthorizationPolicyGrant;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthorizationPolicyGrantRepository extends JpaRepository<AuthorizationPolicyGrant, Long> {
}
