package cn.glogs.activeauth.iamcore.domain;

import cn.glogs.activeauth.iamcore.domain.converter.FillingTemplateEntityListAttributeConverter;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

@Data
@Entity
public class FillingTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, length = 64)
    private String serviceCode;

    private String description;

    @Convert(converter = FillingTemplateEntityListAttributeConverter.class)
    private List<FillingTemplateSentence> sentences;

    private Date createdAt;

    private Date updatedAt;

    public Vo vo() {
        return new Vo(id, serviceCode, description, sentences, createdAt, updatedAt);
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Schema(name = "FillingTemplate.Vo")
    public static class Vo {
        private Long id;
        private String serviceCode;
        private String description;
        private List<FillingTemplateSentence> sentences;
        private Date createdAt;
        private Date updatedAt;
    }

    @Data
    public static class FillingTemplateSentence {
        private String action;
        private List<String> resourceTemplates;
    }
}
