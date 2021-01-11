package cn.glogs.activeauth.iamcore.domain;

import cn.glogs.activeauth.iamcore.api.payload.RestResultPacker;
import cn.glogs.activeauth.iamcore.config.properties.LocatorConfiguration;
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
    public String resourceLocator(LocatorConfiguration locatorConfiguration) {
        return locatorConfiguration.fullLocator(String.valueOf(granter.getId()), "policy", String.valueOf(granter.getId()), "grant", String.valueOf(id));
    }

    public Vo vo(LocatorConfiguration locatorConfiguration) {
        Vo vo = new Vo();
        vo.id = id;
        vo.resourceLocator = resourceLocator(locatorConfiguration);
        vo.granter = granter.resourceLocator(locatorConfiguration);
        vo.grantee = grantee.resourceLocator(locatorConfiguration);
        vo.policy = policy.vo(locatorConfiguration);
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
        private Long id;
        //        @Schema(example = "iam://users/12/grants/765")
        private String resourceLocator;
        //        @Schema(defaultValue = "iam://users/12/principal")
        private String granter;
        //        @Schema(defaultValue = "iam://users/63/principal")
        private String grantee;
        private AuthorizationPolicy.Vo policy;
        private Date createdAt;
        private Date updatedAt;
        private boolean revoked;
    }
}
