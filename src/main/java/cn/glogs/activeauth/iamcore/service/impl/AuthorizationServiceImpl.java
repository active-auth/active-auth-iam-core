package cn.glogs.activeauth.iamcore.service.impl;

import cn.glogs.activeauth.iamcore.config.properties.LocatorConfiguration;
import cn.glogs.activeauth.iamcore.domain.AuthenticationPrincipal;
import cn.glogs.activeauth.iamcore.domain.AuthorizationPolicy;
import cn.glogs.activeauth.iamcore.domain.AuthorizationPolicyGrantRow;
import cn.glogs.activeauth.iamcore.repository.AuthorizationPolicyGrantRowRepository;
import cn.glogs.activeauth.iamcore.service.AuthorizationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.regex.Pattern;

import static cn.glogs.activeauth.iamcore.domain.AuthorizationPolicy.PolicyEffect.ALLOW;


@Service
@Slf4j
public class AuthorizationServiceImpl implements AuthorizationService {

    private final AuthorizationPolicyGrantRowRepository authorizationPolicyGrantRowRepository;
    private final LocatorConfiguration locatorConfiguration;

    public AuthorizationServiceImpl(AuthorizationPolicyGrantRowRepository authorizationPolicyGrantRowRepository, LocatorConfiguration locatorConfiguration) {
        this.authorizationPolicyGrantRowRepository = authorizationPolicyGrantRowRepository;
        this.locatorConfiguration = locatorConfiguration;
    }

    public List<String> wildcardedActions(String originalAction) {
        List<String> result = new ArrayList<>();
        String[] spitsOfOriginalAction = originalAction.split(":");
        String serviceDomain = spitsOfOriginalAction[0];
        result.add(originalAction);
        result.add(serviceDomain + ":*");
        result.add("*");
        return result;
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
        List<String> allowedMyResourcePolicies = new ArrayList<>();
        List<String> allowedNotMyResourcePolicies = new ArrayList<>();
        List<String> deniedMyResourcePolicies = new ArrayList<>();
        List<String> deniedNotMyResourcePolicies = new ArrayList<>();

        String myResourcePattern = locatorConfiguration.myResourcePattern(challenger.getId());

        List<String> myResources = new ArrayList<>();
        List<String> notMyResources = new ArrayList<>();
        for (String resource : resources) {
            if (Pattern.matches(myResourcePattern, resource)) {
                myResources.add(resource);
            } else {
                notMyResources.add(resource);
            }
        }
        log.info("[Auth Challenging: wildcarding] myResources = {}, notMyResources = {}.", myResources, notMyResources);

        List<AuthorizationPolicyGrantRow> rows = new ArrayList<>();
        List<String> wildcardedActions = wildcardedActions(action);
        rows.addAll(authorizationPolicyGrantRowRepository.findAllByGranteeIdAndPolicyActionIn(challenger.getId(), wildcardedActions)); // current challenger as grantee
        rows.addAll(authorizationPolicyGrantRowRepository.findAllByGranteeIdAndPolicyActionIn(0L, wildcardedActions)); // global user as grantee
        rows.forEach(row -> {
            AuthorizationPolicy.PolicyEffect policyEffect = row.getPolicy().getEffect();
            String policyRowResource = row.getPolicyResource();
            if (Pattern.matches(myResourcePattern, policyRowResource)) {
                if (policyEffect == ALLOW) {
                    allowedMyResourcePolicies.add(policyRowResource);
                } else {
                    deniedMyResourcePolicies.add(policyRowResource);
                }
            } else {
                if (policyEffect == ALLOW) {
                    allowedNotMyResourcePolicies.add(policyRowResource);
                } else {
                    deniedNotMyResourcePolicies.add(policyRowResource);
                }
            }
        });
        log.info("[Auth Challenging: from DB] allowingMy = {}, denyingMy = {}, allowingNotMy = {}, denyingNotMy = {}, rows = {}", allowedMyResourcePolicies, deniedMyResourcePolicies, allowedNotMyResourcePolicies, deniedNotMyResourcePolicies, rows);

        // Allowing owner to it's own resources unless declared Denying in policy and grants.
        boolean myResourcesAllAllowed = true;
        for (String myResource : myResources) {
            log.info("[Auth Challenging: checking any policy denied] myResource = {}", myResource);
            boolean anyPolicyDenied = anyMatched(myResource, deniedMyResourcePolicies);
            myResourcesAllAllowed = !anyPolicyDenied;
        }

        boolean notMyResourcesAllAllowed = notMyResources.isEmpty(); // Set true if notMyResource empty to ignore its boolean matter.
        for (String notMyResource : notMyResources) {
            log.info("[Auth Challenging: checking any policy denied] notMyResource = {}", notMyResource);
            boolean anyPolicyDenied = anyMatched(notMyResource, deniedNotMyResourcePolicies);
            log.info("[Auth Challenging: checking some policy allowed] notMyResource = {}", notMyResource);
            boolean somePoliciesAllowed = anyMatched(notMyResource, allowedNotMyResourcePolicies);
            notMyResourcesAllAllowed = !anyPolicyDenied && somePoliciesAllowed;
        }

        log.info("[Auth Challenging: {}] my = {}, notMy = {}, challenger = {}, action = {}, resources = {}", myResourcesAllAllowed && notMyResourcesAllAllowed ? "Allowed" : "Denied", myResourcesAllAllowed, notMyResourcesAllAllowed, challenger.resourceLocator(locatorConfiguration), action, Arrays.deepToString(resources));
        return myResourcesAllAllowed && notMyResourcesAllAllowed;
    }
}
