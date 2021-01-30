package cn.glogs.activeauth.iamcore.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ResourceUtil {

    public static String myResourcePattern(Long challengerId) {
        return String.format("^.+:.+:.+:.*:%s:.+$", challengerId);
    }

    public static Long resourceOwnerId(String resourceLocator) {
        Pattern r = Pattern.compile("^.+:.+:.+:.*:(\\d+):.+$");
        Matcher m = r.matcher(resourceLocator);
        if (m.find()) {
            return Long.valueOf(m.group(1));
        } else {
            throw new RuntimeException("Resource Locator %s is illegal.");
        }
    }
}
