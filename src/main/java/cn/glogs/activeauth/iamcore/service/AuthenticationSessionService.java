package cn.glogs.activeauth.iamcore.service;

import cn.glogs.activeauth.iamcore.domain.AuthenticationPrincipal;
import cn.glogs.activeauth.iamcore.domain.AuthenticationSession;
import cn.glogs.activeauth.iamcore.exception.business.NotFoundException;

import javax.servlet.http.HttpServletRequest;

public interface AuthenticationSessionService {
    AuthenticationSession newSession(AuthenticationSession.CreateSessionForm form) throws NotFoundException, AuthenticationPrincipal.PasswordNotMatchException;

    AuthenticationSession getMeSession(String token) throws AuthenticationSession.SessionNotFoundException, AuthenticationSession.SessionExpiredException;
}
