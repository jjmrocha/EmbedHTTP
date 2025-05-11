package net.uiqui.embedhttp.examples.readme;

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
