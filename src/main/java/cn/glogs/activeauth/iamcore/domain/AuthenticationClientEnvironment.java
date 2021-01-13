package cn.glogs.activeauth.iamcore.domain;

import cn.glogs.activeauth.iamcore.domain.converter.ClientEnvironmentAttributeConverter;
import cn.glogs.activeauth.iamcore.domain.environment.ClientEnvironment;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Date;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class AuthenticationClientEnvironment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private AuthenticationPrincipal principal;

    @Convert(converter = ClientEnvironmentAttributeConverter.class)
    private ClientEnvironment environment;

    private Date createdAt;

    public AuthenticationClientEnvironment(AuthenticationPrincipal principal, ClientEnvironment environment) {
        this.principal = principal;
        this.environment = environment;
        this.createdAt = new Date();
    }
}
