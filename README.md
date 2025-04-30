# EmbedHTTP

EmbedHTTP is a lightweight, dependency-free HTTP server designed to be easily embedded into existing projects. It provides a simple and efficient way to expose HTTP endpoints, making it ideal for use cases such as Kubernetes probes (e.g., liveness and readiness checks) or exposing metrics for Prometheus.

## Features

- **No Dependencies**: Built without external libraries, ensuring minimal footprint and easy integration.
- **Simple API**: Provides a straightforward interface for defining routes and handling HTTP requests.
- **Lightweight**: Designed to be fast and efficient, making it suitable for low-overhead applications.
- **Customizable**: Easily extendable to fit specific needs, allowing for custom route handling and response types.

## Getting Started

### Adding EmbedHTTP to Your Project

Since EmbedHTTP has no dependencies, you can directly include the source code in your project or package it as a library.

### Example Usage

Below is an example of how to use EmbedHTTP to expose a liveness probe and a metrics endpoint.

#### 1. Define Your Routes

```java
import net.uiqui.embedhttp.Router;
import net.uiqui.embedhttp.api.ContentType;
import net.uiqui.embedhttp.api.HttpHeader;
import net.uiqui.embedhttp.api.HttpResponse;
import net.uiqui.embedhttp.api.HttpStatusCode;

import java.net.http.HttpHeaders;

public class ExampleRoutes {
    public static Router createRouter() {
        return Router.newRouter()
                .put("/resource/:id", request -> {
                    var id = request.getPathParam("id");
                    var name = request.getQueryParam("name");
                    if (name == null || name.isEmpty()) {
                        return HttpResponse.badRequest(ContentType.TEXT_PLAIN, "Name parameter is required.");
                    }
                    // Add to queue for resource update logic
                    var response = "Resource " + id + " name's will be updated to " + name + ".";
                    return HttpResponse.withStatus(HttpStatusCode.ACCEPTED)
                            .setBody(ContentType.TEXT_PLAIN, response)
                            .setHeader(HttpHeader.CACHE_CONTROL, "no-cache");
                })
                .get("/health", request -> {
                    // Check system health
                    return HttpResponse.ok(ContentType.TEXT_PLAIN, "OK");
                })
                .get("/metrics", request -> {
                    var metrics = "metric_name 123";
                    return HttpResponse.ok(ContentType.TEXT_PLAIN, metrics);
                });
    }
}
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
