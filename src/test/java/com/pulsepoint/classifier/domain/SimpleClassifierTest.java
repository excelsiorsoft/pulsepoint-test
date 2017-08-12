package com.pulsepoint.classifier.domain;

import com.google.common.util.concurrent.MoreExecutors;
import com.pulsepoint.classifier.http.HTTPClient;

/**
 * Tests for {@link SimpleClassifier}
 */
public class SimpleClassifierTest extends ClassifierTestBase<SimpleClassifier> {
    @Override
    SimpleClassifier getClassifier() {
        return new SimpleClassifier(new HTTPClient(), MoreExecutors.newDirectExecutorService());
    }
}
