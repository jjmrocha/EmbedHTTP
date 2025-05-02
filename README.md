# EmbedHTTP
EmbedHTTP is a lightweight, dependency-free HTTP/1.1 server designed to be easily embedded into existing projects. It provides a simple and efficient way to expose HTTP endpoints, making it ideal for use cases such as Kubernetes probes (e.g., liveness and readiness checks) or exposing metrics for Prometheus.

## Features
- **No Dependencies**: Built without external libraries, ensuring minimal footprint and easy integration.
- **Simple API**: Provides a straightforward interface for defining routes and handling HTTP requests.
- **Lightweight**: Designed to be fast and efficient, making it suitable for low-overhead applications.
- **Customizable**: Easily extendable to fit specific needs, allowing for custom route handling and response types.

## Limitations
- **HTTP/1.1 Only**: Currently supports only HTTP/1.1.
- **No SSL/TLS**: Does not support SSL/TLS.
- **No Built-in Authentication**: Does not include built-in authentication or authorization mechanisms. You can implement these features in your route handlers if needed.
- **No Built-in Middleware**: Does not provide middleware support. You can implement your own middleware-like functionality in the route handlers.
- **No Built-in CORS Support**: Does not include built-in CORS support. You can implement your own CORS handling in the route handlers.
- **No Built-in Request Validation**: Does not include built-in request validation. You can implement your own request validation in the route handlers.
- **No Built-in Response Compression**: Does not include built-in response compression. You can implement your own response compression in the route handlers.
- **No Built-in Error Handling**: Does not include built-in error handling. You can implement your own error handling in the route handlers.
- **Only text base body**: Currently, only text-based bodies are supported. 

## Getting Started

### Adding EmbedHTTP to Your Project
No maven artifact is available yet.

### Example Usage
Below is an example of how to use EmbedHTTP to expose some endpoints like a liveness probe and a metrics endpoint.

#### 1. Define Your Routes

```java
import net.uiqui.embedhttp.Router;
import net.uiqui.embedhttp.api.ContentType;
import net.uiqui.embedhttp.api.HttpHeader;
import net.uiqui.embedhttp.api.HttpResponse;
import net.uiqui.embedhttp.api.HttpStatusCode;

public class ExampleRoutes {
    public static Router createRouter() {
        var api = Router.newRouter();
        
        api.put("/resource/:id", request -> {
            var id = request.getPathParam("id");
            var name = request.getQueryParam("name");
            if (name == null || name.isEmpty()) {
                return HttpResponse.badRequest()
                        .setBody(ContentType.TEXT_PLAIN, "Name parameter is required.");
            }
            // Add to queue for resource update logic
            var response = "Resource " + id + " name's will be updated to " + name + ".";
            return HttpResponse.withStatus(HttpStatusCode.ACCEPTED)
                    .setHeader(HttpHeader.CACHE_CONTROL, "no-cache")
                    .setBody(ContentType.TEXT_PLAIN, response);
        });
        
        api.get("/health", request -> {
            // Check system health
            return HttpResponse.ok()
                    .setBody(ContentType.TEXT_PLAIN, "OK");
        });
        
        api.get("/metrics", request -> {
            var metrics = "metric_name 123";
            return HttpResponse.ok()
                    .setBody(ContentType.TEXT_PLAIN, metrics);
        });
        
        return api;
    }
}
```

Or you can define your routes in a more compact way:

```java
var api = Router.newRouter()
        .put("/resource/:id", request -> {
            // Some logic for handling the PUT /resource/:id
        })
        .get("/health", request -> {
            // Some logic for handling the GET /health
        })
        .get("/metrics", request -> {
            // Some logic for handling the GET /metrics 
        });
```

#### 2. Start the Server

```java
import net.uiqui.embedhttp.HttpServer;

public class Main {
    public static void main(String[] args) throws Exception {
        var router = ExampleRoutes.createRouter();
        var server = HttpServer.newInstance(8080);

        if (server.start(router)) {
            System.out.println("Server started on port: " + server.getInstancePort());
        } else {
            System.err.println("Failed to start the server.");
        }
    }
}
```

#### 3. Access Your Endpoints
Once the server is running, you can access the endpoints using a web browser or tools like `curl`:

```bash
# put request to update a resource
curl -X PUT http://localhost:8080/resource/123?name=test
# Output: Resource 123 name's will be updated to test.
curl http://localhost:8080/health
# Output: OK
curl http://localhost:8080/metrics
# Output: metric_name 123
```

## License
EmbedHTTP is licensed under the MIT License. See the [LICENSE](LICENSE) file for more details.

## Contributing
Contributions are welcome! If you have suggestions for improvements or new features, please open an issue or submit a pull request.
