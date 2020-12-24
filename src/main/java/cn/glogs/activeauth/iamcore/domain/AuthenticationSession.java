package cn.glogs.activeauth.iamcore.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;

import javax.persistence.*;
import java.util.Calendar;
import java.util.Date;

@Data
@Entity
public class AuthenticationSession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private AuthenticationPrincipal authenticationPrincipal;

    private String token;

    private Date createdAt;

    private Date expiredAt;

    public static AuthenticationSession newSession(int expireSeconds, String tokenPrefix, AuthenticationPrincipal authenticationPrincipal) {
        Date now = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(now);
        calendar.add(Calendar.SECOND, expireSeconds);
        AuthenticationSession result = new AuthenticationSession();
        result.token = String.format("%s%s", tokenPrefix, RandomStringUtils.randomAlphanumeric(64));
        result.createdAt = now;
        result.expiredAt = calendar.getTime();
        result.authenticationPrincipal = authenticationPrincipal;
        return result;
    }

    public boolean expired() {
        Date now = new Date();
        return now.after(expiredAt);
    }

    public Vo vo() {
        Vo vo = new Vo();
        vo.id = id;
        vo.token = token;
        vo.createdAt = createdAt;
        vo.expiredAt = expiredAt;
        return vo;
    }

    @Data
    @Schema(name = "AuthenticationSession.Vo")
    public static class Vo {
        @Schema(defaultValue = "66")
        private Long id;
        @Schema(defaultValue = "Bearer hRYUTRJ4TeyrPShYgVwq2b023FZYKFxv41QBlBB7gjfspedPxPuslYjQiYHCcBIx")
        private String token;
        private Date createdAt;
        private Date expiredAt;
    }

    @Data
    @Schema(name = "AuthenticationSession.CreateSessionForm")
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CreateSessionForm {
        @Schema(defaultValue = "pony")
        private String name;
        @Schema(defaultValue = "P0ny_1980")
        private String secret;
    }

    public static class SessionRequestBadHeaderException extends Exception {
        public SessionRequestBadHeaderException(String msg) {
            super(msg);
        }
    }

    public static class SessionNotFoundException extends Exception {
        public SessionNotFoundException(String msg) {
            super(msg);
        }
    }
}
