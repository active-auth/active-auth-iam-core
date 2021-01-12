package cn.glogs.activeauth.iamcore.domain.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

@Converter(autoApply = true)
public class StringSetAttributeConverter implements AttributeConverter<Set<String>, String> {

    private final static ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(Set<String> strings) {
        try {
            return objectMapper.writeValueAsString(strings);
        } catch (JsonProcessingException ex) {
            ex.printStackTrace();
            return "[]";
            // or throw an error
        }
    }

    @Override
    public Set<String> convertToEntityAttribute(String dbData) {
        try {
            JavaType javaType = objectMapper.getTypeFactory().constructParametricType(HashSet.class, String.class);
            return objectMapper.readValue(dbData, javaType);
        } catch (IOException ex) {
            // logger.error("Unexpected IOEx decoding json from database: " + dbData);
            ex.printStackTrace();
            return new HashSet<>();
        }
    }
}
