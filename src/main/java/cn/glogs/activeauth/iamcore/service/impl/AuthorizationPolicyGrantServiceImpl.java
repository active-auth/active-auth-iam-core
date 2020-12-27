package cn.glogs.activeauth.iamcore.service.impl;

import cn.glogs.activeauth.iamcore.domain.AuthenticationPrincipal;
import cn.glogs.activeauth.iamcore.domain.AuthorizationPolicy;
import cn.glogs.activeauth.iamcore.domain.AuthorizationPolicyGrant;
import cn.glogs.activeauth.iamcore.domain.AuthorizationPolicyGrantRow;
import cn.glogs.activeauth.iamcore.repository.AuthorizationPolicyGrantRepository;
import cn.glogs.activeauth.iamcore.repository.AuthorizationPolicyGrantRowRepository;
import cn.glogs.activeauth.iamcore.service.AuthorizationPolicyGrantService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.webjars.NotFoundException;

import javax.persistence.criteria.*;
import java.util.*;
import java.util.stream.Collectors;


@Service
public class AuthorizationPolicyGrantServiceImpl implements AuthorizationPolicyGrantService {

    private final AuthorizationPolicyGrantRepository authorizationPolicyGrantRepository;
    private final AuthorizationPolicyGrantRowRepository authorizationPolicyGrantRowRepository;

    public AuthorizationPolicyGrantServiceImpl(AuthorizationPolicyGrantRepository authorizationPolicyGrantRepository, AuthorizationPolicyGrantRowRepository authorizationPolicyGrantRowRepository) {
        this.authorizationPolicyGrantRepository = authorizationPolicyGrantRepository;
        this.authorizationPolicyGrantRowRepository = authorizationPolicyGrantRowRepository;
    }

    @Override
    @Transactional
    public List<AuthorizationPolicyGrant> addGrants(AuthenticationPrincipal granter, AuthenticationPrincipal grantee, List<AuthorizationPolicy> policies) {
        List<AuthorizationPolicyGrant> savedGrants = new ArrayList<>();
        policies.forEach(policy -> {
            AuthorizationPolicyGrant toBeSavedGrant = new AuthorizationPolicyGrant();
            toBeSavedGrant.setGranter(granter);
            toBeSavedGrant.setGrantee(grantee);
            toBeSavedGrant.setPolicy(policy);
            toBeSavedGrant.setRevoked(false);
            toBeSavedGrant.setCreatedAt(new Date());
            AuthorizationPolicyGrant savedGrant = authorizationPolicyGrantRepository.save(toBeSavedGrant);
            savedGrants.add(savedGrant);
            List<AuthorizationPolicyGrantRow> toBeSavedGrantRows = cartesianProduct(granter, grantee, policy, policy.getActions(), policy.getResources());
            authorizationPolicyGrantRowRepository.saveAll(toBeSavedGrantRows);
        });
        return savedGrants;
    }

    @Override
    @Transactional
    public AuthorizationPolicyGrant getGrantById(Long id) {
        return authorizationPolicyGrantRepository.findById(id).orElseThrow(() -> new NotFoundException("Grant not found."));
    }

    @Override
    @Transactional
    public Page<AuthorizationPolicyGrant> pagingGrantsFrom(AuthenticationPrincipal granter, int page, int size) {
        return authorizationPolicyGrantRepository.findAll((Specification<AuthorizationPolicyGrant>) (root, query, criteriaBuilder) -> {
            Path<AuthenticationPrincipal> granterRoot = root.get("granter");
            return criteriaBuilder.equal(granterRoot, granter);
        }, PageRequest.of(page, size));
    }

    @Override
    @Transactional
    public Page<AuthorizationPolicyGrant> pagingGrantsTo(AuthenticationPrincipal grantee, int page, int size) {
        return authorizationPolicyGrantRepository.findAll((Specification<AuthorizationPolicyGrant>) (root, query, criteriaBuilder) -> {
            Path<AuthenticationPrincipal> granteeRoot = root.get("grantee");
            return criteriaBuilder.equal(granteeRoot, grantee);
        }, PageRequest.of(page, size));
    }

    @Override
    @Transactional
    public void deleteGrant(Long grantId) {
        authorizationPolicyGrantRepository.findById(grantId);
        List<AuthorizationPolicyGrantRow> grantRows = authorizationPolicyGrantRowRepository.findAllByGranterId(grantId);
        authorizationPolicyGrantRowRepository.deleteInBatch(grantRows);
    }

    private List<AuthorizationPolicyGrantRow> cartesianProduct(
            AuthenticationPrincipal granter, AuthenticationPrincipal grantee,
            AuthorizationPolicy policy,
            List<String> actions,
            List<String> resources
    ) {
        return actions.stream().flatMap(action -> resources.stream().map(resource -> new AuthorizationPolicyGrantRow(null, granter, grantee, policy, policy.getPolicyType(), action, resource, false))).collect(Collectors.toUnmodifiableList());
    }
}
