package com.pulsepoint.classifier.embed;

import com.google.common.base.MoreObjects;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;

import javax.ws.rs.core.Application;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.Objects;
import java.util.stream.IntStream;

/**
 * An embeddable HTTP server for JAX-RS {@link Application} instances
 */
public class HTTPServer<T extends Application> implements AutoCloseable {
    /**
     * Default HTTP server port
     */
    public static int DEFAULT_PORT = 8080;

    private int port = 0;
    private Server jettyServer;
    private final T applicationInstance;

    /**
     * Construct a {@code Server} for an application instance
     *
     * @param applicationInstance application instance
     */
    public HTTPServer(T applicationInstance) {
        this.applicationInstance = applicationInstance;
    }

    /**
     * Get application
     *
     * @return application
     */
    public T getApplication() {
        return applicationInstance;
    }

    /**
     * Set server port
     *
     * @param port server port
     * @return {@code this}
     */
    public HTTPServer<T> setPort(int port) {
        this.port = port;
        return this;
    }

    /**
     * Return server port
     *
     * @return server port
     */
    public int getPort() {
        return port;
    }

    /**
     * Start server
     *
     * @return {@code this}
     * @throws Exception if server could not be started
     */
    public HTTPServer<T> start() throws Exception {
        port = port == 0 ? findOpenPort(DEFAULT_PORT) : port;
        jettyServer = new Server(port);
        (new ServletContextHandler(jettyServer, "/*")).addServlet(
                new ServletHolder(new ServletContainer(ResourceConfig.forApplication(applicationInstance)
                        .register(JacksonFeature.class))), "/*");
        jettyServer.start();
        return this;
    }


    /**
     * Await for server to shut down
     *
     * @return {@code this}
     * @throws InterruptedException if interrupted
     */
    public HTTPServer<T> join() throws InterruptedException {
        jettyServer.join();
        return this;
    }

    /**
     * Shut down server
     *
     * @throws Exception if server could not be shut down
     */
    @Override
    public void close() throws Exception {
        try {
            jettyServer.stop();
        } finally {
            jettyServer.destroy();
        }
    }

    /**
     * Find the first available TCP port on all machine interfaces starting with the supplied port.
     *
     * @param startInclusive port to start with
     * @return first open port
     */
    public static int findOpenPort(int startInclusive) {
        return IntStream.range(startInclusive, startInclusive + 1000).filter(port -> {
            try (ServerSocket ignored = new ServerSocket(port)) {
                return true;
            } catch (IOException e) {
                return false;
            }
        }).findFirst().getAsInt();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof HTTPServer)) return false;
        HTTPServer<?> that = (HTTPServer<?>) o;
        return port == that.port &&
                Objects.equals(applicationInstance, that.applicationInstance);
    }

    @Override
    public int hashCode() {
        return Objects.hash(port, applicationInstance);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("port", port)
                .add("applicationInstance", applicationInstance)
                .toString();
    }
}
