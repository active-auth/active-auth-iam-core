package cn.glogs.activeauth.iamcore.domain.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Converter(autoApply = true)
public class StringListAttributeConverter implements AttributeConverter<List<String>, String> {

    private final static ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(List<String> strings) {
        try {
            return objectMapper.writeValueAsString(strings);
        } catch (JsonProcessingException ex) {
            ex.printStackTrace();
            return "[]";
            // or throw an error
        }
    }

    @Override
    public List<String> convertToEntityAttribute(String dbData) {
        try {
            JavaType javaType = objectMapper.getTypeFactory().constructParametricType(ArrayList.class, String.class);
            return objectMapper.readValue(dbData, javaType);
        } catch (IOException ex) {
            // logger.error("Unexpected IOEx decoding json from database: " + dbData);
            ex.printStackTrace();
            return new ArrayList<>();
        }
    }

    @Data
    public static class StringListWrapper {
        private List<String> values;
    }

}
