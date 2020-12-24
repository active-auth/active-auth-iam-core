package cn.glogs.activeauth.iamcore.service;

import cn.glogs.activeauth.iamcore.domain.AuthenticationPrincipal;
import cn.glogs.activeauth.iamcore.exception.business.NotFoundException;
import cn.glogs.activeauth.iamcore.exception.business.PatternException;

public interface AuthenticationPrincipalService {
    AuthenticationPrincipal addPrincipal(String name, String password);

    AuthenticationPrincipal findPrincipalById(Long id) throws NotFoundException;

    AuthenticationPrincipal findPrincipalByLocator(String locator) throws PatternException, NotFoundException;
}
