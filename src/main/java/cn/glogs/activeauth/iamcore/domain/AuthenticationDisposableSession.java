package cn.glogs.activeauth.iamcore.domain;

import cn.glogs.activeauth.iamcore.api.payload.AuthCheckingStatement;
import cn.glogs.activeauth.iamcore.domain.converter.StringSetAttributeConverter;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;

import javax.persistence.*;
import java.util.*;

@Data
@Entity
public class AuthenticationDisposableSession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private AuthenticationPrincipal principal;

    @Column(unique = true)
    private String tokenId;

    @Column(unique = true)
    private String token;

    @Convert(converter = StringSetAttributeConverter.class)
    private Set<String> actions;

    @Convert(converter = StringSetAttributeConverter.class)
    private Set<String> resources;

    private boolean unsealed;

    private boolean ruined;

    private Date createdAt;

    private Date expiredAt;

    private Date unsealedAt;

    private Date ruinedAt;

    public static AuthenticationDisposableSession generate(AuthenticationPrincipal principal, List<String> actions, List<String> resources) {
        AuthenticationDisposableSession disposableSession = new AuthenticationDisposableSession();
        disposableSession.principal = principal;
        disposableSession.tokenId = UUID.randomUUID().toString();
        disposableSession.token = RandomStringUtils.randomAlphanumeric(72);
        disposableSession.actions = new HashSet<>(actions);
        disposableSession.resources = new HashSet<>(resources);
        disposableSession.unsealed = false;
        disposableSession.ruined = false;
        Calendar cal = Calendar.getInstance();
        disposableSession.createdAt = cal.getTime();
        cal.add(Calendar.MINUTE, 15);
        disposableSession.expiredAt = cal.getTime();
        return disposableSession;
    }

    public boolean ifExpired() {
        return expiredAt.before(new Date());
    }

    public void unseal() {
        unsealed = true;
        unsealedAt = new Date();
    }

    public void ruin() {
        ruined = true;
        ruinedAt = new Date();
    }

    public Vo vo() {
        return new Vo(tokenId, token);
    }

    public boolean allow(AuthCheckingStatement authCheckingStatement, Object... formatArgs) {
        boolean actionsMatched = true;
        boolean resourcesMatched = true;

        for (AuthCheckingStatement.Statement statement : authCheckingStatement.getStatements()) {
            String action = statement.getAction();
            List<String> resources = List.of(statement.resourceLocators(formatArgs));
            if (!actions.contains(action)) {
                actionsMatched = false;
                break;
            }
            for (String resource : resources) {
                if (!resources.contains(resource)) {
                    resourcesMatched = false;
                    break;
                }
            }
        }

        return actionsMatched && resourcesMatched;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Vo {
        private String vId;
        private String vToken;
    }
}
