package cn.glogs.activeauth.iamcore.domain;

import cn.glogs.activeauth.iamcore.config.properties.LocatorConfiguration;
import cn.glogs.activeauth.iamcore.domain.converter.StringListAttributeConverter;
import cn.glogs.activeauth.iamcore.domain.validator.ListablePattern;
import cn.glogs.activeauth.iamcore.exception.business.PatternException;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

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
    public String resourceLocator(LocatorConfiguration locatorConfiguration) {
        return locatorConfiguration.fullLocator(String.valueOf(owner.getId()), "policy", String.valueOf(id));
    }

    public AuthorizationPolicy editWithNotNullFields(AuthorizationPolicy newPolicy) {
        if (null != newPolicy.id) id = newPolicy.id;
        if (StringUtils.isNotBlank(newPolicy.name)) name = newPolicy.name;
        if (null != newPolicy.owner && null != newPolicy.owner.getId()) owner = newPolicy.owner;
        if (null != newPolicy.effect) effect = newPolicy.effect;
        if (null != newPolicy.actions && newPolicy.actions.size() > 0) actions = newPolicy.actions;
        if (null != newPolicy.resources && newPolicy.resources.size() > 0) resources = newPolicy.resources;
        return this;
    }

    private static Long numberFromLocator(LocatorConfiguration locatorConfiguration, String locator, int position) throws PatternException {
        String pattern = locatorConfiguration.fullPattern("policy", "(\\d+)");
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(locator);
        if (m.find()) {
            String principalIdStr = m.group(position);
            return Long.valueOf(principalIdStr);
        } else {
            throw new PatternException("Principal locator regex not matching for " + pattern);
        }
    }

    public static Long idFromLocator(LocatorConfiguration locatorConfiguration, String locator) throws PatternException {
        return numberFromLocator(locatorConfiguration, locator, 2);
    }

    public static Long ownerIdFromLocator(LocatorConfiguration locatorConfiguration, String locator) throws PatternException {
        return numberFromLocator(locatorConfiguration, locator, 1);
    }

    public enum PolicyEffect {
        ALLOW, DENY;
    }

    public Vo vo(LocatorConfiguration locatorConfiguration) {
        Vo vo = new Vo();
        vo.id = id;
        vo.name = name;
        vo.effect = effect;
        vo.actions = actions;
        vo.resources = resources;
        vo.resourceLocator = resourceLocator(locatorConfiguration);
        return vo;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
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
//        @ListablePattern(regexp = "^[a-zA-Z0-9/_-]+://users/\\d+/.+$")
        @Schema(example = "[\"arn:cloudapp:bookshelf::31:bought-book/*\", \"arn:cloudapp:bookshelf::31:shoppping-cart/*\"]", type = "array")
        private List<String> resources;
    }

    @Data
    @Schema(name = "AuthorizationPolicy.Vo")
    public static class Vo {

        private Long id;

        @Schema(example = "MyPolicy22")
        private String name;

        @Schema(example = "arn:cloudapp:iam::77:policy/6632")
        private String resourceLocator;

        private AuthorizationPolicy.PolicyEffect effect;

        @Schema(example = "[\"bookshelf:addBooks\", \"bookshelf:listBooks\"]", type = "array")
        private List<String> actions;

        @Schema(example = "[\"arn:cloudapp:bookshelf::31:bought-book/*\", \"arn:cloudapp:bookshelf::31:shoppping-cart/*\"]", type = "array")
        private List<String> resources;
    }
}
