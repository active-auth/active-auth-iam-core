package cn.glogs.activeauth.iamcore.exception.business;

import cn.glogs.activeauth.iamcore.api.payload.SafetyVerifyingContext;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
public class SafetyVerifyingException extends RuntimeException {

    private final SafetyVerifyingContext context;
    private final VerificationRequirement verificationRequirement;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VerificationRequirement {
        private String vId;
    }

    public SafetyVerifyingException(SafetyVerifyingContext context) {
        super("Need safety verifying.");
        this.context = context;
        this.verificationRequirement = new VerificationRequirement(context.getDisposableSession().getTokenId());
    }
}
