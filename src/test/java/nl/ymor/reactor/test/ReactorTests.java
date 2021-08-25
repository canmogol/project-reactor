package nl.ymor.reactor.test;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.Objects;

/**
 * This class has simple test examples.
 */
@Slf4j
public class ReactorTests {

    /**
     * Logs the test name/description to std output.
     *
     * @param description test name.
     */
    private void logDescription(String description) {
        System.out.println("\n\n **** " + description.toUpperCase() + " ****");
    }

    /**
     * This test example shows how to validate values in a Flux.
     */
    @Test
    public void step_by_step_test_example() {
        logDescription("Step by step verifier example.");
        // Nothing happens until we subscribe. This code won't execute until there is a subscription!
        Flux<String> words = Flux.just("all", "work", "no", "play", "makes", "Jack", "a", "dull", "boy")
                .filter(name -> name.length() > 3)
                .map(String::toUpperCase);

        // tests the emitted events/messages, verifies contents and completion of the Flux.
        StepVerifier
                // create() method creates the FirstStep object that implements the Step interface.
                .create(words)
                // expect methods expects one or more of the Flux's type, in this case it's a String.
                .expectNext("WORK")
                .expectNextMatches(name -> name.startsWith("PLA"))
                .expectNext("MAKES", "JACK", "DULL")
                .expectComplete()
                // nothing happens until the verify method is called.
                .verify();
    }


    /**
     * This example test shows how to handle Exceptions.
     */
    @Test
    public void expect_an_exception_test_example() {
        // Again we start with words.
        final Flux<String> words = Flux.just("all", "work", "no", "play", "makes", "Jack", "a", "dull", "boy");
        // expected error message
        final String expectedErrorMessage = "Surviving the Nightmare";
        // Here we concatenate an exception to the 'words' Flux.
        final Flux<String> wordsWithError = words.concatWith(
                Mono.error(new IllegalArgumentException(expectedErrorMessage))
        );

        // tests the number of elements in the Flux and also the Exception.
        StepVerifier
                .create(wordsWithError)
                // there are 9 elements in the Flux<String> words.
                .expectNextCount(9)
                .expectErrorMatches(throwable -> throwable instanceof IllegalArgumentException
                        && Objects.equals(throwable.getMessage(), expectedErrorMessage))
                .verify();
    }


    /**
     * This example test shows how to test the time-based events.
     */
    @Test
    public void time_based_events_real_test_example() {
        // this will create a Flux that emits a message every second for 5 seconds.
        Flux<Long> fiveSecondsFlux = Flux.interval(Duration.ofSeconds(1))
                .take(5);
        // the StepVerifier tests that the Flux emits a message every seconds for 5 seconds.
        StepVerifier
                // This test will take 5 seconds to finish, because we didn't create the Flux
                // in the withVirtualTime method!
                .withVirtualTime(() -> fiveSecondsFlux)
                .expectSubscription()
                .expectNoEvent(Duration.ofSeconds(1))
                .expectNext(0L)
                .thenAwait(Duration.ofSeconds(1))
                .expectNext(1L)
                .thenAwait(Duration.ofSeconds(1))
                .expectNext(2L)
                .thenAwait(Duration.ofSeconds(1))
                .expectNext(3L)
                .thenAwait(Duration.ofSeconds(1))
                .expectNext(4L)
                .verifyComplete();
    }

    /**
     * This example test shows how to test the time-based events.
     */
    @Test
    public void time_based_events_virtual_test_example() {
        // the StepVerifier tests that the Flux emits a message every seconds for 5 seconds.
        StepVerifier
                // this test will finish right away, it will not take 5 seconds,
                // because we create the Flux inside the withVirtualTime method.
                .withVirtualTime(() -> Flux.interval(Duration.ofSeconds(1))
                        .take(5))
                .expectSubscription()
                .expectNoEvent(Duration.ofSeconds(1))
                .expectNext(0L)
                .thenAwait(Duration.ofSeconds(1))
                .expectNext(1L)
                .thenAwait(Duration.ofSeconds(1))
                .expectNext(2L)
                .thenAwait(Duration.ofSeconds(1))
                .expectNext(3L)
                .thenAwait(Duration.ofSeconds(1))
                .expectNext(4L)
                .verifyComplete();
    }
}
