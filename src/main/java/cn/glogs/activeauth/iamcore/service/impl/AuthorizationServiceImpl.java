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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static cn.glogs.activeauth.iamcore.domain.AuthorizationPolicy.PolicyType.ALLOW;


@Service
@Slf4j
public class AuthorizationServiceImpl implements AuthorizationService {

    private final AuthorizationPolicyGrantRowRepository authorizationPolicyGrantRowRepository;

    public AuthorizationServiceImpl(AuthorizationPolicyGrantRowRepository authorizationPolicyGrantRowRepository) {
        this.authorizationPolicyGrantRowRepository = authorizationPolicyGrantRowRepository;
    }

    private boolean anyMatched(String testResource, List<String> policyResources) {
        boolean ayMatched = false;
        for (String policyResource : policyResources) {
            String policyResourceInRegex = "^" + policyResource.replaceAll("\\*", ".+") + "$";
            log.info("[Auth Challenging: checking any matched] policyResourceInRegex = {}, testResource = {}", policyResourceInRegex, testResource);
            if (Pattern.matches(policyResourceInRegex, testResource)) {
                log.info("[Auth Challenging: checking any matched, found] policyResourceInRegex = {}, testResource = {}", policyResourceInRegex, testResource);
                ayMatched = true;
            }
        }
        return ayMatched;
    }

    @Override
    @Transactional
    public boolean challenge(AuthenticationPrincipal challenger, String action, String... resources) {
        List<String> allowedResourcePolicies = new ArrayList<>();
        List<String> deniedResourcePolicies = new ArrayList<>();

        List<String> myResources = new ArrayList<>();
        List<String> notMyResources = new ArrayList<>();
        for (String resource : resources) {
            String pattern = String.format("^.+://users/%s/.*$", challenger.getId());
            if (Pattern.matches(pattern, resource)) {
                myResources.add(resource);
            } else {
                notMyResources.add(resource);
            }
        }
        log.info("[Auth Challenging: wildcarding] myResources = {}, notMyResources = {}.", myResources, notMyResources);

        List<AuthorizationPolicyGrantRow> rows = authorizationPolicyGrantRowRepository.findAllByGranteeAndPolicyAction(challenger, action);
        rows.forEach(row -> {
            if (row.getPolicy().getPolicyType() == ALLOW) {
                allowedResourcePolicies.add(row.getPolicyResource());
            } else {
                deniedResourcePolicies.add(row.getPolicyResource());
            }
        });
        log.info("[Auth Challenging: from DB] allowing = {}, denying = {}, rows = {}", allowedResourcePolicies, deniedResourcePolicies, rows);

        boolean myResourcesAllAllowed = true; // TODO: 支持制定规则，屏蔽用户访问自己的资源
        boolean notMyResourcesAllAllowed = notMyResources.isEmpty(); // Set true if notMyResource empty to ignore its boolean matter.
        if (!notMyResources.isEmpty()) {
            for (String notMyResource : notMyResources) {
                log.info("[Auth Challenging: checking any policy denied] resource = {}", notMyResource);
                boolean anyPolicyDenied = anyMatched(notMyResource, deniedResourcePolicies);
                log.info("[Auth Challenging: checking some policy allowed] resource = {}", notMyResource);
                boolean somePoliciesAllowed = anyMatched(notMyResource, allowedResourcePolicies);
                notMyResourcesAllAllowed = !anyPolicyDenied && somePoliciesAllowed;
            }
        }

        log.info("[Auth Challenging: {}] my = {}, notMy = {}, challenger = {}, action = {}, resources = {}", myResourcesAllAllowed && notMyResourcesAllAllowed ? "Allowed" : "Denied", myResourcesAllAllowed, notMyResourcesAllAllowed, challenger.resourceLocator(), action, Arrays.deepToString(resources));
        return myResourcesAllAllowed && notMyResourcesAllAllowed;
    }
}
