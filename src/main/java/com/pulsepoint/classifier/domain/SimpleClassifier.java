package com.pulsepoint.classifier.domain;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pulsepoint.classifier.http.HTTPClient;

/**
 * Simple classifier
 */
public class SimpleClassifier implements Classifier {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(SimpleClassifier.class);
	
	private final HTTPClient httpClient;
	private final ExecutorService executor;

    /**
     * Construct a simple classifier given an HTTP client and an Executor
     *
     * @param httpClient an HTTP client
     * @param executor   an Executor
     */
    public SimpleClassifier(final HTTPClient httpClient, final Executor executor) {
    	this.httpClient = httpClient;
    	this.executor = (ExecutorService) executor;
    }

    @Override
	public CompletableFuture<String> classify(final String url) {

		String doctoredUrl = preprocess(url);
		if (!doctoredUrl.equalsIgnoreCase(UNKNOWN_CATEGORY)) {
			Future<CompletableFuture<String>> category = (Future<CompletableFuture<String>>) executor.submit(() -> {
				CompletableFuture<String> cf = null;
				cf = httpClient.get(doctoredUrl);
				return cf;
			});
			CompletableFuture<String> result = null;
			try {
				result = category.get();
			} catch (InterruptedException | ExecutionException e) {
				LOGGER.error("Exception while calling WebsiteScaffoldingApplication: {} ", e.getMessage());
			}
			return result;
		} else {
			return CompletableFuture.completedFuture(UNKNOWN_CATEGORY);
		}
    	
    	/*String category = preprocess(url);
		if (category != UNKNOWN_CATEGORY) {
			try {
				category = httpClient.get(category).get();
			} catch (Exception e) {
				LOGGER.error("Exception while calling WebsiteScaffoldingApplication: {} ", e.getMessage());
			}
		}
    	return CompletableFuture.completedFuture(category);
       */
    }

    @Override
    public CompletableFuture<Double> train(final Classification classification) {        
        return CompletableFuture.completedFuture(0.0);
    }
    
    private String preprocess(String url) {
        try {
            new URL(url);
            return url;
        } catch (MalformedURLException e) {
            return UNKNOWN_CATEGORY;
        }
    }
}
