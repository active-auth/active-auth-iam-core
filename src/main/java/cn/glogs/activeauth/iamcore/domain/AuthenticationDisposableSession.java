package cn.glogs.activeauth.iamcore.domain;

import cn.glogs.activeauth.iamcore.domain.converter.StringListAttributeConverter;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

@Data
@Entity
public class AuthenticationDisposableSession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private AuthenticationPrincipal principal;

    @Column(unique = true)
    private String token;

    @Convert(converter = StringListAttributeConverter.class)
    private List<String> actions;

    @Convert(converter = StringListAttributeConverter.class)
    private List<String> resources;

    private boolean ruined;

    private Date createdAt;

    private Date ruinedAt;

    public static AuthenticationDisposableSession generate(AuthenticationPrincipal principal, List<String> actions, List<String> resources) {
        AuthenticationDisposableSession disposableSession = new AuthenticationDisposableSession();
        disposableSession.principal = principal;
        disposableSession.token = RandomStringUtils.randomAlphanumeric(72);
        disposableSession.actions = actions;
        disposableSession.resources = resources;
        disposableSession.ruined = false;
        disposableSession.createdAt = new Date();
        return disposableSession;
    }

    public void ruin() {
        ruined = true;
        ruinedAt = new Date();
    }

    public Vo vo() {
        return new Vo(token);
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Vo {
        private String token;
    }
}
