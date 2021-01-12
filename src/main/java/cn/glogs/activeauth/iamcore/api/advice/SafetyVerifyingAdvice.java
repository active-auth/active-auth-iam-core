package cn.glogs.activeauth.iamcore.api.advice;

import cn.glogs.activeauth.iamcore.api.payload.RestResultPacker;
import cn.glogs.activeauth.iamcore.exception.business.SafetyVerifyingException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseBody
@ControllerAdvice
public class SafetyVerifyingAdvice {

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(SafetyVerifyingException.class)
    public RestResultPacker<Object> handleSafetyVerifyingException(SafetyVerifyingException e) {
        return RestResultPacker.failure(e.getVerificationRequirement(), "Error-401: " + e.getMessage());
    }
}
