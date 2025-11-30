package net.uiqui.embedhttp.routing;

import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class RouteTree {
    private static final Pattern PARAM_PATTERN = Pattern.compile("^:([a-zA-Z][a-zA-Z0-9]*)$");

    private final PathSegment rootSegment = new PathSegment.Root();

    public void addRoute(Route route) {
        var pathSegments = splitPath(route.getPathPattern());

        if (pathSegments.length == 0) {
            throw new InvalidRouteException("Invalid path pattern: " + route.getPathPattern());
        }

        if (isRootPath(pathSegments)) {
            // Special case for root path
            ((PathSegment.Root) rootSegment).setRoute(route);
            return;
        }

        var currentSegment = rootSegment;
        var lastSegmentIndex = pathSegments.length - 1;

        for (var i = 0; i <= lastSegmentIndex; i++) {
            var segment = pathSegments[i];
            var isLastSegment = i == lastSegmentIndex;
            currentSegment = handleSegment(segment, currentSegment, isLastSegment, route);
        }
    }

    private boolean isRootPath(String[] pathSegments) {
        return pathSegments.length == 1 && pathSegments[0].equals("/");
    }

    private PathSegment handleSegment(String segment, PathSegment currentSegment, boolean isLastSegment, Route route) {
        var segmentRoute = isLastSegment ? route : null;

        var parameterName = extractParameterName(segment);
        if (parameterName != null) {
            return handleParameterSegment(parameterName, currentSegment, segmentRoute);
        }

        return handleStaticSegment(segment, currentSegment, segmentRoute);
    }

    private PathSegment handleParameterSegment(String parameterName, PathSegment currentSegment, Route route) {
        var parameterChild = currentSegment.findParameterChild();

        if (parameterChild != null) {
            return parameterChild;
        }

        return currentSegment.registerParameterChild(parameterName, route);
    }

    private PathSegment handleStaticSegment(String pathSegment, PathSegment currentSegment, Route route) {
        var staticChild = currentSegment.findChild(pathSegment, true);

        if (staticChild != null) {
            return staticChild;
        }

        return currentSegment.registerStaticChild(pathSegment, route);
    }

    public RouteMatch findRoute(String pathPattern) {
        var pathSegments = splitPath(pathPattern);
        if (pathSegments.length == 0) {
            return null;
        }

        if (isRootPath(pathSegments)) {
            // Special case for root path
            if (rootSegment.hasRoute()) {
                return new RouteMatch(rootSegment.getRoute(), new HashMap<>());
            } else {
                return null; // No route found for root path
            }
        }

        var pathParameters = new HashMap<String, String>();
        var currentSegment = rootSegment;

        for (var segment : pathSegments) {
            currentSegment = currentSegment.findChild(segment, false);
            if (currentSegment == null) {
                return null; // No matching segment found
            }

            if (currentSegment instanceof PathSegment.Parameter parameter) {
                pathParameters.put(parameter.getParameterName(), segment);
            }
        }

        if (!currentSegment.hasRoute()) {
            return null; // No route found for the given path
        }

        return new RouteMatch(currentSegment.getRoute(), pathParameters);
    }

    public List<Route> getAllRoutes() {
        return rootSegment.getAllRoutes();
    }

    public List<String> getTreePaths() {
        return rootSegment.getTreePaths()
                .stream()
                .sorted()
                .toList();
    }

    protected String extractParameterName(String segment) {
        var matcher = PARAM_PATTERN.matcher(segment);

        if (matcher.matches()) {
            return matcher.group(1);
        }

        return null;
    }

    protected String[] splitPath(String pathPattern) {
        if (pathPattern == null || pathPattern.isEmpty()) {
            return new String[0];
        }

        if (pathPattern.equals("/")) {
            return new String[]{"/"};
        }

        return Stream.of(pathPattern.split("/"))
                .map(String::trim)
                .filter(segment -> !segment.isEmpty())
                .toArray(String[]::new);
    }

    public record RouteMatch(Route route, HashMap<String, String> pathParameters) {
    }
}
