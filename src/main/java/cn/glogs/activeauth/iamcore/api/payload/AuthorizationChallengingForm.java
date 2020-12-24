package cn.glogs.activeauth.iamcore.api.payload;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
public class AuthorizationChallengingForm {
    @Schema(example = "bookshelf:listBooks")
    private String action;
    @Schema(example = "[\"bookshelf://users/31/bought-books\", \"bookshelf://users/31/shopping-cart\"]", type = "array")
    private List<String> resources;
}
