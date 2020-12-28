package cn.glogs.activeauth.iamcore.api;

import cn.glogs.activeauth.iamcore.config.properties.Configuration;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.HashMap;
import java.util.Map;


public class TestRequestTool {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static final ResultMatcher _2XX = MockMvcResultMatchers.status().is2xxSuccessful();
    public static final ResultMatcher _3XX = MockMvcResultMatchers.status().is3xxRedirection();
    public static final ResultMatcher _4XX = MockMvcResultMatchers.status().is4xxClientError();
    public static final ResultMatcher _5XX = MockMvcResultMatchers.status().is5xxServerError();

    public static final ResultMatcher _401 = MockMvcResultMatchers.status().isUnauthorized();
    public static final ResultMatcher _403 = MockMvcResultMatchers.status().isForbidden();

    private final MockMvc mvc;
    private final Configuration configuration;

    public TestRequestTool(MockMvc mvc, Configuration configuration) {
        this.mvc = mvc;
        this.configuration = configuration;
    }

    String get(String url, String token) throws Exception {
        return get(url, new LinkedMultiValueMap<>(), new HashMap<>(), token, _2XX);
    }

    String get(String url, MultiValueMap<String, String> params, Map<String, String> headers, String token, ResultMatcher expect) throws Exception {
        MockHttpServletRequestBuilder builder =
                MockMvcRequestBuilders
                        .get(url).params(params)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON);
        if (StringUtils.isNotBlank(token)) {
            builder = builder.header(configuration.getAuthorizationHeaderName(), token);
        }
        for (Map.Entry<String, String> mapEntry : headers.entrySet()) {
            builder = builder.header(mapEntry.getKey(), mapEntry.getValue());
        }
        return mvc.perform(builder).andExpect(expect).andDo(MockMvcResultHandlers.print()).andReturn().getResponse().getContentAsString();
    }

    String post(String url, Object formData, String token) throws Exception {
        return post(url, new LinkedMultiValueMap<>(), new HashMap(), formData, token, _2XX);
    }

    String post(String url, Object formData, String token, ResultMatcher expect) throws Exception {
        return post(url, new LinkedMultiValueMap<>(), new HashMap<>(), formData, token, expect);
    }

    String post(String url, MultiValueMap<String, String> params, Map<String, String> headers, Object formData, String token, ResultMatcher expect) throws Exception {
        MockHttpServletRequestBuilder builder =
                MockMvcRequestBuilders
                        .post(url).params(params)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(formData))
                        .accept(MediaType.APPLICATION_JSON);
        if (StringUtils.isNotBlank(token)) {
            builder = builder.header(configuration.getAuthorizationHeaderName(), token);
        }
        for (Map.Entry<String, String> mapEntry : headers.entrySet()) {
            builder = builder.header(mapEntry.getKey(), mapEntry.getValue());
        }
        return mvc.perform(builder).andExpect(expect).andDo(MockMvcResultHandlers.print()).andReturn().getResponse().getContentAsString();
    }

    String delete(String url, String token) throws Exception {
        return delete(url, new LinkedMultiValueMap<>(), new HashMap<>(), token, _2XX);
    }

    String delete(String url, MultiValueMap<String, String> params, Map<String, String> headers, String token, ResultMatcher expect) throws Exception {
        MockHttpServletRequestBuilder builder =
                MockMvcRequestBuilders
                        .delete(url).params(params)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON);
        if (StringUtils.isNotBlank(token)) {
            builder = builder.header(configuration.getAuthorizationHeaderName(), token);
        }
        for (Map.Entry<String, String> mapEntry : headers.entrySet()) {
            builder = builder.header(mapEntry.getKey(), mapEntry.getValue());
        }
        return mvc.perform(builder).andExpect(expect).andDo(MockMvcResultHandlers.print()).andReturn().getResponse().getContentAsString();
    }
}
