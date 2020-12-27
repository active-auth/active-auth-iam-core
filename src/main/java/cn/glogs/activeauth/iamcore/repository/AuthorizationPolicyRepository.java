package cn.glogs.activeauth.iamcore.repository;

import cn.glogs.activeauth.iamcore.domain.AuthorizationPolicy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface AuthorizationPolicyRepository extends JpaRepository<AuthorizationPolicy, Long>, JpaSpecificationExecutor<AuthorizationPolicy> {
}
