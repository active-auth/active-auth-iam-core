package cn.glogs.activeauth.iamcore.domain;

import cn.glogs.activeauth.iamcore.domain.converter.StringListAttributeConverter;
import lombok.Data;
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

    private String token;

    private String action;

    @Convert(converter = StringListAttributeConverter.class)
    private List<String> resources;

    private boolean expired;

    private Date createdAt;

    private Date expiredAt;

    public static AuthenticationDisposableSession generate(AuthenticationPrincipal principal, String action, List<String> resources) {
        AuthenticationDisposableSession disposableSession = new AuthenticationDisposableSession();
        disposableSession.principal = principal;
        disposableSession.token = RandomStringUtils.randomAlphanumeric(72);
        disposableSession.action = action;
        disposableSession.resources = resources;
        disposableSession.expired = false;
        disposableSession.createdAt = new Date();
        return disposableSession;
    }

    public void ruin() {
        expired = true;
        expiredAt = new Date();
    }
}
