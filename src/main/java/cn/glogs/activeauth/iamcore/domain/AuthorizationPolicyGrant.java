package cn.glogs.activeauth.iamcore.domain;

import cn.glogs.activeauth.iamcore.api.payload.RestResultPacker;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Date;

@Data
@Entity
public class AuthorizationPolicyGrant implements IamResource {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(cascade = CascadeType.REMOVE)
    private AuthorizationPolicy policy;

    @ManyToOne(cascade = CascadeType.REMOVE)
    private AuthenticationPrincipal granter;

    @ManyToOne(cascade = CascadeType.REMOVE)
    private AuthenticationPrincipal grantee;

    private Date createdAt;

    private Date updatedAt;

    private boolean revoked = false;

    @Override
    public String resourceLocator() {
        return String.format("iam://users/%s/authorization-policy-grants/%s", granter.getId(), id);
    }

    public Vo vo() {
        Vo vo = new Vo();
        vo.resourceLocator = resourceLocator();
        vo.granter = granter.resourceLocator();
        vo.grantee = grantee.resourceLocator();
        vo.policy = policy.vo();
        vo.createdAt = createdAt;
        vo.updatedAt = updatedAt;
        vo.revoked = revoked;
        return vo;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Schema(name = "AuthorizationPolicyGrant.Vo")
    public static class Vo {
        @Schema(example = "iam://users/12/authorization-policy-grants/765")
        private String resourceLocator;
        @Schema(defaultValue = "iam://users/12/principal")
        private String granter;
        @Schema(defaultValue = "iam://users/63/principal")
        private String grantee;
        private AuthorizationPolicy.Vo policy;
        private Date createdAt;
        private Date updatedAt;
        private boolean revoked;
    }
}
