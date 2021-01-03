package cn.glogs.activeauth.iamcore.domain.converter;

import cn.glogs.activeauth.iamcore.domain.FillingTemplate;
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
public class FillingTemplateEntityListAttributeConverter implements AttributeConverter<List<FillingTemplate.FillingTemplateEntity>, String> {

    private final static ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(List<FillingTemplate.FillingTemplateEntity> strings) {
        try {
            return objectMapper.writeValueAsString(strings);
        } catch (JsonProcessingException ex) {
            ex.printStackTrace();
            return "[]";
            // or throw an error
        }
    }

    @Override
    public List<FillingTemplate.FillingTemplateEntity> convertToEntityAttribute(String dbData) {
        try {
            JavaType javaType = objectMapper.getTypeFactory().constructParametricType(ArrayList.class, FillingTemplate.FillingTemplateEntity.class);
            return objectMapper.readValue(dbData, javaType);
        } catch (IOException ex) {
            // logger.error("Unexpected IOEx decoding json from database: " + dbData);
            ex.printStackTrace();
            return new ArrayList<>();
        }
    }
}
