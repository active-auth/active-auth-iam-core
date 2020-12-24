package cn.glogs.activeauth.iamcore.api;

import cn.glogs.activeauth.iamcore.api.payload.RestResultPacker;
import cn.glogs.activeauth.iamcore.config.properties.Configuration;
import cn.glogs.activeauth.iamcore.domain.AuthenticationPrincipal;
import cn.glogs.activeauth.iamcore.domain.AuthenticationSession;
import cn.glogs.activeauth.iamcore.domain.AuthorizationPolicy;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@SpringBootTest
@AutoConfigureMockMvc
class AuthorizationApiTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private Configuration.LordAuthConfiguration lordAuthConfiguration;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private static final String granterUsername = "ponyma";
    private static final String granterPassword = "pony199210251311";

    private static final String granteeUsername = "jackma";
    private static final String granteePassword = "1@mJ@ckm@!";

    private AuthenticationPrincipal.Vo granterPrincipal;
    private AuthenticationPrincipal.Vo granteePrincipal;

    private AuthenticationSession.Vo granterSession;
    private AuthenticationSession.Vo granteeSession;

    private AuthorizationPolicy.Vo testPolicy;

    public <T> T getPackedReturningBody(String content, Class<T> dataType) throws JsonProcessingException {
        JavaType javaType = objectMapper.getTypeFactory().constructParametricType(RestResultPacker.class, dataType);
        RestResultPacker<T> pack = objectMapper.readValue(content, javaType);
        return pack.getData();
    }

    @BeforeEach
    void setUp() throws Exception {
        // Granter Register
        AuthenticationPrincipal.CreatePrincipalForm granterRegisterForm = new AuthenticationPrincipal.CreatePrincipalForm(granterUsername, granterPassword);
        String granterRegisterResponseContent = mvc.perform(MockMvcRequestBuilders
                .post("/authentication-principals")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(granterRegisterForm))
                .accept(MediaType.APPLICATION_JSON)
        ).andExpect(MockMvcResultMatchers.status().is2xxSuccessful()).andDo(MockMvcResultHandlers.print()).andReturn().getResponse().getContentAsString();
        this.granterPrincipal = getPackedReturningBody(granterRegisterResponseContent, AuthenticationPrincipal.Vo.class);

        // Grantee Register
        AuthenticationPrincipal.CreatePrincipalForm granteeRegisterForm = new AuthenticationPrincipal.CreatePrincipalForm(granteeUsername, granteePassword);
        String granteeRegisterResponseContent = mvc.perform(MockMvcRequestBuilders
                .post("/authentication-principals")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(granteeRegisterForm))
                .accept(MediaType.APPLICATION_JSON)
        ).andExpect(MockMvcResultMatchers.status().is2xxSuccessful()).andDo(MockMvcResultHandlers.print()).andReturn().getResponse().getContentAsString();
        this.granteePrincipal = getPackedReturningBody(granteeRegisterResponseContent, AuthenticationPrincipal.Vo.class);


        // Granter Login
        AuthenticationSession.CreateSessionForm granterLoginForm = new AuthenticationSession.CreateSessionForm(granterUsername, granterPassword);
        String granterLoginResponseContent = mvc.perform(MockMvcRequestBuilders
                .post("/authentications/ticketing")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(granterLoginForm))
                .accept(MediaType.APPLICATION_JSON)
        ).andExpect(MockMvcResultMatchers.status().is2xxSuccessful()).andDo(MockMvcResultHandlers.print()).andReturn().getResponse().getContentAsString();
        this.granterSession = getPackedReturningBody(granterLoginResponseContent, AuthenticationSession.Vo.class);

        // Grantee Login
        AuthenticationSession.CreateSessionForm granteeLoginForm = new AuthenticationSession.CreateSessionForm(granteeUsername, granteePassword);
        String granteeLoginResponseContent = mvc.perform(MockMvcRequestBuilders
                .post("/authentications/ticketing")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(granteeLoginForm))
                .accept(MediaType.APPLICATION_JSON)
        ).andExpect(MockMvcResultMatchers.status().is2xxSuccessful()).andDo(MockMvcResultHandlers.print()).andReturn().getResponse().getContentAsString();
        this.granteeSession = getPackedReturningBody(granteeLoginResponseContent, AuthenticationSession.Vo.class);
    }

    @Test
    @Transactional
    void testCreatePolicy() throws Exception {
        String granterLocator = granterPrincipal.getResourceLocator();
        Long granterId = AuthenticationPrincipal.idFromLocator(granterLocator);
        AuthorizationPolicy.Form createPolicyForm = new AuthorizationPolicy.Form();
        createPolicyForm.setName("testCreatePolicy");
        createPolicyForm.setPolicyType(AuthorizationPolicy.PolicyType.ALLOW);
        createPolicyForm.setActions(List.of("bookstore:listBooks", "bookstore:getBook"));
        createPolicyForm.setResources(List.of(
                String.format("bookstore://users/%s/bought-books", granterId),
                String.format("bookstore://users/%s/in-chart-books", granterId)
        ));

        String createPolicyResponseContent = mvc.perform(MockMvcRequestBuilders
                .post("/authorization-policies")
                .header(lordAuthConfiguration.getAuthorizationHeaderName(), granterSession.getToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createPolicyForm))
                .accept(MediaType.APPLICATION_JSON)
        ).andExpect(MockMvcResultMatchers.status().is2xxSuccessful()).andDo(MockMvcResultHandlers.print()).andReturn().getResponse().getContentAsString();
        this.testPolicy = getPackedReturningBody(createPolicyResponseContent, AuthorizationPolicy.Vo.class);
    }

    @Test
    @Transactional
    void testGrantPolicy() {
    }
}