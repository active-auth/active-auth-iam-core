package cn.glogs.activeauth.iamcore.service.impl;

import cn.glogs.activeauth.iamcore.config.properties.Configuration;
import cn.glogs.activeauth.iamcore.domain.AuthenticationPrincipal;
import cn.glogs.activeauth.iamcore.exception.business.NotFoundException;
import cn.glogs.activeauth.iamcore.exception.business.PatternException;
import cn.glogs.activeauth.iamcore.repository.AuthenticationPrincipalRepository;
import cn.glogs.activeauth.iamcore.service.AuthenticationPrincipalService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.Path;

@Service
public class AuthenticationPrincipalServiceImpl implements AuthenticationPrincipalService {

    private final AuthenticationPrincipalRepository authenticationPrincipalRepository;
    private final Configuration configuration;

    public AuthenticationPrincipalServiceImpl(AuthenticationPrincipalRepository authenticationPrincipalRepository, Configuration configuration) {
        this.authenticationPrincipalRepository = authenticationPrincipalRepository;
        this.configuration = configuration;
    }

    @Override
    public AuthenticationPrincipal createPrincipal(AuthenticationPrincipal toCreatePrincipal) {
        authenticationPrincipalRepository.save(toCreatePrincipal);
        return toCreatePrincipal;
    }

    @Override
    @Transactional
    public AuthenticationPrincipal findPrincipalById(Long id) throws NotFoundException {
        return authenticationPrincipalRepository.findById(id).orElseThrow(() -> new NotFoundException("Principal not found"));
    }

    @Override
    @Transactional
    public Page<AuthenticationPrincipal> pagingPrincipals(int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size);
        return authenticationPrincipalRepository.findAll(pageRequest);
    }

    @Override
    public AuthenticationPrincipal createSubprincipal(AuthenticationPrincipal owner, String name, String password) {
        AuthenticationPrincipal authenticationPrincipal = AuthenticationPrincipal.createPrincipal(name, password, configuration.getPasswordHashingStrategy(), AuthenticationPrincipal.PrincipalType.PRINCIPAL).withOwner(owner);
        authenticationPrincipalRepository.save(authenticationPrincipal);
        return authenticationPrincipal;
    }

    @Override
    public Page<AuthenticationPrincipal> pagingSubprincipals(AuthenticationPrincipal owner, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size);
        return authenticationPrincipalRepository.findAll((Specification<AuthenticationPrincipal>) (root, query, criteriaBuilder) -> {
            Path<AuthenticationPrincipal> principalField = root.get("owner");
            Path<AuthenticationPrincipal.PrincipalType> principalTypeField = root.get("principalType");
            return criteriaBuilder.and(
                    criteriaBuilder.equal(principalField, owner),
                    criteriaBuilder.equal(principalTypeField, AuthenticationPrincipal.PrincipalType.PRINCIPAL)
            );
        }, pageRequest);
    }

    @Override
    public AuthenticationPrincipal createPrincipalGroup(AuthenticationPrincipal owner, AuthenticationPrincipal principalGroup) {
        principalGroup.setOwner(owner);
        return null;
    }

    @Override
    public Page<AuthenticationPrincipal> pagingPrincipalGroups(AuthenticationPrincipal owner, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size);
        return authenticationPrincipalRepository.findAll((Specification<AuthenticationPrincipal>) (root, query, criteriaBuilder) -> {
            Path<AuthenticationPrincipal> principalField = root.get("owner");
            Path<AuthenticationPrincipal.PrincipalType> principalTypeField = root.get("principalType");
            return criteriaBuilder.and(
                    criteriaBuilder.equal(principalField, owner),
                    criteriaBuilder.equal(principalTypeField, AuthenticationPrincipal.PrincipalType.PRINCIPAL_GROUP)
            );
        }, pageRequest);
    }

    @Override
    public Page<AuthenticationPrincipal> pagingAppDomains(AuthenticationPrincipal owner, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size);
        return authenticationPrincipalRepository.findAll((Specification<AuthenticationPrincipal>) (root, query, criteriaBuilder) -> {
            Path<AuthenticationPrincipal> principalField = root.get("owner");
            Path<AuthenticationPrincipal.PrincipalType> principalTypeField = root.get("principalType");
            return criteriaBuilder.and(
                    criteriaBuilder.equal(principalField, owner),
                    criteriaBuilder.equal(principalTypeField, AuthenticationPrincipal.PrincipalType.APP_DOMAIN)
            );
        }, pageRequest);
    }
}
