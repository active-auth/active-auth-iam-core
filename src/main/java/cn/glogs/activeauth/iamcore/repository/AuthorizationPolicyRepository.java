package cn.glogs.activeauth.iamcore.repository;

import cn.glogs.activeauth.iamcore.domain.AuthenticationPrincipal;
import cn.glogs.activeauth.iamcore.domain.AuthorizationPolicy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface AuthorizationPolicyRepository extends JpaRepository<AuthorizationPolicy, Long>, JpaSpecificationExecutor<AuthorizationPolicy> {
    Optional<AuthorizationPolicy> findByIdAndOwner(Long policyId, AuthenticationPrincipal owner);
}
