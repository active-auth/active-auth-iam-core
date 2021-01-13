package cn.glogs.activeauth.iamcore.domain.converter;

import cn.glogs.activeauth.iamcore.domain.environment.ClientEnvironment;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.persistence.AttributeConverter;
import java.io.IOException;

public class ClientEnvironmentAttributeConverter implements AttributeConverter<ClientEnvironment, String> {

    private final static ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(ClientEnvironment strings) {
        try {
            return objectMapper.writeValueAsString(strings);
        } catch (JsonProcessingException ex) {
            ex.printStackTrace();
            return "[]";
            // or throw an error
        }
    }

    @Override
    public ClientEnvironment convertToEntityAttribute(String dbData) {
        try {
            return objectMapper.readValue(dbData, ClientEnvironment.class);
        } catch (IOException ex) {
            // logger.error("Unexpected IOEx decoding json from database: " + dbData);
            ex.printStackTrace();
            return new ClientEnvironment("", "");
        }
    }
}
