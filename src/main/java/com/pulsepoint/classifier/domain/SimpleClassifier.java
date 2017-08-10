package com.pulsepoint.classifier.domain;

import com.pulsepoint.classifier.http.HTTPClient;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * Simple classifier
 */
public class SimpleClassifier implements Classifier {

    /**
     * Construct a simple classifier given an HTTP client and an Executor
     *
     * @param httpClient an HTTP client
     * @param executor   an Executor
     */
    public SimpleClassifier(HTTPClient httpClient, Executor executor) {
    }

    @Override
    public CompletableFuture<String> classify(String url) {
        /* TODO: implement me */
        return CompletableFuture.completedFuture(UNKNOWN_CATEGORY);
    }

    @Override
    public CompletableFuture<Double> train(Classification classification) {
        /* TODO: implement me */
        return CompletableFuture.completedFuture(0.0);
    }
}
