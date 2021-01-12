package cn.glogs.activeauth.iamcore.api.payload;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class RestResultPacker<T> {

    private Meta meta;
    private T data;

    @Data
    public static class Meta {
        private boolean success;

        @Schema(defaultValue = "Succeeded!")
        private String message;

        public static Meta success() {
            Meta meta = new Meta();
            meta.success = true;
            meta.message = "Succeeded!";
            return meta;
        }

        public static Meta success(String message) {
            Meta meta = new Meta();
            meta.success = true;
            meta.message = message;
            return meta;
        }

        public static Meta failure() {
            Meta meta = new Meta();
            meta.success = false;
            meta.message = "Failed!";
            return meta;
        }

        public static Meta failure(String message) {
            Meta meta = new Meta();
            meta.success = false;
            meta.message = message;
            return meta;
        }
    }

    public static <T> RestResultPacker<T> success(T data) {
        RestResultPacker<T> packer = new RestResultPacker<>();
        packer.data = data;
        packer.meta = Meta.success();
        return packer;
    }

    public static <T> RestResultPacker<T> success(T data, String message) {
        RestResultPacker<T> packer = new RestResultPacker<>();
        packer.data = data;
        packer.meta = Meta.success(message);
        return packer;
    }

    public static <T> RestResultPacker<T> failure() {
        RestResultPacker<T> packer = new RestResultPacker<>();
        packer.meta = Meta.failure();
        return packer;
    }


    public static <T> RestResultPacker<T> failure(T data, String message) {
        RestResultPacker<T> packer = new RestResultPacker<>();
        packer.data = data;
        packer.meta = Meta.failure(message);
        return packer;
    }

    public static <T> RestResultPacker<T> failure(String message) {
        RestResultPacker<T> packer = new RestResultPacker<>();
        packer.meta = Meta.failure(message);
        return packer;
    }
}
