package cn.glogs.activeauth.iamcore.service.impl;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AuthorizationServiceImplTest {

    @Test
    void wildcardedResourceLocators() {
        String full = "bookshop://users/7/books/scifi/liucixin/908";
        List<String> locators = AuthorizationServiceImpl.wildcardedResourceLocators(full);
        locators.forEach(System.out::println);
        assertEquals(locators.size(), 10);
    }
}