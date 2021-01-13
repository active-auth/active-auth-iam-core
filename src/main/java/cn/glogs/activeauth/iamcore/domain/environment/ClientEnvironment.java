package cn.glogs.activeauth.iamcore.domain.environment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class ClientEnvironment {
    private String ip;
    private String userAgent;
}
