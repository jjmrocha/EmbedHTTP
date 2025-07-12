package net.uiqui.embedhttp.routing;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract sealed class PathSegment {
    protected static final String MATCH_ALL = "*";

    protected final PathSegment parent;
    private final Map<String, PathSegment> children = new HashMap<>();
    protected Route route;

    protected PathSegment(PathSegment parent, Route route) {
        this.parent = parent;
        this.route = route;
    }

    public int getChildCount() {
        return children.size();
    }

    public PathSegment findChild(String segment, boolean ignoreMachAll) {
        var child = children.get(segment);
        if (child != null) {
            return child;
        }

        if (ignoreMachAll) {
            return null;
        }

        return findParameterChild();
    }

    public PathSegment findParameterChild() {
        return children.get(MATCH_ALL);
    }

    public boolean hasRoute() {
        return route != null;
    }

    public Route getRoute() {
        return route;
    }

    public PathSegment registerParameterChild(String pathParameter, Route route) {
        return registerChild(MATCH_ALL, new Parameter(this, pathParameter, route));
    }

    public PathSegment registerStaticChild(String pathSegment, Route route) {
        return registerChild(pathSegment, new Static(this, pathSegment, route));
    }

    private PathSegment registerChild(String segmentKey, PathSegment child) {
        if (children.containsKey(segmentKey)) {
            throw new InvalidRouteException("Path segment '" + segmentKey + "' already exists at '" + this + "'.");
        }

        children.put(segmentKey, child);
        return child;
    }

    public List<Route> getAllRoutes() {
        var routes = new ArrayList<Route>();

        if (hasRoute()) {
            routes.add(getRoute());
        }

        for (PathSegment child : children.values()) {
            routes.addAll(child.getAllRoutes());
        }

        return routes;
    }

    public List<String> getTreePaths() {
        var paths = new ArrayList<String>();
        var buffer = new StringBuilder();

        buffer.append(this);

        if (hasRoute()) {
            buffer.append("+");
        }

        paths.add(buffer.toString());

        for (PathSegment child : children.values()) {
            paths.addAll(child.getTreePaths());
        }

        return paths;
    }

    static final class Root extends PathSegment {
        public Root() {
            super(null, null);
        }

        @Override
        public String toString() {
            return "/";
        }

        public void setRoute(Route route) {
            if (hasRoute()) {
                throw new InvalidRouteException("Path segment '/' already has a handler.");
            }

            this.route = route;
        }
    }

    static final class Parameter extends PathSegment {
        private final String parameterName;

        private Parameter(PathSegment parent, String parameterName, Route handler) {
            super(parent, handler);
            this.parameterName = parameterName;
        }

        public String getParameterName() {
            return parameterName;
        }

        @Override
        public String toString() {
            var parentPath = parent.toString();

            if (!parentPath.endsWith("/")) {
                parentPath += "/";
            }

            return parentPath + ":" + parameterName;
        }
    }

    static final class Static extends PathSegment {
        private final String pathSegment;

        private Static(PathSegment parent, String pathSegment, Route handler) {
            super(parent, handler);
            this.pathSegment = pathSegment;
        }

        @Override
        public String toString() {
            var parentPath = parent.toString();

            if (!parentPath.endsWith("/")) {
                parentPath += "/";
            }

            return parentPath + pathSegment;
        }
    }
}