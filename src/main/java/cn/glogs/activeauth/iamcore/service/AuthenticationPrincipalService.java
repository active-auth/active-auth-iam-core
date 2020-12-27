package cn.glogs.activeauth.iamcore.service;

import cn.glogs.activeauth.iamcore.domain.AuthenticationPrincipal;
import cn.glogs.activeauth.iamcore.exception.business.NotFoundException;
import cn.glogs.activeauth.iamcore.exception.business.PatternException;
import org.springframework.data.domain.Page;

public interface AuthenticationPrincipalService {

    AuthenticationPrincipal addPrincipal(String name, String password);

    AuthenticationPrincipal findPrincipalById(Long id) throws NotFoundException;

    Page<AuthenticationPrincipal> pagingPrincipals(int page, int size);

    AuthenticationPrincipal addSubprincipal(AuthenticationPrincipal owner, String name, String password);

    Page<AuthenticationPrincipal> pagingSubprincipals(AuthenticationPrincipal owner, int page, int size);
}
