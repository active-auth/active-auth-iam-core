package cn.glogs.activeauth.iamcore.api.payload;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.List;

@Data
public class AuthorizationChallengeFormOfPrincipal {
    @NotBlank
    @Schema(example = "iam://users/7/principals")
    private String principal;
    @NotBlank
    @Schema(example = "bookshelf:listBooks")
    private String action;
    @NotEmpty
    @Schema(example = "[\"bookshelf://users/31/bought-books\", \"bookshelf://users/31/shopping-cart\"]", type = "array")
    private List<String> resources;

    public String[] resourcesArray() {
        String[] strArr = new String[resources.size()];
        for (int i = 0; i < resources.size(); i++) {
            strArr[i] = resources.get(i);
        }
        return strArr;
    }
}
