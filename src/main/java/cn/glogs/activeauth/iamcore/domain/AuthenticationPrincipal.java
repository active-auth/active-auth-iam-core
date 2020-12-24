package cn.glogs.activeauth.iamcore.domain;

import cn.glogs.activeauth.iamcore.domain.password.PasswordHashingStrategy;
import cn.glogs.activeauth.iamcore.exception.business.PatternException;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;

import javax.persistence.*;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Data
@Entity
public class AuthenticationPrincipal implements IamResource {

    private static final String INFO_SECURED = "INFORMATION IS SECURED AND HIDDEN!";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String name;

    private String encryptedPassword;

    private String secretKey;

    private Date createTime;

    private boolean canUseToken;

    private boolean canUseSignature;

    @Override
    public String resourceLocator() {
        return String.format("%s://users/%s/principal", "iam", id);
    }

    public static Long idFromLocator(String locator) throws PatternException {
        String pattern = "^iam://users/(\\d+)/principal/?$";
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(locator);
        if (m.find()) {
            String principalIdStr = m.group(1);
            return Long.valueOf(principalIdStr);
        } else {
            throw new PatternException("Principal locator regex not matching for ^iam://users/\\d+$/principal/?");
        }
    }

    public static AuthenticationPrincipal createPrincipal(String name, String password, PasswordHashingStrategy passwordHashingStrategy) {
        AuthenticationPrincipal result = new AuthenticationPrincipal();
        result.name = name;
        result.encryptedPassword = passwordHashingStrategy.getHashing().hashing(password);
        result.secretKey = RandomStringUtils.randomAlphanumeric(32);
        result.createTime = new Date();
        return result;
    }

    public boolean passwordVerify(String toCheckPassword, PasswordHashingStrategy passwordHashingStrategy) {
        return passwordHashingStrategy.getHashing().check(toCheckPassword, encryptedPassword);
    }

    public Vo vo() {
        Vo vo = new Vo();
        vo.resourceLocator = this.resourceLocator();
        vo.name = name;
        vo.secretKey = secretKey;
        vo.createTime = createTime;
        vo.canUseToken = canUseToken;
        vo.canUseSignature = canUseSignature;
        return vo;
    }

    @Data
    @Schema(name = "AuthenticationPrincipal.Vo")
    public static class Vo {
        @Schema(defaultValue = "iam://users/116/principal")
        private String resourceLocator;
        @Schema(defaultValue = "pony")
        private String name;
        @Schema(defaultValue = "KSnFIyWeIzibwel49JrckTO7YrZ46xhA")
        private String secretKey;
        private Date createTime;
        private boolean canUseToken;
        private boolean canUseSignature;

        public Vo secureSecretKey() {
            this.secretKey = INFO_SECURED;
            return this;
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(name = "AuthenticationPrincipal.CreatePrincipalForm")
    public static class CreatePrincipalForm {
        @Schema(defaultValue = "pony")
        private String name;
        @Schema(defaultValue = "P0ny_1980")
        private String password;
    }

    public static class PasswordNotMatchException extends Exception {
        public PasswordNotMatchException(String msg) {
            super(msg);
        }
    }
}
