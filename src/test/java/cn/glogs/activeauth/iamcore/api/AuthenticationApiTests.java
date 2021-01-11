package cn.glogs.activeauth.iamcore.api;

import cn.glogs.activeauth.iamcore.config.properties.AuthConfiguration;
import cn.glogs.activeauth.iamcore.domain.AuthenticationPrincipal;
import cn.glogs.activeauth.iamcore.domain.AuthenticationSession;
import cn.glogs.activeauth.iamcore.util.ResponseContentMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;

import java.util.HashMap;

@SpringBootTest
@AutoConfigureMockMvc
class AuthenticationApiTests {

    private final TestRequestTool testRequestTool;

    private static final String username = "pony";
    private static final String password = "pony123456";

    private AuthenticationPrincipal.Vo userPrincipal;
    private AuthenticationSession.Vo userSession;

    @Autowired
    public AuthenticationApiTests(MockMvc mockMvc, AuthConfiguration authConfiguration) {
        this.testRequestTool = new TestRequestTool(mockMvc, authConfiguration);
    }

    @BeforeEach
    public void setUp() throws Exception {
        AuthenticationPrincipal.UserRegisterForm registerForm = new AuthenticationPrincipal.UserRegisterForm(username, password);
        String userRegisterResponseContent = testRequestTool.post("/user-center/register", registerForm, null);
        this.userPrincipal = ResponseContentMapper.getPackedReturningBody(userRegisterResponseContent, AuthenticationPrincipal.Vo.class);

        AuthenticationSession.UserLoginForm loginForm = new AuthenticationSession.UserLoginForm(username, password);
        String userLoginResponseContent = testRequestTool.post("/user-center/login", loginForm, null);
        this.userSession = ResponseContentMapper.getPackedReturningBody(userLoginResponseContent, AuthenticationSession.Vo.class);
    }

    @Test
    @Transactional
    void testDeleteCurrentPrincipal() throws Exception {
        testRequestTool.delete("/principals/" + userPrincipal.getId(), new LinkedMultiValueMap<>(), new HashMap<>(), userSession.getToken(), TestRequestTool._403);
    }
}