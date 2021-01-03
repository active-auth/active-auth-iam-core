package cn.glogs.activeauth.iamcore.domain;

import lombok.Data;

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
    private String code;

    private String description;

    private Date createdAt;

    private Date updatedAt;

    @Data
    public static class FillingTemplateEntity {
        private String action;
        private List<String> resourceTemplates;
    }
}
