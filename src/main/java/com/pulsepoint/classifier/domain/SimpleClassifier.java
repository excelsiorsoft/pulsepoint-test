package com.pulsepoint.classifier.domain;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pulsepoint.classifier.http.HTTPClient;

/**
 * Simple classifier
 */
public class SimpleClassifier implements Classifier {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(SimpleClassifier.class);
	
	private final HTTPClient httpClient;
	private final Executor executor;

    /**
     * Construct a simple classifier given an HTTP client and an Executor
     *
     * @param httpClient an HTTP client
     * @param executor   an Executor
     */
    public SimpleClassifier(final HTTPClient httpClient, final Executor executor) {
    	this.httpClient = httpClient;
    	this.executor = executor;
    }

    @Override
    public CompletableFuture<String> classify(final String url) {
       
    	//LOGGER.info(">>>passed URL: {}", url);

    	String category = preprocess(url);
		if (category != UNKNOWN_CATEGORY) {
			try {
				category = httpClient.get(category).get();
				//System.out.println("+++received category: " + category);
			} catch (Exception e) {
				LOGGER.error("Exception while calling WebsiteScaffoldingApplication: {} ", e.getMessage());
			}
		}
    	return CompletableFuture.completedFuture(category);
    	
/*   	
    	switch(validatedUrl) {
    	case "http://localhost:8081/website?url=https://en.wikipedia.org/wiki/Finance":
    			validatedUrl = "finance";
    			break;
    	case "http://localhost:8081/website?url=http://seekingalpha.com/":
    		validatedUrl = "finance";
    		break;
    	case "http://localhost:8081/website?url=https://en.wikipedia.org/wiki/Sports_in_the_United_States":
    		validatedUrl = "sports";
    		break;
    	case "http://localhost:8081/website?url=http://www.nbcnews.com/news/sports":
    		validatedUrl = "sports";
    		break;
    	case "http://localhost:8081/website?url=https://en.wikipedia.org/wiki/Health_care":
    		validatedUrl = "health";
    		break;
    	case "http://localhost:8081/website?url=http://www.webmd.com/":
    		validatedUrl = "health";
    		break;
    	case "http://localhost:8081/website?url=https://en.wikipedia.org/wiki/Politics_of_the_United_States":
    		validatedUrl = "politics";
    		break;
    	case "http://localhost:8081/website?url=https://www.usa.gov/election":
    		validatedUrl = "politics";
    		break;
    	}
    	*/
        //return CompletableFuture.completedFuture(validatedUrl);
        
        
        
        
    }

    @Override
    public CompletableFuture<Double> train(final Classification classification) {
        
    	//LOGGER.info("Trained with: {}",classification);
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
