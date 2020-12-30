package cn.glogs.activeauth.iamcore.repository;

import cn.glogs.activeauth.iamcore.domain.AuthenticationPrincipal;
import cn.glogs.activeauth.iamcore.domain.AuthorizationPolicyGrantRow;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuthorizationPolicyGrantRowRepository extends JpaRepository<AuthorizationPolicyGrantRow, Long> {
    List<AuthorizationPolicyGrantRow> findAllByGranteeIdAndPolicyAction(Long granteeId, String policyAction);

    List<AuthorizationPolicyGrantRow> findAllByGranterId(Long granterId);

    List<AuthorizationPolicyGrantRow> findAllByPolicyId(Long policyId);
}
