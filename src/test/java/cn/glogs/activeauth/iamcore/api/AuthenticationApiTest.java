package cn.glogs.activeauth.iamcore.api;

import cn.glogs.activeauth.iamcore.domain.AuthenticationPrincipal;
import cn.glogs.activeauth.iamcore.domain.AuthenticationSession;
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

@SpringBootTest
@AutoConfigureMockMvc
class AuthenticationApiTest {

    @Autowired
    private MockMvc mvc;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private static final String username = "pony";
    private static final String password = "pony123456";

    @BeforeEach
    public void registerBeforeEachTest() throws Exception {
        AuthenticationPrincipal.CreatePrincipalForm form = new AuthenticationPrincipal.CreatePrincipalForm(username, password);
        mvc.perform(MockMvcRequestBuilders
                .post("/authentications")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(form))
                .accept(MediaType.APPLICATION_JSON)
        ).andExpect(MockMvcResultMatchers.status().is2xxSuccessful()).andDo(MockMvcResultHandlers.print()).andReturn();
    }

    @Test
    @Transactional
    void testRegister() {
    }

    @Test
    @Transactional
    void testLogin() throws Exception {
        AuthenticationSession.CreateSessionForm form = new AuthenticationSession.CreateSessionForm(username, password);
        mvc.perform(MockMvcRequestBuilders
                .post("/authentications/ticketing")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(form))
                .accept(MediaType.APPLICATION_JSON)
        ).andExpect(MockMvcResultMatchers.status().is2xxSuccessful()).andDo(MockMvcResultHandlers.print()).andReturn();
    }
}