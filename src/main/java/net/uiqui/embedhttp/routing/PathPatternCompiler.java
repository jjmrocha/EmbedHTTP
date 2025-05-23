package net.uiqui.embedhttp.routing;

import java.util.regex.Pattern;

public class PathPatternCompiler {
    private static final Pattern paramPattern = Pattern.compile(":([a-zA-Z][a-zA-Z0-9]*)");

    private PathPatternCompiler() {
        // Prevent instantiation
    }

    protected static String pathToRegex(String path) {
        var matcher = paramPattern.matcher(path);
        var regexBuffer = new StringBuilder();

        while (matcher.find()) {
            var paramName = matcher.group(1);
            var paramReplacement = "(?<" + paramName + ">[^/]+)";
            matcher.appendReplacement(regexBuffer, paramReplacement);
        }

        matcher.appendTail(regexBuffer);

        return regexBuffer.toString();
    }

    public static Pattern compile(String path) {
        var regex = pathToRegex(path);
        return Pattern.compile(regex);
    }
}
