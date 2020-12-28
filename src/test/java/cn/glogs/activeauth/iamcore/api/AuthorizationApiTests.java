package cn.glogs.activeauth.iamcore.api;

import cn.glogs.activeauth.iamcore.api.payload.AuthorizationChallengeForm;
import cn.glogs.activeauth.iamcore.api.payload.AuthorizationPolicyGrantingForm;
import cn.glogs.activeauth.iamcore.api.payload.RestResultPacker;
import cn.glogs.activeauth.iamcore.config.properties.Configuration;
import cn.glogs.activeauth.iamcore.domain.AuthenticationPrincipal;
import cn.glogs.activeauth.iamcore.domain.AuthenticationSession;
import cn.glogs.activeauth.iamcore.domain.AuthorizationPolicy;
import cn.glogs.activeauth.iamcore.domain.AuthorizationPolicyGrant;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.List;

@SpringBootTest
@AutoConfigureMockMvc
class AuthorizationApiTests {

    private final TestRequestTool testRequestTool;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private static final String user1Username = "ponyma";
    private static final String user1Password = "pony199210251311";

    private static final String user2Username = "jackma";
    private static final String user2Password = "1@mJ@ckm@!";

    private AuthenticationPrincipal.Vo user1Principal;
    private AuthenticationPrincipal.Vo user2Principal;

    private AuthenticationSession.Vo user1Session;
    private AuthenticationSession.Vo user2Session;

    private AuthorizationPolicy.Vo user1TestPolicy;
    private AuthorizationPolicy.Vo user2TestPolicy;
    private List<AuthorizationPolicyGrant.Vo> user1TestGrants;
    private List<AuthorizationPolicyGrant.Vo> user2TestGrants;

    public <T> List<T> getPackedReturningList(String content, Class<T> itemType) throws JsonProcessingException {
        JavaType listJavaType = objectMapper.getTypeFactory().constructParametricType(List.class, itemType);
        JavaType packedJavaType = objectMapper.getTypeFactory().constructParametricType(RestResultPacker.class, listJavaType);
        RestResultPacker<List<T>> pack = objectMapper.readValue(content, packedJavaType);
        return pack.getData();
    }

    public <T> T getPackedReturningBody(String content, Class<T> dataType) throws JsonProcessingException {
        JavaType packedJavaType = objectMapper.getTypeFactory().constructParametricType(RestResultPacker.class, dataType);
        RestResultPacker<T> pack = objectMapper.readValue(content, packedJavaType);
        return pack.getData();
    }

    @Autowired
    public AuthorizationApiTests(MockMvc mockMvc, Configuration configuration) {
        this.testRequestTool = new TestRequestTool(mockMvc, configuration);
    }

    @BeforeEach
    void setUp() throws Exception {
        // user-1 Register
        AuthenticationPrincipal.CreatePrincipalForm user1RegisterForm = new AuthenticationPrincipal.CreatePrincipalForm(user1Username, user1Password);
        String user1RegisterResponseContent = testRequestTool.post("/principals", user1RegisterForm, null);
        this.user1Principal = getPackedReturningBody(user1RegisterResponseContent, AuthenticationPrincipal.Vo.class);

        // user-2 Register
        AuthenticationPrincipal.CreatePrincipalForm user2RegisterForm = new AuthenticationPrincipal.CreatePrincipalForm(user2Username, user2Password);
        String user2RegisterResponseContent = testRequestTool.post("/principals", user2RegisterForm, null);
        this.user2Principal = getPackedReturningBody(user2RegisterResponseContent, AuthenticationPrincipal.Vo.class);


        // user-1 Login
        AuthenticationSession.CreateSessionForm user1LoginForm = new AuthenticationSession.CreateSessionForm(user1Username, user1Password);
        String user1LoginResponseContent = testRequestTool.post("/principals/none/authentication-ticketings", user1LoginForm, null);
        this.user1Session = getPackedReturningBody(user1LoginResponseContent, AuthenticationSession.Vo.class);

        // user-2 Login
        AuthenticationSession.CreateSessionForm user2LoginForm = new AuthenticationSession.CreateSessionForm(user2Username, user2Password);
        String user2LoginResponseContent = testRequestTool.post("/principals/none/authentication-ticketings", user2LoginForm, null);
        this.user2Session = getPackedReturningBody(user2LoginResponseContent, AuthenticationSession.Vo.class);
    }

    @Test
    @Transactional
    void testCreatePolicy() throws Exception {
        // create policy-1 of user-1
        String user1Locator = user1Principal.getResourceLocator();
        Long user1Id = AuthenticationPrincipal.idFromLocator(user1Locator);
        AuthorizationPolicy.Form createPolicy1Form = new AuthorizationPolicy.Form();
        createPolicy1Form.setName("policy-1: Pony's bookshelf");
        createPolicy1Form.setPolicyType(AuthorizationPolicy.PolicyType.ALLOW);
        createPolicy1Form.setActions(List.of("bookshelf:getBook"));
        createPolicy1Form.setResources(List.of(
                String.format("bookshelf://users/%s/bought-books", user1Id),
                String.format("bookshelf://users/%s/in-chart-books", user1Id)
        ));

        String createPolicy1ResponseContent = testRequestTool.post("/principals/current/policies", createPolicy1Form, user1Session.getToken());
        this.user1TestPolicy = getPackedReturningBody(createPolicy1ResponseContent, AuthorizationPolicy.Vo.class);

        // create policy-2 of user-2
        String user2Locator = user2Principal.getResourceLocator();
        Long user2Id = AuthenticationPrincipal.idFromLocator(user2Locator);
        AuthorizationPolicy.Form createPolicy2Form = new AuthorizationPolicy.Form();
        createPolicy2Form.setName("policy-2: Jack's petshop");
        createPolicy2Form.setPolicyType(AuthorizationPolicy.PolicyType.ALLOW);
        createPolicy2Form.setActions(List.of("petshop:getCat", "petshop:getDog"));
        createPolicy2Form.setResources(List.of(
                String.format("petshop://users/%s/bought-cats", user2Id),
                String.format("petshop://users/%s/bought-dogs", user2Id)
        ));

        String createPolicy2ResponseContent = testRequestTool.post("/principals/current/policies", createPolicy2Form, user2Session.getToken());
        this.user2TestPolicy = getPackedReturningBody(createPolicy2ResponseContent, AuthorizationPolicy.Vo.class);
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
        this.user1TestGrants = getPackedReturningList(createGrantResponsePolicy, AuthorizationPolicyGrant.Vo.class);

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

        Assert.isTrue(user1TestGrants.size() > 0, "not grants");
        for (AuthorizationPolicyGrant.Vo grant : user1TestGrants) {
            testRequestTool.delete("/principals/current/grants/" + grant.getId(), user1Session.getToken());
        }
    }

    @Test
    @Transactional
    void testChallengingAuthorities() throws Exception {
        testAddGrant();

        Long user1Id = AuthenticationPrincipal.idFromLocator(user1Principal.getResourceLocator());
        AuthorizationChallengeForm challengingForm = new AuthorizationChallengeForm();
        challengingForm.setAction("bookshelf:listBooks");
        challengingForm.setResources(List.of(String.format("bookshelf://users/%s/bought-books", user1Id)));

        // user1 challenging its own resource
        testRequestTool.post("/principals/current/authorization-challengings", challengingForm, user1Session.getToken());

        // user2 challenging user1's resource while user1 or system not allowed
        testRequestTool.post("/principals/current/authorization-challengings", challengingForm, user2Session.getToken(), TestRequestTool._403);

        // user1 challenging a wrong resource
        challengingForm.setResources(List.of(String.format("bookshelf://users/%s/bought-books", user1Id + 2)));
        testRequestTool.post("/principals/current/authorization-challengings", challengingForm, user1Session.getToken(), TestRequestTool._403);
    }
}