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

    @Override
    @Transactional
    public boolean challenge(AuthenticationPrincipal challenger, String action, String... resources) {
        List<String> allowedResourcePolicies = new ArrayList<>();
        List<String> deniedResourcePolicies = new ArrayList<>();

        // TODO: 支持制定规则，屏蔽用户访问自己的资源
        List<String> notMyResources = new ArrayList<>();
        for (String resource : resources) {
            String pattern = String.format("^.+://users/%s/.*$", challenger.getId());
            if (!Pattern.matches(pattern, resource)) {
                notMyResources.add(resource);
            }
        }

        log.info("[Auth Challenging: Wildcarding] {}.", notMyResources);

        if (notMyResources.size() > 0) {
            List<AuthorizationPolicyGrantRow> rows = authorizationPolicyGrantRowRepository.findAllByGranteeAndPolicyAction(challenger, action);
            rows.forEach(row -> {
                if (row.getPolicy().getPolicyType() == ALLOW) {
                    allowedResourcePolicies.add(row.getPolicyResource());
                } else {
                    deniedResourcePolicies.add(row.getPolicyResource());
                }
            });
            log.info("[Auth Challenging: From DB] rows = {}, allowing = {}, denying = {}", rows, allowedResourcePolicies, deniedResourcePolicies);

            boolean allResourcesAllowed = true;
            for (String notMyResource : notMyResources) {
                for (String deniedResourcePolicy : deniedResourcePolicies) {
                    // 有一个非己资源被禁止列表中的任意 pattern 匹配都会拒绝整个请求
                    String deniedResourceRegex = "^" + deniedResourcePolicy.replaceAll("\\*", ".+") + "$";
                    log.info("[Auth Challenging: Checking Denials] deniedResourcePolicyInRegexFormat = {}, checkedResource = {}", deniedResourceRegex, notMyResource);
                    if (Pattern.matches(deniedResourceRegex, notMyResource)) {
                        allResourcesAllowed = false;
                    }
                }
                boolean currentResourceAllowed = false;
                for (String allowedResourcePolicy : allowedResourcePolicies) {
                    // 当前资源被其中一个允许规则匹配，则当前资源被允许；没有被任一允许规则匹配则当前资源不被允许，所有资源被允许则整个请求被允许
                    String allowedResourceRegex = "^" + allowedResourcePolicy.replaceAll("\\*", ".+") + "$";
                    log.info("[Auth Challenging: Checking Allowances] allowedResourcePolicyInRegexFormat = {}, checkedResource = {}", allowedResourceRegex, notMyResource);
                    if (Pattern.matches(allowedResourceRegex, notMyResource)) {
                        currentResourceAllowed = true;
                    }
                }
                if (!currentResourceAllowed) {
                    allResourcesAllowed = false;
                }
            }
            log.info("[Auth Challenging: {}] challenger = {}, action = {}, resources = {}", allResourcesAllowed ? "Allowed" : "Denied", challenger.resourceLocator(), action, Arrays.deepToString(resources));
            return allResourcesAllowed;
        }
        log.info("[Auth Challenging: Allowed Default] challenger = {}, action = {}, resources = {}", challenger.resourceLocator(), action, Arrays.deepToString(resources));
        return true;
    }
}
