package com.pulsepoint.classifier;

import com.google.common.base.Stopwatch;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.pulsepoint.classifier.domain.Classifier;
import com.pulsepoint.classifier.test.ClassifierApplicationScaffolding;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.touk.throwing.ThrowingConsumer;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.IntStream;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.MINUTES;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

/**
 * Tests for {@link ClassifierApplication}
 *
 * @see ClassifierApplicationScaffolding
 */
public class ClassifierApplicationTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClassifierApplicationTest.class);

    @ClassRule
    public static ClassifierApplicationScaffolding scaffolding = new ClassifierApplicationScaffolding()
            .setTestDataLocation("test.data.json")
            .setTrainingDataLocation("training.data.json");

    @Before
    public void before() {
        /* reset delay */
        scaffolding.setDelayMs(0);
    }

    /**
     * Test that bogus input does not cause errors and returns {@link Classifier#UNKNOWN_CATEGORY} instead
     */
    @Test
    public void testUnknownClassification() throws ExecutionException, InterruptedException {
        Arrays.asList(null, "", "not a url").forEach(ThrowingConsumer.unchecked(url ->
                assertThat(scaffolding.getRemoteClassifier().classify(url).get()).isEqualTo(Classifier.UNKNOWN_CATEGORY)
        ));
    }

    /**
     * Test classification accuracy
     */
    @Test
    public void testAccuracy() {
        /* count up correct classifications */
        AtomicLong correctClassifications = new AtomicLong(0);
        scaffolding.getTestData().stream().forEach(ThrowingConsumer.unchecked(testData -> {
            String classification = scaffolding.getRemoteClassifier().classify(testData.getUrl()).get();
            boolean isCorrect = testData.getExpectedCategory().equals(classification);
            if (isCorrect) {
                correctClassifications.incrementAndGet();
            }
            LOGGER.info("URL {} classified {} (expected {} classified as {})",
                    testData.getUrl(), isCorrect ? "correctly" : "incorrectly", testData.getExpectedCategory(), classification);
        }));
        double accuracy = correctClassifications.get() / (double) scaffolding.getTestData().size();
        LOGGER.info("Classified {}/{} URLs correctly ({}%)", correctClassifications.get(), scaffolding.getTestData().size(), accuracy * 100);
        /* assert 62.5% accuracy */
        assertThat(accuracy).isGreaterThanOrEqualTo(0.625);
    }

    /**
     * Test concurrent performance
     *
     * @throws BrokenBarrierException if an exception occurs
     * @throws InterruptedException   if an exception occurs
     */
    @Test
    public void testConcurrentPerformance() throws BrokenBarrierException, InterruptedException {
        /* configure test parameters */
        final int delayMs = 250;
        final int nThreads = 3;
        final int nIterations = 2;
        final AtomicLong attemptedClassifications = new AtomicLong(0);
        final AtomicLong correctClassifications = new AtomicLong(0);
        final CyclicBarrier startBarrier = new CyclicBarrier(nThreads + 1);

        scaffolding.setDelayMs(delayMs);
        /* fire off a bunch of concurrent classification requests */
        ExecutorService executor = Executors.newFixedThreadPool(nThreads, new ThreadFactoryBuilder()
                .setNameFormat("concurrent-test-thread-%d").build());
        try {
            IntStream.range(0, nThreads).forEach(threadNum -> executor.submit(() -> {
                startBarrier.await();
                IntStream.range(0, nIterations).forEach(iteration -> {
                    List<ClassifierApplicationScaffolding.TestData> testData = scaffolding.getTestData();
                    Collections.shuffle(testData);
                    testData.forEach(ThrowingConsumer.unchecked(testedData -> {
                        attemptedClassifications.incrementAndGet();
                        if (testedData.getExpectedCategory().equals(
                                scaffolding.getRemoteClassifier().classify(testedData.getUrl()).get())) {
                            correctClassifications.incrementAndGet();
                        }
                    }));
                });
                return 3;
            }));
            Stopwatch stopwatch = Stopwatch.createStarted();
            /* fire off start barrier */
            startBarrier.await();
            /* shut down executor */
            executor.shutdown();
            /* wait for termination */
            if (!executor.awaitTermination(5, MINUTES)) {
                fail("Concurrent test took too long to perform.");
            }
            long testDurationMillis = stopwatch.elapsed(MILLISECONDS);

            /* log results */
            LOGGER.info("Performed {} classifications via {} threads in {}ms ({}c/s or {}ms/c)",
                    attemptedClassifications.get(),
                    nThreads,
                    testDurationMillis,
                    (attemptedClassifications.get() / (double) testDurationMillis) * 1000,
                    testDurationMillis / (double) attemptedClassifications.get());

            /* assert attempted classifications */
            assertThat(attemptedClassifications.get()).isEqualTo(scaffolding.getTestData().size() * nThreads * nIterations);
            /* assert accuracy */
            assertThat(correctClassifications.get() / (double) attemptedClassifications.get()).isGreaterThanOrEqualTo(0.625);
            /* assert that each classification took < 3x the time it took to fetch pages */
            assertThat(testDurationMillis).isLessThanOrEqualTo(3 * delayMs * attemptedClassifications.get());
        } finally {
            executor.shutdownNow();
        }
        Stopwatch.createStarted();

    }


}
