package net.uiqui.embedhttp.examples.readme;

import net.uiqui.embedhttp.Router;
import net.uiqui.embedhttp.api.ContentType;
import net.uiqui.embedhttp.api.HttpHeader;
import net.uiqui.embedhttp.api.HttpResponse;
import net.uiqui.embedhttp.api.HttpStatusCode;

public class ExampleRoutes {
    public static Router createRouter() {
        var api = Router.newRouter();

        api.put("/resource/:id", request -> {
            var id = request.getPathParameter("id");
            var name = request.getQueryParameter("name");
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