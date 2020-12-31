package cn.glogs.activeauth.iamcore.api.payload;

import cn.glogs.activeauth.iamcore.domain.validator.ListablePattern;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import java.util.List;

@Data
public class AuthorizationChallengeForm {

    @NotBlank
    @Pattern(regexp = "^[a-zA-Z0-9/_-]+:[a-zA-Z0-9]+$")
    @Schema(example = "bookshelf:listBooks")
    private String action;

    @NotEmpty
    @ListablePattern(regexp = "^[a-zA-Z0-9/_-]+://users/\\d+/.+$")
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
