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
        if (m.find()) {
            String resourcePathPrefix = m.group(1);
            String resourceOwnerId = m.group(2);
            String resourceFullPath = m.group(3);

            String[] resourcePathStack = resourceFullPath.split("/");

            // any user
            wildcardedResourceLocators.add(resourcePathPrefix + "://users/*");
            for (int i = 1; i < resourcePathStack.length; i++) {
                StringBuilder sBuilder = new StringBuilder();
                sBuilder.append(resourcePathPrefix).append("://users/*");
                for (int j = 0; j < i; j++) {
                    sBuilder.append("/").append(resourcePathStack[j]);
                }
                wildcardedResourceLocators.add(sBuilder.append("/*").toString());
            }
            wildcardedResourceLocators.add(resourcePathPrefix + "://users/*/" + resourceFullPath);

            // this user
            for (int i = 0; i < resourcePathStack.length; i++) {
                StringBuilder sBuilder = new StringBuilder();
                sBuilder.append(resourcePathPrefix).append("://users/").append(resourceOwnerId);
                for (int j = 0; j < i; j++) {
                    sBuilder.append("/").append(resourcePathStack[j]);
                }
                wildcardedResourceLocators.add(sBuilder.append("/*").toString());
            }
        }
        wildcardedResourceLocators.add(fullLocator);
        return wildcardedResourceLocators;
    }

    @Override
    @Transactional
    public boolean challenge(AuthenticationPrincipal challenger, String action, String... resources) {
        Set<String> allowedResource = new HashSet<>();
        Set<String> deniedResource = new HashSet<>();

        // TODO: 支持制定规则，屏蔽用户访问自己的资源
        List<String> notMyResources = new ArrayList<>();
        for (String resource : resources) {
            String pattern = String.format("^.+://users/%s/.*$", challenger.getId());
            if (!Pattern.matches(pattern, resource)) {
                notMyResources.add(resource);
            }
        }

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
                    log.info("[Auth Challenging: Denied] challenger = {}, action = {}, resources = {}", challenger.resourceLocator(), action, Arrays.deepToString(resources));
                    return false;
                }
            }
        }
        log.info("[Auth Challenging: Allowed] challenger = {}, action = {}, resources = {}", challenger.resourceLocator(), action, Arrays.deepToString(resources));
        return true;
    }
}
