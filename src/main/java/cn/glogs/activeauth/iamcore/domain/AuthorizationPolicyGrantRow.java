package cn.glogs.activeauth.iamcore.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class AuthorizationPolicyGrantRow {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(cascade = CascadeType.REMOVE)
    private AuthenticationPrincipal granter;

    @ManyToOne(cascade = CascadeType.REMOVE)
    private AuthenticationPrincipal grantee;

    @ManyToOne(cascade = CascadeType.REMOVE)
    private AuthorizationPolicy policy;

    private AuthorizationPolicy.PolicyType policyType;

    private String policyAction;

    private String policyResource;

    private boolean revoked = false;
}
