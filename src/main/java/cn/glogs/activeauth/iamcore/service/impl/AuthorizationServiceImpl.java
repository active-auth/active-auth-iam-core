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

    public static List<String> wildcardedResourceLocators(String fullLocator) {
        String resourcePattern = "^(.+)://users/(\\d+)/(.*)$";
        Pattern r = Pattern.compile(resourcePattern);
        Matcher m = r.matcher(fullLocator);
        List<String> wildcardedResourceLocators = new ArrayList<>();
        wildcardedResourceLocators.add(fullLocator);
        if (m.find()) {
            String resourcePathPrefix = m.group(1);
            String resourceOwnerId = m.group(2);
            String resourceFullPath = m.group(3);

            String[] resourcePathStack = resourceFullPath.split("/");

            // this user
            for (int i = resourcePathStack.length - 1; i >= 0; i--) {
                StringBuilder sBuilder = new StringBuilder();
                sBuilder.append(resourcePathPrefix).append("://users/").append(resourceOwnerId);
                for (int j = 0; j < i; j++) {
                    sBuilder.append("/").append(resourcePathStack[j]);
                }
                wildcardedResourceLocators.add(sBuilder.append("/*").toString());
            }

            // any user
            wildcardedResourceLocators.add(resourcePathPrefix + "://users/*/" + resourceFullPath);
            for (int i = resourcePathStack.length - 1; i > 0; i--) {
                StringBuilder sBuilder = new StringBuilder();
                sBuilder.append(resourcePathPrefix).append("://users/*");
                for (int j = 0; j < i; j++) {
                    sBuilder.append("/").append(resourcePathStack[j]);
                }
                wildcardedResourceLocators.add(sBuilder.append("/*").toString());
            }
            wildcardedResourceLocators.add(resourcePathPrefix + "://users/*");

        }
        return wildcardedResourceLocators;
    }

    @Override
    @Transactional
    public boolean challenge(AuthenticationPrincipal challenger, String action, String... resources) {
        Set<String> allowedResource = new HashSet<>();
        Set<String> deniedResource = new HashSet<>();

        // TODO: 支持制定规则，屏蔽用户访问自己的资源
        List<String> notMyResourceWildcards = new ArrayList<>();
        for (String resource : resources) {
            String pattern = String.format("^.+://users/%s/.*$", challenger.getId());
            if (!Pattern.matches(pattern, resource)) {
                notMyResourceWildcards.addAll(wildcardedResourceLocators(resource));
            }
        }

        log.info("[Auth Challenging: Wildcarding] {}.", notMyResourceWildcards);

        if (notMyResourceWildcards.size() > 0) {
            List<AuthorizationPolicyGrantRow> rows = authorizationPolicyGrantRowRepository.findAllByGranteeAndPolicyAction(challenger, action);
            rows.forEach(row -> {
                if (row.getPolicy().getPolicyType() == ALLOW) {
                    allowedResource.add(row.getPolicyResource());
                } else {
                    deniedResource.add(row.getPolicyResource());
                }
            });
            log.info("[Auth Challenging: From DB] rows = {}, allowing = {}, denying = {}", rows, allowedResource, deniedResource);

            boolean proceed = false;
            for (String notMyResourceWildcard : notMyResourceWildcards) {
                // DENY if one of resources is not met, before checking allowance.
                if (deniedResource.contains(notMyResourceWildcard)) {
                    log.info("[Auth Challenging: Denied before checking allowance] challenger = {}, action = {}, deniedResourceWildcard = {}", challenger.resourceLocator(), action, Arrays.deepToString(resources));
                    return false;
                }
                // ALLOW if one of resources is met, after checking denials.
                if (allowedResource.contains(notMyResourceWildcard)) {
                    log.info("[Auth Challenging: Allowed after checking denials proceeded] challenger = {}, action = {}, allowedResourceWildcard = {}", challenger.resourceLocator(), action, notMyResourceWildcard);
                    proceed = true;
                }
            }
            log.info("[Auth Challenging: {}] challenger = {}, action = {}, resources = {}", proceed ? "Allowed" : "Denied", challenger.resourceLocator(), action, Arrays.deepToString(resources));
            return proceed;
        }
        log.info("[Auth Challenging: Allowed Default] challenger = {}, action = {}, resources = {}", challenger.resourceLocator(), action, Arrays.deepToString(resources));
        return true;
    }
}
