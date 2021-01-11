package cn.glogs.activeauth.iamcore.domain;

import cn.glogs.activeauth.iamcore.domain.converter.StringListAttributeConverter;
import lombok.Data;
import org.apache.commons.lang3.RandomStringUtils;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

@Data
@Entity
public class AuthenticationOneTimeSession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private AuthenticationPrincipal principal;

    private String token;

    private String action;

    @Convert(converter = StringListAttributeConverter.class)
    private List<String> resources;

    private boolean ruined;

    private Date createdAt;

    private Date ruinedAt;

    public static AuthenticationOneTimeSession create(AuthenticationPrincipal principal, String action, List<String> resources) {
        AuthenticationOneTimeSession oneTimeSession = new AuthenticationOneTimeSession();
        oneTimeSession.principal = principal;
        oneTimeSession.token = RandomStringUtils.randomAlphanumeric(72);
        oneTimeSession.action = action;
        oneTimeSession.resources = resources;
        oneTimeSession.ruined = false;
        return oneTimeSession;
    }

    public void ruin() {
        this.ruined = true;
        this.ruinedAt = new Date();
    }
}
