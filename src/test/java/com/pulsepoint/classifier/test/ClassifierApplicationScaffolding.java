package com.pulsepoint.classifier.test;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.MoreObjects;
import com.google.common.io.Resources;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.pulsepoint.classifier.ClassifierApplication;
import com.pulsepoint.classifier.domain.Classification;
import com.pulsepoint.classifier.domain.Classifier;
import com.pulsepoint.classifier.embed.HTTPServer;
import com.pulsepoint.classifier.web.ClassifierResource;
import org.junit.rules.ExternalResource;
import pl.touk.throwing.ThrowingSupplier;

import javax.annotation.PreDestroy;
import javax.ws.rs.*;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.InvocationCallback;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.pulsepoint.classifier.web.ClassifierResource.*;

/**
 * A JUnit rule for scaffolding an HTTP server running {@link ClassifierApplication}, an HTTP client for {@link
 * ClassifierApplication} and an HTTP server that serves static content to mimic real websites for offline testing.
 *
 * @see ClassifierApplication
 * @see WebsiteScaffoldingApplication
 * @see ClassifierClient
 */
public class ClassifierApplicationScaffolding extends ExternalResource {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final String HTTP_LOCALHOST_PREFIX = "http://localhost:";
    /* configuration */
    private String testDataLocation;
    private String trainingDataLocation;
    /* scaffolded test components */
    private HTTPServer<ClassifierApplication> classifierServer;
    private HTTPServer<WebsiteScaffoldingApplication> testWebsiteServer;
    private ClassifierClient remoteClassifier;

    /* public methods and classes */

    /**
     * An HTTP client for remote {@link ClassifierApplication}s.  Implements the standard
     * {@link Classifier} interface
     */
    public static class ClassifierClient implements Classifier, AutoCloseable {
        private final Client jaxRsClient = ClientBuilder.newBuilder().build();
        private final String serverURL;

        /**
         * Construct a {@code ClassifierClient} for supplied server URL
         *
         * @param serverURL server URL
         */
        public ClassifierClient(String serverURL) {
            this.serverURL = serverURL;
        }

        @Override
        public CompletableFuture<String> classify(String url) {
            return with(new Callback<String>() {}, 
	            		callback -> jaxRsClient.target(serverURL + RESOURCE_PATH + CLASSIFY_PATH)
	                    .queryParam(URL_PARAM, url)
	                    .request()
	                    .async()
	                    .get(callback));

        }

        @Override
        public CompletableFuture<Double> train(Classification classification) {
            return with(new Callback<Double>() {},
	            		callback -> jaxRsClient.target(serverURL + RESOURCE_PATH + TRAIN_PATH)
	                    .request()
	                    .async()
	                    .post(Entity.entity(classification, MediaType.APPLICATION_JSON), callback));
        }

        private static <T> CompletableFuture<T> with(Callback<T> callback, Consumer<Callback<T>> consumer) {
            consumer.accept(callback);
            return callback;
        }

        @Override
        public void close() throws Exception {
            jaxRsClient.close();
        }

        private static class Callback<T> extends CompletableFuture<T> implements InvocationCallback<T> {
            @Override
            public void completed(T t) {
                complete(t);
            }

            @Override
            public void failed(Throwable throwable) {
                completeExceptionally(throwable);
            }
        }
    }

    /**
     * A test data class
     *
     * @see WebsiteScaffoldingApplication#getTestData()
     */
    public static class TestData {
        private final String expectedCategory;
        private final String url;

        public TestData(String expectedCategory, String url) {
            this.expectedCategory = expectedCategory;
            this.url = url;
        }

        /**
         * Get the expected category of content found at url
         *
         * @return expected category of content found at url
         */
        public String getExpectedCategory() {
            return expectedCategory;
        }

        /**
         * Get content URL
         *
         * @return url
         */
        public String getUrl() {
            return url;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof TestData)) return false;
            TestData testData = (TestData) o;
            return Objects.equals(expectedCategory, testData.expectedCategory) &&
                    Objects.equals(url, testData.url);
        }

        @Override
        public int hashCode() {
            return Objects.hash(expectedCategory, url);
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("expectedCategory", expectedCategory)
                    .add("url", url)
                    .toString();
        }
    }

    /**
     * Set the test data JSON file classpath location (see {@link WebsiteScaffoldingApplication.SampleData} for format)
     *
     * @param testDataLocation test data classpath location
     * @return {@code this}
     */
    public ClassifierApplicationScaffolding setTestDataLocation(String testDataLocation) {
        this.testDataLocation = testDataLocation;
        return this;
    }

    /**
     * Set the training data JSON file classpath location (see {@link Classification} for format)
     *
     * @param trainingDataLocation training data classpath location
     * @return {@code this}
     */
    public ClassifierApplicationScaffolding setTrainingDataLocation(String trainingDataLocation) {
        this.trainingDataLocation = trainingDataLocation;
        return this;
    }

    /**
     * Set test data server response delay in milliseconds
     *
     * @param millis milliseconds to delay test data server responses
     */
    public void setDelayMs(long millis) {
        testWebsiteServer.getApplication().setDelayMs(millis);
    }

    /**
     * Get a list of sample test data; can be used for test assertions
     *
     * @return list of sample test data
     */
    public List<TestData> getTestData() {
    	
    	List<TestData> result = 
       /* return*/ testWebsiteServer.getApplication().getTestData().stream().map(sample ->
                new TestData(sample.category, 
                		HTTP_LOCALHOST_PREFIX + testWebsiteServer.getPort() +
                        UriBuilder.fromPath(WebsiteScaffoldingApplication.WEBSITE_PATH)
                                .queryParam(ClassifierResource.URL_PARAM, sample.url)
                                .build()))
                .collect(Collectors.toList());
    	System.out.println("test data: "+result);
    	return result;
    }

    /**
     * Get {@link Classifier} instance that makes request to {@link com.pulsepoint.classifier.web.ClassifierResource}
     * via HTTP
     *
     * @return test classifier
     */
    public Classifier getRemoteClassifier() {
        return remoteClassifier;
    }

    /* junit rule overrides */

    @Override
    protected void before() throws Throwable {
        classifierServer = new HTTPServer<>(new ClassifierApplication()).start();
        testWebsiteServer = new HTTPServer<>(new WebsiteScaffoldingApplication(testDataLocation)).start();
        remoteClassifier = new ClassifierClient(HTTP_LOCALHOST_PREFIX + classifierServer.getPort());

        /* train classifier from training.data.json for test purposes */
        CompletableFuture.allOf(load(trainingDataLocation, new TypeReference<List<Classification>>() {})
                .stream().map(classification -> remoteClassifier.train(classification)).collect(Collectors.toList())
                .toArray((new CompletableFuture[0]))).get();
    }

    @Override
    protected void after() {
        closeQuiety(classifierServer, testWebsiteServer, remoteClassifier);
    }

    /* utility methods and classes */

    /**
     * Close any number of {@link AutoCloseable} instances quietly
     *
     * @param closeables array of {@link AutoCloseable} elements
     */
    private static void closeQuiety(AutoCloseable... closeables) {
        Arrays.stream(closeables).forEach(closeable -> {
            try {
                closeable.close();
            } catch (Exception ignore) {
            }
        });
    }

    /**
     * Load a JSON resource from specified classpath location
     *
     * @param resourceLocation classpath location
     * @param type             concrete object type
     * @param <T>              concrete object type
     * @return JSON resource
     */
    private static <T> T load(String resourceLocation, TypeReference<T> type) {
        return ThrowingSupplier.<T, IOException>unchecked(() -> {
            try (InputStream is = Resources.asByteSource(Resources.getResource(resourceLocation)).openBufferedStream()) {
                return objectMapper.readValue(is, type);
            }
        }).get();
    }

    /**
     * An application that provides some fake web content; useful strictly for scaffolding purposes to mimic real
     * pages.
     * <p/>
     * This class needs to be public in order to be picked up by jaxrs
     */
    @ApplicationPath("/")
    @Path("/")
    public static class WebsiteScaffoldingApplication extends Application {
        public static final String WEBSITE_PATH = "/website";
        private final List<SampleData> testData;
        private final ScheduledExecutorService executorService;
        private volatile long delayMs = 0;

        public static class SampleData {
            public String category;
            public String url;
            public String content;
        }

        public WebsiteScaffoldingApplication(String testDataLocation) throws IOException {
            executorService = Executors.newScheduledThreadPool(5, new ThreadFactoryBuilder().setNameFormat(getClass().getSimpleName() + "-scheduler-thread-%d").build());
            testData = load(testDataLocation, new TypeReference<List<SampleData>>() {});
        }

        @Override
        public Set<Object> getSingletons() {
            return Collections.singleton(this);
        }

        public List<SampleData> getTestData() {
            return testData;
        }

        //@SuppressWarnings("VoidMethodAnnotatedWithGET")
        @Path(WEBSITE_PATH)
        //@Produces(MediaType.TEXT_HTML)
        @Produces(MediaType.APPLICATION_JSON)
        @GET
        public void get(@QueryParam(ClassifierResource.URL_PARAM) String url, @Suspended final AsyncResponse asyncResponse) {
            SampleData data = testData.stream().filter(sampleData -> url.equals(sampleData.url)).findFirst().get();
            executorService.schedule(() -> asyncResponse.resume(data.category/*.content*/), delayMs, TimeUnit.MILLISECONDS);
        }

        @PreDestroy
        public void destroy() {
            executorService.shutdownNow();
        }

        public WebsiteScaffoldingApplication setDelayMs(long delayMs) {
            this.delayMs = delayMs;
            return this;
        }
    }
}
