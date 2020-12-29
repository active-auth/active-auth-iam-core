package cn.glogs.activeauth.iamcore.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AuthorizationServiceImplTest {

    @Test
    void wildcardedResourceLocators() {
        String full = "bookshop://users/769/books/scifi/liucixin/908";
        List<String> locators = AuthorizationServiceImpl.wildcardedResourceLocators(full);
        locators.forEach(System.out::println);
        assertEquals(locators.size(), 5);
    }
}