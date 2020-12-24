package cn.glogs.activeauth.iamcore.domain;

import cn.glogs.activeauth.iamcore.domain.converter.StringListAttributeConverter;
import cn.glogs.activeauth.iamcore.exception.business.PatternException;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class AuthorizationPolicy implements IamResource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @ManyToOne(cascade = CascadeType.REMOVE)
    private AuthenticationPrincipal owner;

    private PolicyType policyType;

    @Convert(converter = StringListAttributeConverter.class)
    private List<String> actions;

    @Convert(converter = StringListAttributeConverter.class)
    private List<String> resources;

    @Override
    public String resourceLocator() {
        return String.format("iam://users/%s/authorization-policies/%s", owner.getId(), id);
    }

    public static Long idFromLocator(String locator) throws PatternException {
        String pattern = "^iam://users/\\d+/authorization-policies/(\\d+)/?$";
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(locator);
        if (m.find()) {
            String principalIdStr = m.group(1);
            return Long.valueOf(principalIdStr);
        } else {
            throw new PatternException("Principal locator regex not matching for ^iam://users/\\d+/authorization-policies/(\\d+)/?$");
        }
    }

    public enum PolicyType {
        ALLOW, DENY;
    }

    public Vo vo() {
        Vo vo = new Vo();
        vo.name = name;
        vo.policyType = policyType;
        vo.actions = actions;
        vo.resources = resources;
        vo.resourceLocator = resourceLocator();
        return vo;
    }

    @Data
    @Schema(name = "AuthorizationPolicy.Form")
    public static class Form {
        @Schema(example = "MyPolicy22")
        private String name;
        private PolicyType policyType;
        @Schema(example = "[\"bookshelf:addBooks\", \"bookshelf:listBooks\"]", type = "array")
        private List<String> actions;
        @Schema(example = "[\"bookshelf://users/31/bought-books\", \"bookshelf://users/31/shopping-cart\"]", type = "array")
        private List<String> resources;
    }

    @Data
    @Schema(name = "AuthorizationPolicy.Vo")
    public static class Vo {
        @Schema(example = "MyPolicy22")
        private String name;
        @Schema(example = "iam://users/77/authorization-policies/62701")
        private String resourceLocator;
        private PolicyType policyType;
        @Schema(example = "[\"bookshelf:addBooks\", \"bookshelf:listBooks\"]", type = "array")
        private List<String> actions;
        @Schema(example = "[\"bookshelf://users/31/bought-books\", \"bookshelf://users/31/shopping-cart\"]", type = "array")
        private List<String> resources;
    }
}
