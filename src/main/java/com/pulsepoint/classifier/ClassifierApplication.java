package com.pulsepoint.classifier;

import com.pulsepoint.classifier.domain.SimpleClassifier;
import com.pulsepoint.classifier.embed.HTTPServer;
import com.pulsepoint.classifier.http.HTTPClient;
import com.pulsepoint.classifier.web.ClassifierResource;

import javax.annotation.PreDestroy;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Main classifier web application entry point.  Contains a main method
 *
 * @see HTTPServer
 * @see #main(String[])
 */
@ApplicationPath("/")
public class ClassifierApplication extends Application {

    private ExecutorService executor;
    private ClassifierResource classifierResource;

    /**
     * Construct a {@code ClassifierApplication}
     */
    public ClassifierApplication() {
        executor = Executors.newWorkStealingPool(Runtime.getRuntime().availableProcessors());
        classifierResource = new ClassifierResource(new SimpleClassifier(new HTTPClient(), executor));
    }

    @Override
    public Set<Object> getSingletons() {
        return Collections.singleton(classifierResource);
    }

    @PreDestroy
    public void shutDown() {
        executor.shutdownNow();
    }

    /**
     * Run an HTTP server with the classifier JAX-RS application
     *
     * @param args arguments; first argument can optionally supply a port number to run server on
     * @throws Exception on error
     */
    public static void main(String args[]) throws Exception {
        new HTTPServer<>(new ClassifierApplication())
                .setPort(args.length > 0 ? Integer.parseInt(args[0]) : HTTPServer.DEFAULT_PORT)
                .start()
                .join()
                .close();
    }
}
