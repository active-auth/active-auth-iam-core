package cn.glogs.activeauth.iamcore.service.impl;

import cn.glogs.activeauth.iamcore.domain.AuthenticationPrincipal;
import cn.glogs.activeauth.iamcore.exception.business.NotFoundException;
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

    public AuthenticationPrincipalServiceImpl(AuthenticationPrincipalRepository authenticationPrincipalRepository) {
        this.authenticationPrincipalRepository = authenticationPrincipalRepository;
    }

    @Override
    public AuthenticationPrincipal findPrincipalByName(String name) throws NotFoundException {
        return authenticationPrincipalRepository.findByName(name).orElseThrow(() -> new NotFoundException(String.format("Principal name:%s not found", name)));
    }

    @Override
    public AuthenticationPrincipal createPrincipal(AuthenticationPrincipal toCreatePrincipal) {
        authenticationPrincipalRepository.save(toCreatePrincipal);
        return toCreatePrincipal;
    }

    @Override
    @Transactional
    public AuthenticationPrincipal findPrincipalById(Long id) throws NotFoundException {
        return authenticationPrincipalRepository.findById(id).orElseThrow(() -> new NotFoundException(String.format("Principal id:%s not found", id)));
    }

    @Override
    public void updatePrincipal(Long id, AuthenticationPrincipal principal) {
        principal.setId(id);
        authenticationPrincipalRepository.save(principal);
    }

    @Override
    @Transactional
    public Page<AuthenticationPrincipal> pagingPrincipals(int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size);
        return authenticationPrincipalRepository.findAll(pageRequest);
    }

    @Override
    public void deletePrincipalById(Long principalId) {
        authenticationPrincipalRepository.deleteById(principalId);
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
