package cn.glogs.activeauth.iamcore.repository;

import cn.glogs.activeauth.iamcore.domain.AuthorizationPolicyGrant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface AuthorizationPolicyGrantRepository extends JpaRepository<AuthorizationPolicyGrant, Long>, JpaSpecificationExecutor<AuthorizationPolicyGrant> {
    List<AuthorizationPolicyGrant> findAllByPolicyId(Long policyId);
}
