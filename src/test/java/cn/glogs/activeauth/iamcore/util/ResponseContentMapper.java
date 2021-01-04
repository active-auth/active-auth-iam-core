package cn.glogs.activeauth.iamcore.util;

import cn.glogs.activeauth.iamcore.api.payload.RestResultPacker;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

public class ResponseContentMapper {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static <T> List<T> getPackedReturningList(String content, Class<T> itemType) throws JsonProcessingException {
        JavaType listJavaType = objectMapper.getTypeFactory().constructParametricType(List.class, itemType);
        JavaType packedJavaType = objectMapper.getTypeFactory().constructParametricType(RestResultPacker.class, listJavaType);
        RestResultPacker<List<T>> pack = objectMapper.readValue(content, packedJavaType);
        return pack.getData();
    }

    public static  <T> T getPackedReturningBody(String content, Class<T> dataType) throws JsonProcessingException {
        JavaType packedJavaType = objectMapper.getTypeFactory().constructParametricType(RestResultPacker.class, dataType);
        RestResultPacker<T> pack = objectMapper.readValue(content, packedJavaType);
        return pack.getData();
    }
}
