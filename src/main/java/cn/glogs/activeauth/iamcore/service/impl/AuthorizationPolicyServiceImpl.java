package cn.glogs.activeauth.iamcore.service.impl;

import cn.glogs.activeauth.iamcore.domain.AuthenticationPrincipal;
import cn.glogs.activeauth.iamcore.domain.AuthorizationPolicy;
import cn.glogs.activeauth.iamcore.domain.AuthorizationPolicyGrant;
import cn.glogs.activeauth.iamcore.domain.AuthorizationPolicyGrantRow;
import cn.glogs.activeauth.iamcore.exception.business.NotFoundException;
import cn.glogs.activeauth.iamcore.repository.AuthorizationPolicyGrantRepository;
import cn.glogs.activeauth.iamcore.repository.AuthorizationPolicyGrantRowRepository;
import cn.glogs.activeauth.iamcore.repository.AuthorizationPolicyRepository;
import cn.glogs.activeauth.iamcore.service.AuthorizationPolicyService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.*;
import java.util.List;


@Service
public class AuthorizationPolicyServiceImpl implements AuthorizationPolicyService {

    private final AuthorizationPolicyRepository authorizationPolicyRepository;
    private final AuthorizationPolicyGrantRepository authorizationPolicyGrantRepository;
    private final AuthorizationPolicyGrantRowRepository authorizationPolicyGrantRowRepository;

    public AuthorizationPolicyServiceImpl(AuthorizationPolicyRepository authorizationPolicyRepository, AuthorizationPolicyGrantRepository authorizationPolicyGrantRepository, AuthorizationPolicyGrantRowRepository authorizationPolicyGrantRowRepository) {
        this.authorizationPolicyRepository = authorizationPolicyRepository;
        this.authorizationPolicyGrantRepository = authorizationPolicyGrantRepository;
        this.authorizationPolicyGrantRowRepository = authorizationPolicyGrantRowRepository;
    }

    @Override
    @Transactional
    public AuthorizationPolicy addPolicy(AuthenticationPrincipal owner, AuthorizationPolicy.Form form) {
        AuthorizationPolicy policyToBeSaved = new AuthorizationPolicy();
        policyToBeSaved.setOwner(owner);
        policyToBeSaved.setName(form.getName());
        policyToBeSaved.setPolicyType(form.getPolicyType());
        policyToBeSaved.setActions(form.getActions());
        policyToBeSaved.setResources(form.getResources());
        return authorizationPolicyRepository.save(policyToBeSaved);
    }

    @Override
    @Transactional
    public AuthorizationPolicy getPolicyById(Long id) throws NotFoundException {
        return authorizationPolicyRepository.findById(id).orElseThrow(() -> new NotFoundException("Policy not found."));
    }

    @Override
    @Transactional
    public Page<AuthorizationPolicy> pagingPolicies(AuthenticationPrincipal owner, int page, int size) {
        return authorizationPolicyRepository.findAll((Specification<AuthorizationPolicy>) (root, query, criteriaBuilder) -> {
            Path<AuthenticationPrincipal> ownerPath = root.get("owner");
            return criteriaBuilder.equal(ownerPath, owner);
        }, PageRequest.of(page, size));
    }

    @Override
    @Transactional
    public void deletePolicy(Long id) {
        List<AuthorizationPolicyGrantRow> grantRows = authorizationPolicyGrantRowRepository.findAllByPolicyId(id);
        authorizationPolicyGrantRowRepository.deleteInBatch(grantRows);
        List<AuthorizationPolicyGrant> grants = authorizationPolicyGrantRepository.findAllByPolicyId(id);
        authorizationPolicyGrantRepository.deleteInBatch(grants);
        authorizationPolicyRepository.deleteById(id);
    }
}
