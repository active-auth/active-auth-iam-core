package cn.glogs.activeauth.iamcore.api;

import cn.glogs.activeauth.iamcore.config.properties.AuthConfiguration;
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
class UserApiTests {

    private final TestRequestTool testRequestTool;

    private static final String username = "pony";
    private static final String password = "pony123456";

    @Autowired
    public UserApiTests(MockMvc mockMvc, AuthConfiguration authConfiguration) {
        this.testRequestTool = new TestRequestTool(mockMvc, authConfiguration);
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