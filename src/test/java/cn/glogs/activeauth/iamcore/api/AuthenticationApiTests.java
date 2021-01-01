package cn.glogs.activeauth.iamcore.api;

import cn.glogs.activeauth.iamcore.config.properties.Configuration;
import cn.glogs.activeauth.iamcore.domain.AuthenticationPrincipal;
import cn.glogs.activeauth.iamcore.domain.AuthenticationSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
class AuthenticationApiTests {

    private final TestRequestTool testRequestTool;

    private static final String username = "pony";
    private static final String password = "pony123456";

    @Autowired
    public AuthenticationApiTests(MockMvc mockMvc, Configuration configuration) {
        this.testRequestTool = new TestRequestTool(mockMvc, configuration);
    }

    @BeforeEach
    public void setUp() throws Exception {
        AuthenticationPrincipal.UserRegisterForm form = new AuthenticationPrincipal.UserRegisterForm(username, password);
        testRequestTool.post("/user-center/register", form, null);
    }

    @Test
    @Transactional
    void testRegister() {
    }

    @Test
    @Transactional
    void testLogin() throws Exception {
        AuthenticationSession.UserLoginForm form = new AuthenticationSession.UserLoginForm(username, password);
        testRequestTool.post("/user-center/login", form, null);
    }
}