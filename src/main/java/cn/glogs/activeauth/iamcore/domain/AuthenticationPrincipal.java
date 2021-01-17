package cn.glogs.activeauth.iamcore.domain;

import cn.glogs.activeauth.iamcore.config.properties.LocatorConfiguration;
import cn.glogs.activeauth.iamcore.domain.password.PasswordHashingStrategy;
import cn.glogs.activeauth.iamcore.exception.business.PatternException;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Data
@Entity
@NoArgsConstructor
public class AuthenticationPrincipal implements IamResource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String name;

    private String description;

    private String encryptedPassword;

    private Date createdAt;

    private Date updatedAt;

    private boolean mfaEnable = false;

    private String mfaSecret;

    private boolean sessionCreatable;

    private boolean sessionUsable;

    private boolean secretKeyCreatable;

    private boolean secretKeyUsable;

    private PrincipalType principalType;

    @ManyToOne
    private AuthenticationPrincipal owner;

    public boolean canCreateSignature() {
        return secretKeyCreatable && principalType != PrincipalType.PRINCIPAL_GROUP;
    }

    public boolean canCreateSession() {
        return sessionCreatable && principalType == PrincipalType.PRINCIPAL;
    }

    @Override
    public String resourceLocator(LocatorConfiguration locatorConfiguration) {
        return locatorConfiguration.fullLocator(id.toString(), "principal");
    }

    public AuthenticationPrincipal(
            String name, String originalPassword,
            PrincipalType principalType,
            PasswordHashingStrategy passwordHashingStrategy
    ) {
        this.name = name;
        this.encryptedPassword = passwordHashingStrategy.getHashing().hashing(originalPassword);
        this.sessionCreatable = principalType.defaultSessionCreatable;
        this.sessionUsable = principalType.defaultSessionUsable;
        this.secretKeyCreatable = principalType.defaultSecretKeyCreatable;
        this.secretKeyUsable = principalType.defaultSecretKeyUsable;
        this.principalType = principalType;
        this.createdAt = new Date();
    }

    public AuthenticationPrincipal(
            String name, String originalPassword,
            Boolean sessionCreatable, Boolean secretKeyCreatable,
            Boolean sessionUsable, Boolean secretKeyUsable,
            PrincipalType principalType,
            PasswordHashingStrategy passwordHashingStrategy
    ) {
        this.name = name;
        this.encryptedPassword = passwordHashingStrategy.getHashing().hashing(originalPassword);
        this.sessionCreatable = sessionCreatable != null ? sessionCreatable : principalType.defaultSessionCreatable;
        this.sessionUsable = sessionUsable != null ? sessionUsable : principalType.defaultSessionUsable;
        this.secretKeyCreatable = secretKeyCreatable != null ? secretKeyCreatable : principalType.defaultSecretKeyCreatable;
        this.secretKeyUsable = secretKeyUsable != null ? secretKeyUsable : principalType.defaultSecretKeyUsable;
        this.principalType = principalType;
        this.createdAt = new Date();
    }

    public boolean typeIs(PrincipalType type) {
        return null != principalType && principalType != type;
    }

    public boolean childChallenging() {
        return principalType != null && principalType.childChallenging;
    }

    @Getter
    @AllArgsConstructor
    public enum PrincipalType {
        PRINCIPAL(false, true, true, true, true),
        PRINCIPAL_GROUP(true, false, false, false, false),
        APP_DOMAIN(false, false, false, true, true);

        private final boolean childChallenging;
        private final boolean defaultSessionCreatable;
        private final boolean defaultSessionUsable;
        private final boolean defaultSecretKeyCreatable;
        private final boolean defaultSecretKeyUsable;
    }

    public static Long idFromLocator(LocatorConfiguration locatorConfiguration, String locator) throws PatternException {
        String pattern = locatorConfiguration.fullPattern("principal");
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(locator);
        if (m.find()) {
            String principalIdStr = m.group(1);
            return Long.valueOf(principalIdStr);
        } else {
            throw new PatternException("Principal locator regex not matching for " + pattern);
        }
    }

    public boolean passwordVerify(String toCheckPassword, PasswordHashingStrategy passwordHashingStrategy) {
        return passwordHashingStrategy.getHashing().check(toCheckPassword, encryptedPassword);
    }

    public Vo vo(LocatorConfiguration locatorConfiguration) {
        Vo vo = new Vo();
        vo.id = id;
        vo.resourceLocator = this.resourceLocator(locatorConfiguration);
        vo.name = name;
        vo.description = description;
        vo.createAt = createdAt;
        vo.sessionCreatable = sessionCreatable;
        vo.secretKeyCreatable = secretKeyCreatable;
        vo.principalType = principalType;
        return vo;
    }

    @Data
    @Schema(name = "AuthenticationPrincipal.Vo")
    public static class Vo {
        private Long id;
        @Schema(example = "arn:cloudapp:iam::116:principal")
        private String resourceLocator;
        @Schema(defaultValue = "pony")
        private String name;
        @Schema(defaultValue = "pony")
        private String description;
        private Date createAt;
        @Schema(defaultValue = "true", type = "boolean")
        private boolean sessionCreatable;
        @Schema(defaultValue = "true", type = "boolean")
        private boolean sessionUsable;
        @Schema(defaultValue = "true", type = "boolean")
        private boolean secretKeyCreatable;
        @Schema(defaultValue = "true", type = "boolean")
        private boolean secretKeyUsable;
        private PrincipalType principalType;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(name = "AuthenticationPrincipal.PrincipalForm")
    public static class PrincipalForm {

        @NotBlank
        @Schema(defaultValue = "pony")
        private String name;

        @NotBlank
        @Schema(defaultValue = "P0ny_1980")
        private String password;

        private Boolean sessionCreatable;

        private Boolean sessionUsable;

        private Boolean secretKeyCreatable;

        private Boolean secretKeyUsable;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(name = "AuthenticationPrincipal.AppDomainForm")
    public static class AppDomainForm {

        @NotBlank
        @Schema(defaultValue = "SampleApp")
        private String name;

        @Schema(defaultValue = "Sample App of pony.")
        private String description;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(name = "AuthenticationPrincipal.PrincipalGroupForm")
    public static class PrincipalGroupForm {

        @NotBlank
        @Schema(defaultValue = "AdminGroup")
        private String name;

        @Schema(defaultValue = "Admin Group of pony.")
        private String description;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(name = "AuthenticationPrincipal.UserRegisterForm")
    public static class UserRegisterForm {

        @NotBlank
        @Schema(defaultValue = "pony")
        private String name;

        @NotBlank
        @Schema(defaultValue = "P0ny_1980")
        private String password;
    }

    public static class PasswordNotMatchException extends Exception {
        public PasswordNotMatchException(String msg) {
            super(msg);
        }
    }

    public static class PrincipalTypeDoesNotAllowedToLoginException extends Exception {
        public PrincipalTypeDoesNotAllowedToLoginException(String msg) {
            super(msg);
        }
    }
}
