package com.pulsepoint.classifier.web;

import com.pulsepoint.classifier.domain.Classification;
import com.pulsepoint.classifier.domain.Classifier;

import javax.inject.Singleton;
import javax.ws.rs.*;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.MediaType;
import java.util.function.BiConsumer;

/**
 * An async JAX-RS resources that wraps a {@link Classifier} instance and exposes it via HTTP
 */
@Path(ClassifierResource.RESOURCE_PATH)
@Singleton
public class ClassifierResource {
    /**
     * HTTP path under application context path that this resource is available at
     */
    public static final String RESOURCE_PATH = "/classifier";
    /**
     * HTTP path under resource path that the train endpoint is available at
     *
     * @see #train(Classification, AsyncResponse)
     */
    public static final String TRAIN_PATH = "/train";
    /**
     * HTTP path under resource path that the classify endpoitn is available at
     *
     * @see #classify(String, AsyncResponse)
     */
    public static final String CLASSIFY_PATH = "/classify";
    /**
     * Query parameter name for URL
     *
     * @see #classify(String, AsyncResponse)
     */
    public static final String URL_PARAM = "url";

    /**
     * Wrapped classifier instance
     */
    private final Classifier classifier;

    /**
     * Construct a {@code ClassifierResource} that relies on the supplied {@code Classifier}
     *
     * @param classifier a {@code Classifier} instance
     */
    public ClassifierResource(Classifier classifier) {
        this.classifier = classifier;
    }

    /**
     * Classification training endpoint; accepts a document and category the document belongs to, replies with
     * information gain
     *
     * @param classification classification
     * @param asyncResponse  asyncResponse
     * @see Classifier#train(Classification)
     */
    @POST
    @Path(TRAIN_PATH)
    @Consumes(MediaType.APPLICATION_JSON)
    public void train(Classification classification,
                      @Suspended final AsyncResponse asyncResponse) {
        classifier.train(classification).whenComplete(resume(asyncResponse));
    }

    /**
     * Classification endpoint; accepts a {@code URL}, replies with category of content at URL
     *
     * @param url           a {@code URL}
     * @param asyncResponse asyncResponse
     * @see Classifier#classify(String)
     */
    @SuppressWarnings("VoidMethodAnnotatedWithGET")
    @GET
    @Path(CLASSIFY_PATH)
    public void classify(@QueryParam(URL_PARAM) String url, @Suspended final AsyncResponse asyncResponse) {
        classifier.classify(url).whenComplete(resume(asyncResponse));
    }

    /**
     * Given an {@code AsyncResponse} return a {@code BiConsumer} that resumes the response with either an error or a
     * throwable
     *
     * @param asyncResponse asyncResponse
     * @param <T>           concrete object type to resume response with
     * @return a {@code BiConsumer} that resumes the response with either an error or a throwable
     */
    private <T> BiConsumer<T, Throwable> resume(AsyncResponse asyncResponse) {
        return (result, throwable) -> {
            if (throwable != null) {
                asyncResponse.resume(throwable);
            } else {
                asyncResponse.resume(result);
            }
        };
    }
}
