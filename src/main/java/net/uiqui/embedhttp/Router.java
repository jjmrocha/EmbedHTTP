package net.uiqui.embedhttp;

import net.uiqui.embedhttp.api.HttpRequestHandler;
import net.uiqui.embedhttp.routing.RouterImpl;

/**
 * Interface for a router that maps HTTP request paths to handlers.
 * <p>
 * This interface provides methods to register handlers for different HTTP methods (GET, POST, PUT, DELETE, HEAD, OPTIONS,
 * PATCH) with specified path patterns.
 * </p>
 * <p>
 * * Example usage:
 * <pre>
 *     Router router = Router.newRouter()
 *     .get("/api/resource/:id", request -> {
 *       return HttpResponse.ok()
 *          .setBody(ContentType.TEXT_PLAIN, "Resource ID: " + request.getPathParameter("id"));
 *     })
 *     .post("/api/resource", request -> {
 *     // Handle POST request
 *     });
 *   </pre>
 * </p>
 */
public interface Router {
    /**
     * Registers a handler for GET method and path pattern.
     *
     * @param pathPattern The path pattern to match against incoming requests.
     * @param handler     The handler to be invoked when a request matches the specified method and path pattern.
     * @return The current Router instance for method chaining.
     */
    Router get(String pathPattern, HttpRequestHandler handler);

    /**
     * Registers a handler for POST method and path pattern.
     *
     * @param pathPattern The path pattern to match against incoming requests.
     * @param handler     The handler to be invoked when a request matches the specified method and path pattern.
     * @return The current Router instance for method chaining.
     */
    Router post(String pathPattern, HttpRequestHandler handler);

    /**
     * Registers a handler for PUT method and path pattern.
     *
     * @param pathPattern The path pattern to match against incoming requests.
     * @param handler     The handler to be invoked when a request matches the specified method and path pattern.
     * @return The current Router instance for method chaining.
     */
    Router put(String pathPattern, HttpRequestHandler handler);

    /**
     * Registers a handler for DELETE method and path pattern.
     *
     * @param pathPattern The path pattern to match against incoming requests.
     * @param handler     The handler to be invoked when a request matches the specified method and path pattern.
     * @return The current Router instance for method chaining.
     */
    Router delete(String pathPattern, HttpRequestHandler handler);

    /**
     * Registers a handler for HEAD method and path pattern.
     *
     * @param pathPattern The path pattern to match against incoming requests.
     * @param handler     The handler to be invoked when a request matches the specified method and path pattern.
     * @return The current Router instance for method chaining.
     */
    Router head(String pathPattern, HttpRequestHandler handler);

    /**
     * Registers a handler for OPTIONS method and path pattern.
     *
     * @param pathPattern The path pattern to match against incoming requests.
     * @param handler     The handler to be invoked when a request matches the specified method and path pattern.
     * @return The current Router instance for method chaining.
     */
    Router options(String pathPattern, HttpRequestHandler handler);

    /**
     * Registers a handler for PATCH method and path pattern.
     *
     * @param pathPattern The path pattern to match against incoming requests.
     * @param handler     The handler to be invoked when a request matches the specified method and path pattern.
     * @return The current Router instance for method chaining.
     */
    Router patch(String pathPattern, HttpRequestHandler handler);

    /**
     * Creates a new router instance.
     *
     * @return A new Router instance for method chaining.
     */
    static Router newRouter() {
        return new RouterImpl();
    }
}
