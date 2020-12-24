package cn.glogs.activeauth.iamcore.service.impl;

import cn.glogs.activeauth.iamcore.domain.AuthenticationPrincipal;
import cn.glogs.activeauth.iamcore.domain.AuthorizationPolicy;
import cn.glogs.activeauth.iamcore.domain.AuthorizationPolicyGrant;
import cn.glogs.activeauth.iamcore.domain.AuthorizationPolicyGrantRow;
import cn.glogs.activeauth.iamcore.exception.business.NotFoundException;
import cn.glogs.activeauth.iamcore.exception.business.PatternException;
import cn.glogs.activeauth.iamcore.repository.AuthorizationPolicyGrantRepository;
import cn.glogs.activeauth.iamcore.repository.AuthorizationPolicyGrantRowRepository;
import cn.glogs.activeauth.iamcore.repository.AuthorizationPolicyRepository;
import cn.glogs.activeauth.iamcore.service.AuthorizationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static cn.glogs.activeauth.iamcore.domain.AuthorizationPolicy.PolicyType.ALLOW;


@Service
public class AuthorizationServiceImpl implements AuthorizationService {

    private final AuthorizationPolicyRepository authorizationPolicyRepository;
    private final AuthorizationPolicyGrantRepository authorizationPolicyGrantRepository;
    private final AuthorizationPolicyGrantRowRepository authorizationPolicyGrantRowRepository;

    public AuthorizationServiceImpl(AuthorizationPolicyRepository authorizationPolicyRepository, AuthorizationPolicyGrantRepository authorizationPolicyGrantRepository, AuthorizationPolicyGrantRowRepository authorizationPolicyGrantRowRepository) {
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
    public AuthorizationPolicy getPolicyByLocator(String locator) throws PatternException, NotFoundException {
        Long id = AuthorizationPolicy.idFromLocator(locator);
        return authorizationPolicyRepository.findById(id).orElseThrow(() -> new NotFoundException("Policy not found."));
    }

    @Override
    @Transactional
    public List<AuthorizationPolicyGrant> grantPolicies(AuthenticationPrincipal granter, AuthenticationPrincipal grantee, List<AuthorizationPolicy> policies) {
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

    private List<AuthorizationPolicyGrantRow> cartesianProduct(
            AuthenticationPrincipal granter, AuthenticationPrincipal grantee,
            AuthorizationPolicy policy,
            List<String> actions,
            List<String> resources
    ) {
        return actions.stream().flatMap(action -> resources.stream().map(resource -> new AuthorizationPolicyGrantRow(null, granter, grantee, policy, policy.getPolicyType(), action, resource, false))).collect(Collectors.toUnmodifiableList());
    }

    @Override
    @Transactional
    public boolean challenge(AuthenticationPrincipal challenger, String action, List<String> resources) {
        Set<String> allowedResource = new HashSet<>();
        Set<String> deniedResource = new HashSet<>();

        List<String> notMyResources = new ArrayList<>();
        resources.forEach(resource -> {
            String pattern = String.format("^.+://users/%s/.*$", challenger.getId());
            if (!Pattern.matches(pattern, resource)) {
                notMyResources.add(resource);
            }
        });

        if (notMyResources.size() > 0) {
            List<AuthorizationPolicyGrantRow> rows = authorizationPolicyGrantRowRepository.findAllByGranteeAndPolicyAction(challenger, action);
            rows.forEach(row -> {
                if (row.getPolicy().getPolicyType() == ALLOW) {
                    allowedResource.add(row.getPolicyResource());
                } else {
                    deniedResource.add(row.getPolicyResource());
                }
            });
            // TODO: 支持通配符 * 匹配
            for (String notMyResource : notMyResources) {
                if (!allowedResource.contains(notMyResource) || deniedResource.contains(notMyResource)) {
                    return false;
                }
            }
        }
        return true;
    }
}
