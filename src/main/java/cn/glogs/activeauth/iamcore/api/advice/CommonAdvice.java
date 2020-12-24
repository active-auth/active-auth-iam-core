package cn.glogs.activeauth.iamcore.api.advice;

import cn.glogs.activeauth.iamcore.api.payload.RestResultPacker;
import cn.glogs.activeauth.iamcore.exception.HTTP400Exception;
import cn.glogs.activeauth.iamcore.exception.HTTP401Exception;
import cn.glogs.activeauth.iamcore.exception.HTTP403Exception;
import cn.glogs.activeauth.iamcore.exception.HTTP404Exception;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseBody
@ControllerAdvice
public class CommonAdvice {

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(HTTP400Exception.class)
    public RestResultPacker<Object> handleHTTP400Exception(HTTP400Exception e) {
        return RestResultPacker.failure("错误400: " + e.getMessage());
    }

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(HTTP401Exception.class)
    public RestResultPacker<Object> handleHTTP401Exception(HTTP401Exception e) {
        return RestResultPacker.failure("错误401: " + e.getMessage());
    }

    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler(HTTP403Exception.class)
    public RestResultPacker<Object> handleHTTP403Exception(HTTP403Exception e) {
        return RestResultPacker.failure("错误403: " + e.getMessage());
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(HTTP404Exception.class)
    public RestResultPacker<Object> handleHTTP404Exception(HTTP404Exception e) {
        return RestResultPacker.failure("错误404: " + e.getMessage());
    }
}
