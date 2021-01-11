package cn.glogs.activeauth.iamcore.api.payload;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
public class AuthorizationPolicyGrantingForm {
    @Schema(example = "arn:cloudapp:iam::63:principal")
    private String grantee;
    @Schema(example = "[\"arn:cloudapp:iam::77:policy/62701\", \"arn:cloudapp:iam::77:policy/65789\"]", type = "array")
    private List<String> policies;
}
