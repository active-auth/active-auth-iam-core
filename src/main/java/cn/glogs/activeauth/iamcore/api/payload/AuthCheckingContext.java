package cn.glogs.activeauth.iamcore.api.payload;

import cn.glogs.activeauth.iamcore.domain.AuthenticationPrincipal;
import cn.glogs.activeauth.iamcore.domain.AuthenticationSession;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthCheckingContext {
    private AuthenticationSession currentSession;
    private AuthenticationPrincipal resourceOwner;
}
