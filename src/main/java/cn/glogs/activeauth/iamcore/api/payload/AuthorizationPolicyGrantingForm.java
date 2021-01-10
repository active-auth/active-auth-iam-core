package cn.glogs.activeauth.iamcore.api.payload;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
public class AuthorizationPolicyGrantingForm {
//    @Schema(example = "iam://users/63/principal")
    private String grantee;
//    @Schema(example = "[\"iam://users/77/authorization-policies/62701\", \"iam://users/77/authorization-policies/65789\"]", type = "array")
    private List<String> policies;
}
