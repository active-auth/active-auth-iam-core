package cn.glogs.activeauth.iamcore.api.payload;

import cn.glogs.activeauth.iamcore.domain.AuthenticationDisposableSession;
import cn.glogs.activeauth.iamcore.domain.AuthenticationPrincipal;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SafetyVerifyingContext {
    private AuthenticationDisposableSession disposableSession;
    private AuthenticationPrincipal resourceOwner;
}
