package cn.glogs.activeauth.iamcore.service.impl;

import cn.glogs.activeauth.iamcore.config.properties.Configuration;
import cn.glogs.activeauth.iamcore.domain.AuthenticationPrincipal;
import cn.glogs.activeauth.iamcore.exception.business.NotFoundException;
import cn.glogs.activeauth.iamcore.exception.business.PatternException;
import cn.glogs.activeauth.iamcore.repository.AuthenticationPrincipalRepository;
import cn.glogs.activeauth.iamcore.service.AuthenticationPrincipalService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthenticationPrincipalServiceImpl implements AuthenticationPrincipalService {

    private final AuthenticationPrincipalRepository authenticationPrincipalRepository;
    private final Configuration configuration;

    public AuthenticationPrincipalServiceImpl(AuthenticationPrincipalRepository authenticationPrincipalRepository, Configuration configuration) {
        this.authenticationPrincipalRepository = authenticationPrincipalRepository;
        this.configuration = configuration;
    }

    @Override
    @Transactional
    public AuthenticationPrincipal addPrincipal(String name, String password) {
        AuthenticationPrincipal authenticationPrincipal = AuthenticationPrincipal.createPrincipal(name, password, configuration.getPasswordHashingStrategy());
        authenticationPrincipalRepository.save(authenticationPrincipal);
        return authenticationPrincipal;
    }

    @Override
    @Transactional
    public AuthenticationPrincipal findPrincipalById(Long id) throws NotFoundException {
        return authenticationPrincipalRepository.findById(id).orElseThrow(() -> new NotFoundException("Principal not found"));
    }

    @Override
    @Transactional
    public AuthenticationPrincipal findPrincipalByLocator(String locator) throws PatternException, NotFoundException {
        return authenticationPrincipalRepository.findById(AuthenticationPrincipal.idFromLocator(locator)).orElseThrow(() -> new NotFoundException("Principal not found"));

    }
}
