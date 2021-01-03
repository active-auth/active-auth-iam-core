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

    public boolean belongToResourceOwner(AuthenticationPrincipal ownerFromResourceData) {
        return resourceOwner != null
                && resourceOwner.getId() != null
                && ownerFromResourceData.getId() != null
                && resourceOwner.getId().equals(ownerFromResourceData.getId());
    }

    public boolean belongToCurrentSession(AuthenticationPrincipal ownerFromResourceData) {
        return currentSession != null
                && currentSession.getAuthenticationPrincipal() != null
                && currentSession.getAuthenticationPrincipal().getId() != null
                && ownerFromResourceData.getId() != null
                && currentSession.getAuthenticationPrincipal().getId().equals(ownerFromResourceData.getId());
    }
}
