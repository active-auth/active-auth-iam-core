package cn.glogs.activeauth.iamcore.api.payload;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AuthCheckingStatement {

    private List<Statement> statements = new ArrayList<>();

    public static AuthCheckingStatement checks(String firstAction, String... firstResourceFormats) {
        AuthCheckingStatement payload = new AuthCheckingStatement();
        payload.statements.add(new Statement(firstAction, List.of(firstResourceFormats)));
        return payload;
    }

    public AuthCheckingStatement and(String action, String... resourceFormats) {
        statements.add(new Statement(action, List.of(resourceFormats)));
        return this;
    }

    @Data
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Statement {
        private String action;
        private List<String> resourceFormats;

        public String[] resourceLocators(Object... formatArgs) {
            String[] strArr = new String[resourceFormats.size()];
            for (int i = 0; i < resourceFormats.size(); i++) {
                String resourceFormat = resourceFormats.get(i);
                if (!(resourceFormat.split("%s").length == formatArgs.length + 1)) {
                    throw new ResourceArgsException("Not enough or too many resource formats args. Resource format: " + resourceFormat + ", args count: " + formatArgs.length);
                }
                strArr[i] = String.format(resourceFormat, formatArgs);
            }
            return strArr;
        }

        public static class ResourceArgsException extends RuntimeException {
            public ResourceArgsException(String msg) {
                super(msg);
            }
        }
    }
}
