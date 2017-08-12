package com.pulsepoint.classifier.http;


import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClient;

import java.util.concurrent.CompletableFuture;

/**
 * An asynchronous HTTP client
 */
public class HTTPClient {
    final AsyncHttpClient asyncHttpClient = new DefaultAsyncHttpClient();

    /**
     * Given a URL, execute a "GET" request and return {@link CompletableFuture} that resolves to response body after
     * request is complete
     *
     * @param url URL to fetch
     * @return {@code CompletableFuture} that resolves to document body
     */
    public CompletableFuture<String> get(String url) {
        return asyncHttpClient.prepareGet(url).execute().toCompletableFuture().thenApply(response -> response.getResponseBody());
    }
}
