package cn.glogs.activeauth.iamcore.api;

import cn.glogs.activeauth.iamcore.api.payload.AuthorizationChallengeForm;
import cn.glogs.activeauth.iamcore.api.payload.AuthorizationPolicyGrantingForm;
import cn.glogs.activeauth.iamcore.config.properties.AuthConfiguration;
import cn.glogs.activeauth.iamcore.config.properties.LocatorConfiguration;
import cn.glogs.activeauth.iamcore.domain.*;
import cn.glogs.activeauth.iamcore.domain.sign.HTTPSignatureRsaSha256Signer;
import cn.glogs.activeauth.iamcore.util.ResponseContentMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.LinkedMultiValueMap;

import java.util.*;

@SpringBootTest
@AutoConfigureMockMvc
class AuthorizationApiTests {

    private final TestRequestTool testRequestTool;
    private final LocatorConfiguration locatorConfiguration;
    private final String timestampHeaderName;
    private static final Base64.Decoder base64Decoder = Base64.getDecoder();


    private static final String user1Username = "ponyma";
    private static final String user1Password = "pony199210251311";

    private static final String user2Username = "jackma";
    private static final String user2Password = "1@mJ@ckm@!";

    private AuthenticationPrincipal.Vo user1Principal;
    private AuthenticationPrincipal.Vo user2Principal;

    private AuthenticationSession.Vo user1Session;
    private AuthenticationSession.Vo user2Session;

    private AuthenticationPrincipalSecretKey.Vo user1KeyPair;
    private AuthenticationPrincipalSecretKey.Vo user2KeyPair;

    private AuthorizationPolicy.Vo user1TestPolicy;
    private AuthorizationPolicy.Vo user1TestPolicy_a;
    private AuthorizationPolicy.Vo user1TestPolicy_b;
    private AuthorizationPolicy.Vo user1TestPolicy_c;
    private AuthorizationPolicy.Vo user2TestPolicy;
    private List<AuthorizationPolicyGrant.Vo> testGrants;

    @Autowired
    public AuthorizationApiTests(MockMvc mockMvc, AuthConfiguration authConfiguration, LocatorConfiguration locatorConfiguration) {
        this.testRequestTool = new TestRequestTool(mockMvc, authConfiguration);
        this.locatorConfiguration = locatorConfiguration;
        this.timestampHeaderName = authConfiguration.getTimestampHeaderName();
    }

    @BeforeEach
    void setUp() throws Exception {
        // user-1 Register
        AuthenticationPrincipal.UserRegisterForm user1RegisterForm = new AuthenticationPrincipal.UserRegisterForm(user1Username, user1Password);
        String user1RegisterResponseContent = testRequestTool.post("/user-center/user/register", user1RegisterForm, null);
        this.user1Principal = ResponseContentMapper.getPackedReturningBody(user1RegisterResponseContent, AuthenticationPrincipal.Vo.class);

        // user-2 Register
        AuthenticationPrincipal.UserRegisterForm user2RegisterForm = new AuthenticationPrincipal.UserRegisterForm(user2Username, user2Password);
        String user2RegisterResponseContent = testRequestTool.post("/user-center/user/register", user2RegisterForm, null);
        this.user2Principal = ResponseContentMapper.getPackedReturningBody(user2RegisterResponseContent, AuthenticationPrincipal.Vo.class);


        // user-1 Login
        AuthenticationSession.UserLoginForm user1LoginForm = new AuthenticationSession.UserLoginForm(user1Username, user1Password);
        String user1LoginResponseContent = testRequestTool.post("/user-center/user/login", user1LoginForm, null);
        this.user1Session = ResponseContentMapper.getPackedReturningBody(user1LoginResponseContent, AuthenticationSession.Vo.class);

        // user-2 Login
        AuthenticationSession.UserLoginForm user2LoginForm = new AuthenticationSession.UserLoginForm(user2Username, user2Password);
        String user2LoginResponseContent = testRequestTool.post("/user-center/user/login", user2LoginForm, null);
        this.user2Session = ResponseContentMapper.getPackedReturningBody(user2LoginResponseContent, AuthenticationSession.Vo.class);

        // user-1 create key-pairs
        AuthenticationPrincipalSecretKey.GenKeyPairForm user1GenKeyPairForm = new AuthenticationPrincipalSecretKey.GenKeyPairForm("keypair of user1");
        String user1GenKeyPairResponseContent = testRequestTool.post("/principals/current/secret-keys/rsa2048-key-pairs", user1GenKeyPairForm, user1Session.getToken());
        this.user1KeyPair = ResponseContentMapper.getPackedReturningBody(user1GenKeyPairResponseContent, AuthenticationPrincipalSecretKey.Vo.class);

        // user-2 create key-pairs
        AuthenticationPrincipalSecretKey.GenKeyPairForm user2GenKeyPairForm = new AuthenticationPrincipalSecretKey.GenKeyPairForm("keypair of user2");
        String user2GenKeyPairResponseContent = testRequestTool.post("/principals/current/secret-keys/rsa2048-key-pairs", user2GenKeyPairForm, user2Session.getToken());
        this.user2KeyPair = ResponseContentMapper.getPackedReturningBody(user2GenKeyPairResponseContent, AuthenticationPrincipalSecretKey.Vo.class);
    }

    @Test
    @Transactional
    void testCreatePolicy() throws Exception {
        // create policy-1 of user-1
        String user1Locator = user1Principal.getResourceLocator();
        Long user1Id = AuthenticationPrincipal.idFromLocator(locatorConfiguration, user1Locator);
        AuthorizationPolicy.Form createPolicy1Form = new AuthorizationPolicy.Form();
        createPolicy1Form.setName("policy-1: Pony's bookshelf");
        createPolicy1Form.setEffect(AuthorizationPolicy.PolicyEffect.ALLOW);
        createPolicy1Form.setActions(List.of("bookshelf:getBook"));
        createPolicy1Form.setResources(List.of(
                new Locator("crn", "cloudapp-cn", "bookshelf", "", user1Id.toString(), "bought-books").toString(),
                new Locator("crn", "cloudapp-cn", "bookshelf", "", user1Id.toString(), "in-chart-books").toString()
        ));

        String createPolicy1ResponseContent = testRequestTool.post("/principals/current/policies", createPolicy1Form, user1Session.getToken());
        this.user1TestPolicy = ResponseContentMapper.getPackedReturningBody(createPolicy1ResponseContent, AuthorizationPolicy.Vo.class);

        // create policy-2 of user-2
        String user2Locator = user2Principal.getResourceLocator();
        Long user2Id = AuthenticationPrincipal.idFromLocator(locatorConfiguration, user2Locator);
        AuthorizationPolicy.Form createPolicy2Form = new AuthorizationPolicy.Form();
        createPolicy2Form.setName("policy-2: Jack's petshop");
        createPolicy2Form.setEffect(AuthorizationPolicy.PolicyEffect.ALLOW);
        createPolicy2Form.setActions(List.of("petshop:getCat", "petshop:getDog"));
        createPolicy2Form.setResources(List.of(
                new Locator("crn", "cloudapp-cn", "petshop", "", user2Id.toString(), "bought-cats").toString(),
                new Locator("crn", "cloudapp-cn", "petshop", "", user2Id.toString(), "bought-dogs").toString()
        ));

        String createPolicy2ResponseContent = testRequestTool.post("/principals/current/policies", createPolicy2Form, user2Session.getToken());
        this.user2TestPolicy = ResponseContentMapper.getPackedReturningBody(createPolicy2ResponseContent, AuthorizationPolicy.Vo.class);
    }

    @Test
    @Transactional
    void testAddGrant() throws Exception {
        testCreatePolicy();

        // test allowed grant for: user1 >- policy1 -> user2, expecting 2xx.
        AuthorizationPolicyGrantingForm grantingForm1 = new AuthorizationPolicyGrantingForm();
        grantingForm1.setGrantee(user2Principal.getResourceLocator());
        grantingForm1.setPolicies(List.of(user1TestPolicy.getResourceLocator()));
        String createGrantResponsePolicy = testRequestTool.post("/principals/current/grants", grantingForm1, user1Session.getToken());
        this.testGrants = ResponseContentMapper.getPackedReturningList(createGrantResponsePolicy, AuthorizationPolicyGrant.Vo.class);

        // test denied grant for: user1 >- policy2 -> user1, expecting 403.
        AuthorizationPolicyGrantingForm grantingForm2 = new AuthorizationPolicyGrantingForm();
        grantingForm2.setGrantee(user1Principal.getResourceLocator());
        grantingForm2.setPolicies(List.of(user2TestPolicy.getResourceLocator()));
        testRequestTool.post("/principals/current/grants", grantingForm2, user1Session.getToken(), TestRequestTool._403);
    }

    @Test
    @Transactional
    void testDeleteGrant() throws Exception {
        testAddGrant();

        Assert.isTrue(testGrants.size() > 0, "not grants");
        for (AuthorizationPolicyGrant.Vo grant : testGrants) {
            testRequestTool.delete("/principals/current/grants/" + grant.getId(), user1Session.getToken());
        }
    }

    @Test
    @Transactional
    void testChallengingAuthorities() throws Exception {
        testAddGrant();

        Long user1Id = AuthenticationPrincipal.idFromLocator(locatorConfiguration, user1Principal.getResourceLocator());
        AuthorizationChallengeForm challengingForm = new AuthorizationChallengeForm();
        challengingForm.setAction("bookshelf:getBook");
        challengingForm.setResources(List.of(new Locator("crn", "cloudapp-cn", "bookshelf", "", user1Id.toString(), "favorite-books").toString()));

        // user1 challenging its own resource
        testRequestTool.post("/principals/current/authorization-challengings", challengingForm, user1Session.getToken());

        // user2 challenging user1's resource while user1 or system not allowed
        testRequestTool.post("/principals/current/authorization-challengings", challengingForm, user2Session.getToken(), TestRequestTool._403);

        // user1 challenging a wrong resource
        challengingForm.setResources(List.of(new Locator("crn", "cloudapp-cn", "bookshelf", "", String.valueOf((user1Id + 2)), "bought-books").toString()));
        testRequestTool.post("/principals/current/authorization-challengings", challengingForm, user1Session.getToken(), TestRequestTool._403);
    }

    @Test
    @Transactional
    void testCreatePolicyInWildcard() throws Exception {
        // create policy-1a of user-1, ALLOW some resources.
        String user1Locator = user1Principal.getResourceLocator();
        Long user1Id = AuthenticationPrincipal.idFromLocator(locatorConfiguration, user1Locator);
        AuthorizationPolicy.Form createPolicy1aForm = new AuthorizationPolicy.Form();
        createPolicy1aForm.setName("policy-1a: Pony's bookshelf");
        createPolicy1aForm.setEffect(AuthorizationPolicy.PolicyEffect.ALLOW);
        createPolicy1aForm.setActions(List.of("bookshelf:getBook"));
        createPolicy1aForm.setResources(List.of(
                new Locator("crn", "cloudapp-cn", "bookshelf", "", user1Id.toString(), "bought-books/*").toString(),
                new Locator("crn", "cloudapp-cn", "bookshelf", "", user1Id.toString(), "in-chart-books/*/liucixin/*").toString()
        ));

        String createPolicy1aResponseContent = testRequestTool.post("/principals/current/policies", createPolicy1aForm, user1Session.getToken());
        this.user1TestPolicy_a = ResponseContentMapper.getPackedReturningBody(createPolicy1aResponseContent, AuthorizationPolicy.Vo.class);

        // create policy-1b of user-1, DENY some resources.
        AuthorizationPolicy.Form createPolicy1bForm = new AuthorizationPolicy.Form();
        createPolicy1bForm.setName("policy-1b: Pony's petshop denials");
        createPolicy1bForm.setEffect(AuthorizationPolicy.PolicyEffect.DENY);
        createPolicy1bForm.setActions(List.of("petshop:buyPet"));
        createPolicy1bForm.setResources(List.of(
                new Locator("crn", "cloudapp-cn", "petshop", "", user1Id.toString(), "dangerous-animals/*/tiger").toString()
        ));

        String createPolicy1bResponseContent = testRequestTool.post("/principals/current/policies", createPolicy1bForm, user1Session.getToken());
        this.user1TestPolicy_b = ResponseContentMapper.getPackedReturningBody(createPolicy1bResponseContent, AuthorizationPolicy.Vo.class);

        // create policy-1b of user-1, ALLOW some resources.
        // Test case: user add a super-resourced policy by accident.
        AuthorizationPolicy.Form createPolicy1cForm = new AuthorizationPolicy.Form();
        createPolicy1cForm.setName("policy-1c: Pony's petshop allowance");
        createPolicy1cForm.setEffect(AuthorizationPolicy.PolicyEffect.ALLOW);
        createPolicy1cForm.setActions(List.of("petshop:buyPet"));
        createPolicy1cForm.setResources(List.of(
                new Locator("crn", "cloudapp-cn", "petshop", "", user1Id.toString(), "dangerous-animals/*").toString()
        ));

        String createPolicy1cResponseContent = testRequestTool.post("/principals/current/policies", createPolicy1cForm, user1Session.getToken());
        this.user1TestPolicy_c = ResponseContentMapper.getPackedReturningBody(createPolicy1cResponseContent, AuthorizationPolicy.Vo.class);
    }

    @Test
    @Transactional
    void testAddGrantInWildcardingPolicies() throws Exception {
        testCreatePolicyInWildcard();

        // test allowed grant for: user1 >- policy1_a && policy1_b, && policy1_c -> user2, expecting 2xx.
        AuthorizationPolicyGrantingForm grantingForm1a = new AuthorizationPolicyGrantingForm();
        grantingForm1a.setGrantee(user2Principal.getResourceLocator()); // grant to user-2
        grantingForm1a.setPolicies(List.of(
                user1TestPolicy_a.getResourceLocator(),
                user1TestPolicy_b.getResourceLocator(),
                user1TestPolicy_c.getResourceLocator()
        )); // grant policy1a policy1b policy1c
        String createGrantResponsePolicy_a = testRequestTool.post("/principals/current/grants", grantingForm1a, user1Session.getToken());
        this.testGrants = ResponseContentMapper.getPackedReturningList(createGrantResponsePolicy_a, AuthorizationPolicyGrant.Vo.class);
    }

    @Test
    @Transactional
    void testChallengingAuthoritiesInWildcardingPolicies() throws Exception {
        testAddGrantInWildcardingPolicies();

        Long user1Id = AuthenticationPrincipal.idFromLocator(locatorConfiguration, user1Principal.getResourceLocator());
        AuthorizationChallengeForm challengingForm = new AuthorizationChallengeForm();

        // User2 challenging granted denied resource, expecting 403.
        // Granted: ALLOW  petshop:buyPet  petshop://users/{user1Id}/dangerous-animals/*/tiger
        challengingForm.setAction("petshop:buyPet");
        challengingForm.setResources(List.of(new Locator("crn", "cloudapp-cn", "petshop", "", user1Id.toString(), "dangerous-animals/asia/tiger").toString()));
        testRequestTool.post("/principals/current/authorization-challengings", challengingForm, user2Session.getToken(), TestRequestTool._403);

        // User2 challenging resource that DENY policy does not cover, expecting 200.
        // Granted: ALLOW  petshop:buyPet  petshop://users/{user1Id}/dangerous-animals/*/tiger
        challengingForm.setResources(List.of(new Locator("crn", "cloudapp-cn", "petshop", "", user1Id.toString(), "dangerous-animals/asia/wolf").toString()));
        testRequestTool.post("/principals/current/authorization-challengings", challengingForm, user2Session.getToken());

        // user2 challenging granted ALLOW subresource, expecting 2xx.
        // Granted: ALLOW  bookshelf:getBook  bookshelf://users/%s/bought-books/*
        //                                    bookshelf://users/%s/in-chart-books/*/liucixin/*
        challengingForm.setAction("bookshelf:getBook");
        challengingForm.setResources(List.of(new Locator("crn", "cloudapp-cn", "bookshelf", "", user1Id.toString(), "bought-books/8880").toString()));
        testRequestTool.post("/principals/current/authorization-challengings", challengingForm, user2Session.getToken());

        // user2 challenging granted subresource - level2, expecting 2xx.
        // Granted: ALLOW  bookshelf:getBook  bookshelf://users/%s/bought-books/*
        //                                    bookshelf://users/%s/in-chart-books/*/liucixin/*
        challengingForm.setResources(List.of(new Locator("crn", "cloudapp-cn", "bookshelf", "", user1Id.toString(), "in-chart-books/scifi/liucixin/8609").toString()));
        testRequestTool.post("/principals/current/authorization-challengings", challengingForm, user2Session.getToken());

        // user2 challenging subresource that is not granted, expecting 403.
        // Granted: ALLOW  bookshelf:getBook  bookshelf://users/%s/bought-books/*
        //                                    bookshelf://users/%s/in-chart-books/*/liucixin/*
        challengingForm.setResources(List.of(new Locator("crn", "cloudapp-cn", "bookshelf", "", user1Id.toString(), "favorite-books/721").toString()));
        testRequestTool.post("/principals/current/authorization-challengings", challengingForm, user2Session.getToken(), TestRequestTool._403);
    }

    @Test
    @Transactional
    void testChallengingAuthoritiesWithSignature() throws Exception {
        long currentTimestampSeconds = Calendar.getInstance().getTimeInMillis() / 1000;
        long timestampSeconds1HourAgo = currentTimestampSeconds - 60 * 60;

        testAddGrant();

        Map<String, String> headers = new HashMap<>();
        headers.put(timestampHeaderName, Long.toString(currentTimestampSeconds));

        Long user1Id = AuthenticationPrincipal.idFromLocator(locatorConfiguration, user1Principal.getResourceLocator());
        AuthorizationChallengeForm challengingForm = new AuthorizationChallengeForm();
        challengingForm.setAction("bookshelf:getBook");
        challengingForm.setResources(List.of(new Locator("crn", "cloudapp-cn", "bookshelf", "", user1Id.toString(), "favorite-books").toString()));

        String user1PrivateKey = new String(base64Decoder.decode(user1KeyPair.getPrivateKey()));
        String user2PrivateKey = new String(base64Decoder.decode(user2KeyPair.getPrivateKey()));

        String user1Signature = HTTPSignatureRsaSha256Signer.signRequest(user1KeyPair.getKeyCode(), headers, user1PrivateKey).toString();
        String user2Signature = HTTPSignatureRsaSha256Signer.signRequest(user2KeyPair.getKeyCode(), headers, user2PrivateKey).toString();

        // user1 challenging its own resource, expecting 2xx.
        testRequestTool.post("/principals/current/authorization-challengings", new LinkedMultiValueMap<>(), headers, challengingForm, user1Signature, TestRequestTool._2XX);

        // user2 challenging user1's resource while user1 or system not allowed, expecting 403.
        testRequestTool.post("/principals/current/authorization-challengings", new LinkedMultiValueMap<>(), headers, challengingForm, user2Signature, TestRequestTool._403);

        // user1 challenging a wrong resource, expecting 403.
        challengingForm.setResources(List.of(new Locator("crn", "cloudapp-cn", "bookshelf", "", String.valueOf(user1Id + 2), "bought-books").toString()));
        testRequestTool.post("/principals/current/authorization-challengings", new LinkedMultiValueMap<>(), headers, challengingForm, user1Signature, TestRequestTool._403);

        // user1 challenging its own resource with an expired timestamp, expecting 401.
        headers.put(timestampHeaderName, Long.toString(timestampSeconds1HourAgo));
        user1Signature = HTTPSignatureRsaSha256Signer.signRequest(user1KeyPair.getKeyCode(), headers, user1PrivateKey).toString();
        testRequestTool.post("/principals/current/authorization-challengings", new LinkedMultiValueMap<>(), headers, challengingForm, user1Signature, TestRequestTool._401);
    }
}