package cn.glogs.activeauth.iamcore.domain;

import cn.glogs.activeauth.iamcore.domain.converter.StringListAttributeConverter;
import cn.glogs.activeauth.iamcore.domain.validator.ListablePattern;
import cn.glogs.activeauth.iamcore.exception.business.PatternException;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
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

    private PolicyEffect effect;

    @Convert(converter = StringListAttributeConverter.class)
    private List<String> actions;

    @Convert(converter = StringListAttributeConverter.class)
    private List<String> resources;

    @Override
    public String resourceLocator() {
        return String.format("iam://users/%s/policies/%s", owner.getId(), id);
    }

    private static Long numberFromLocator(String locator, int position) throws PatternException {
        String pattern = "^iam://users/(\\d+)/policies/(\\d+)/?$";
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(locator);
        if (m.find()) {
            String principalIdStr = m.group(position);
            return Long.valueOf(principalIdStr);
        } else {
            throw new PatternException("Principal locator regex not matching for ^iam://users/\\d+/policies/(\\d+)/?$");
        }
    }

    public static Long idFromLocator(String locator) throws PatternException {
        return numberFromLocator(locator, 2);
    }

    public static Long ownerIdFromLocator(String locator) throws PatternException {
        return numberFromLocator(locator, 1);
    }

    public enum PolicyEffect {
        ALLOW, DENY;
    }

    public Vo vo() {
        Vo vo = new Vo();
        vo.id = id;
        vo.name = name;
        vo.effect = effect;
        vo.actions = actions;
        vo.resources = resources;
        vo.resourceLocator = resourceLocator();
        return vo;
    }

    @Data
    @Schema(name = "AuthorizationPolicy.Form")
    public static class Form {

        @NotBlank
        @Schema(example = "MyPolicy22")
        private String name;

        @NotNull
        private AuthorizationPolicy.PolicyEffect effect;

        @NotEmpty
        @ListablePattern(regexp = "^[a-zA-Z0-9_-]+:[a-zA-Z0-9]+$")
        @Schema(example = "[\"bookshelf:addBooks\", \"bookshelf:listBooks\"]", type = "array")
        private List<String> actions;

        @NotEmpty
        @ListablePattern(regexp = "^[a-zA-Z0-9/_-]+://users/\\d+/.+$")
        @Schema(example = "[\"bookshelf://users/31/bought-books\", \"bookshelf://users/31/shopping-cart\"]", type = "array")
        private List<String> resources;
    }

    @Data
    @Schema(name = "AuthorizationPolicy.Vo")
    public static class Vo {

        private Long id;

        @Schema(example = "MyPolicy22")
        private String name;

        @Schema(example = "iam://users/77/authorization-policies/62701")
        private String resourceLocator;

        private AuthorizationPolicy.PolicyEffect effect;
        @Schema(example = "[\"bookshelf:addBooks\", \"bookshelf:listBooks\"]", type = "array")

        private List<String> actions;

        @Schema(example = "[\"bookshelf://users/31/bought-books\", \"bookshelf://users/31/shopping-cart\"]", type = "array")
        private List<String> resources;
    }
}
