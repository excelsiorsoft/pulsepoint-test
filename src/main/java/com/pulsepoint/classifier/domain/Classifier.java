package com.pulsepoint.classifier.domain;

import java.util.concurrent.CompletableFuture;

/**
 * An async classifier API with two methods.  One to train the classifier given some document content and a category
 * ({@link #train(Classification)} and one to classify a specific web page given a URL ({@link #classify(String)})
 */
public interface Classifier {
    /**
     * Category string to return for URLs that can not be classified
     */
    String UNKNOWN_CATEGORY = "unknown";

    /**
     * Given a URL, returns a category for this URL.  Both unknown classification and incorrect URL input should result
     * in a result of {@link #UNKNOWN_CATEGORY}
     *
     * @param url well formed URL
     * @return a future that resolves to a category
     */
    CompletableFuture<String> classify(String url);

    /**
     * Train classifier by supplying it some document text and and an explicit category this document belongs to.  The
     * returned {@code CompletableFuture} should resolve to an <b>arbitrary</b> double representing information gain
     *
     * @param classification a valid classification
     * @return arbitrary information gain from training
     * @see Classification
     */
    CompletableFuture<Double> train(Classification classification);
}
