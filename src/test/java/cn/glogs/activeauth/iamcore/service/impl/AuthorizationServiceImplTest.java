package cn.glogs.activeauth.iamcore.service.impl;

import org.junit.jupiter.api.Test;

import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertTrue;

class AuthorizationServiceImplTest {
    @Test
    void wildcardReplace() {
        String policy = "bookshop://users/7/books/*/liucixin/*";
        String policyToCheck = "bookshop://users/7/books/scifi/liucixin/78994";
        String policyPattern = "^" + policy.replaceAll("\\*", ".+") + "$";
        assertTrue(Pattern.matches(policyPattern, policyToCheck));
    }
}