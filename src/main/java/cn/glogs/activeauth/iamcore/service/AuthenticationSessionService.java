package cn.glogs.activeauth.iamcore.service;

import cn.glogs.activeauth.iamcore.domain.AuthenticationPrincipal;
import cn.glogs.activeauth.iamcore.domain.AuthenticationSession;
import cn.glogs.activeauth.iamcore.exception.business.NotFoundException;

public interface AuthenticationSessionService {
    AuthenticationSession login(AuthenticationSession.UserLoginForm form) throws NotFoundException, AuthenticationPrincipal.PasswordNotMatchException, AuthenticationPrincipal.PrincipalTypeDoesNotAllowedToLoginException;

    AuthenticationSession getSessionByToken(String token) throws AuthenticationSession.SessionNotFoundException, AuthenticationSession.SessionExpiredException;
}
