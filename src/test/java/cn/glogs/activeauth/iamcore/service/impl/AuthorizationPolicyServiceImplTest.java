package cn.glogs.activeauth.iamcore.service.impl;

import cn.glogs.activeauth.iamcore.domain.AuthenticationPrincipal;
import cn.glogs.activeauth.iamcore.domain.AuthorizationPolicy;
import cn.glogs.activeauth.iamcore.domain.password.PasswordHashingStrategy;
import cn.glogs.activeauth.iamcore.exception.business.NotFoundException;
import cn.glogs.activeauth.iamcore.repository.AuthenticationPrincipalRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static cn.glogs.activeauth.iamcore.domain.AuthorizationPolicy.PolicyEffect.ALLOW;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class AuthorizationPolicyServiceImplTest {

    @Autowired
    private AuthenticationPrincipalRepository principalRepository;

    @Autowired
    private AuthorizationPolicyServiceImpl policyService;

    private AuthenticationPrincipal sampleOwner;

    @BeforeEach
    void init() {
        AuthenticationPrincipal toSavePrincipal = new AuthenticationPrincipal("mayun", "IAmMaYun", AuthenticationPrincipal.PrincipalType.PRINCIPAL, PasswordHashingStrategy.B_CRYPT);
        this.sampleOwner = principalRepository.save(toSavePrincipal);
    }

    @Test
    @Transactional
    AuthorizationPolicy addPolicyTest() {
        AuthorizationPolicy.Form sampleForm = new AuthorizationPolicy.Form(
                "JackMa's Principal - 1", ALLOW,
                List.of("bookshelf:AddBooks"),
                List.of("arn:cloudapp:bookshelf::31:bought-book/*")
        );
        return policyService.addPolicy(sampleForm, this.sampleOwner);
    }

    @Test
    @Transactional
    void editPolicyTest() throws NotFoundException {
        AuthorizationPolicy toEditPolicy = addPolicyTest();
        AuthorizationPolicy.Form sampleForm = new AuthorizationPolicy.Form(
                "JackMa's Principal - 2", ALLOW,
                List.of("bookshelf:AddBooks", "bookshelf:DeleteBooks"),
                List.of("arn:cloudapp:bookshelf::31:bought-book/*", "arn:cloudapp:bookshelf::31:favorites/*")
        );
        policyService.editPolicy(toEditPolicy.getId(), sampleForm, this.sampleOwner);
        assertThrows(NotFoundException.class, () ->
                policyService.editPolicy(toEditPolicy.getId() + 1, sampleForm, this.sampleOwner)
        );
    }
}