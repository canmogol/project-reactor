package nl.ymor.reactor.examples;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

import static java.lang.Thread.sleep;

/**
 * This class has example methods not any tests.
 */
@Slf4j
public class ReactorExampleTests {

    /**
     * Logs the test name/description to std output.
     *
     * @param description test name.
     */
    private void logDescription(String description) {
        System.out.println("\n\n **** " + description.toUpperCase() + " ****");
    }

    /**
     * This example just prints "Hello" and "World" words in new lines.
     */
    @Test
    public void example_flux_just_print() {
        logDescription("Print Hello World words");
        Flux.just("Hello", "World")
                .subscribe(System.out::println);
    }

    /**
     * This example just prints words and letters from a list.
     */
    @Test
    public void example_create_flux_from_list() {
        List<String> words = Arrays.asList("the", "quick", "brown", "fox", "jumpes", "over", "the", "lazy", "dog");
        // the following line prints the whole list as a single entry, it does not
        // print every word in a new line, it prints the list on a single new line as,
        // [the, quick, brown, fox, jumped, over, the, lazy, dog]
        logDescription("Print words List.");
        Flux.just(words)
                .subscribe(System.out::println);
        // the following example will print all the words from the lust in a new line.
        // the
        // quick
        // ...
        // lazy
        // dog
        logDescription("Print every word on a new line.");
        Flux.fromIterable(words)
                .subscribe(System.out::println);

        // we can combine other operators, such as zip.
        // this will print a line number for every word, such as
        // 1. the
        // 2. quick
        // ...
        // 8. lazy
        // 9. dog
        logDescription("Print words with line numbers.");
        Flux.fromIterable(words)
                .zipWith(Flux.range(1, 100), (word, line) -> line + ". " + word)
                .subscribe(System.out::println);

        // If we want to print all the letters from all the words
        // we need to split every word to its letters,
        // but, if we use map for this, we won't get what we want.
        // The 'map' operator will create a Flux<String[]> which will print something like this,
        // 1. [Ljava.lang.String;@18271936
        // 2. [Ljava.lang.String;@606e4010
        // ...
        // 8. [Ljava.lang.String;@2cb4893b
        // 9. [Ljava.lang.String;@cc43f62
        logDescription("Print letter (String) arrays with line numbers.");
        Flux.fromIterable(words) // Flux<String>
                .map(word -> word.split("")) // Flux<String[]>
                .zipWith(Flux.range(1, 100), (word, line) -> line + ". " + word)
                .subscribe(System.out::println);

        // In order to print all the letters, we need use the 'flatMap',
        // this operation similar to the Java Stream API's flatMap.
        logDescription("Print all the letters with line numbers.");
        Flux.fromIterable(words) // Flux<String>
                .flatMap(word -> Flux.fromArray(word.split(""))) // Flux<String>
                .zipWith(Flux.range(1, 100), (word, line) -> line + ". " + word)
                .subscribe(System.out::println);

        // There should be 26 distinct letters.
        logDescription("Print all the distinct letters with line numbers in order.");
        Flux.fromIterable(words) // Flux<String>
                .flatMap(word -> Flux.fromArray(word.split(""))) // Flux<String>
                .distinct()
                .sort()
                .zipWith(Flux.range(1, 100), (word, line) -> line + ". " + word)
                .subscribe(System.out::println);
    }

    /**
     * This example shows time based flux examples.
     */
    @Test
    public void example_time_dimension_flux() throws InterruptedException {
        // The 'interval' method creates a Flux that emits a message on that period.
        logDescription("Print ticking clocks");
        // We create two clocks here, fast and slow.
        // Both of them will emit a string message on every tick.
        Flux<String> fastClock = Flux.interval(Duration.ofSeconds(1))
                .map(tick -> "fast " + tick);
        Flux<String> slowClock = Flux.interval(Duration.ofSeconds(1))
                .map(tick -> "slow " + tick);

        // Here we merge these two clocks, but at the same time, we filter them,
        // for the sake of the example, we decided that the 2nd tick is slow tick,
        // therefore, the fast clock won't emit anything on 2sd tick.
        // But the slow clock will only emit a message on 2nd tick.
        Flux<String> clock = Flux.merge(
                fastClock.filter(this::isFastTime),
                slowClock.filter(this::isSlowTime));

        // We also create a time Publisher which will emit the current time every second.
        Flux<LocalTime> timeFlux = Flux.interval(Duration.ofSeconds(1))
                .map(tick -> LocalTime.now());

        // Now we can use the clock feed and the time feed to print
        // the clock message on every second.
        // This should print a log similar to this,
        // fast 1 21:14:32.291502
        // slow 2 21:14:33.291396
        // fast 3 21:14:34.291044
        // fast 4 21:14:34.291044
        Flux<String> tickTime = clock.withLatestFrom(timeFlux, (tick, time) -> tick + " " + time);
        tickTime.subscribe(System.out::println);

        // if we don't sleep here, the TEST main thread will exit before we see
        // any log from the clock subscribers.
        sleep(5_000);
    }

    private boolean isFastTime(String tick) {
        return !tick.endsWith(" 2");
    }

    private boolean isSlowTime(String tick) {
        return tick.endsWith(" 2");
    }

    /**
     * This example shows regular old-style producer and listener.
     */
    @Test
    public void example_existing_feed_old_style() throws InterruptedException {
        logDescription("Print new prices, Regular Old-Style");
        // Here is a Feeder and a FeedListener, regular old-style listener and a producer, no Flux, no nothing.
        Feeder feeder = new Feeder();
        feeder.addListener(System.out::println);
        // if we don't sleep here, the TEST main thread will exit before we see anything.
        sleep(5_000);
    }

    /**
     * This example shows how to attach to an existing feed.
     */
    @Test
    public void example_existing_feed() throws InterruptedException {
        logDescription("Print new prices, Flux New-Style");
        Feeder feeder = new Feeder();
        // Now we'll create a Flux out of the Feeder.
        Flux.create(emitter -> feeder.addListener(emitter::next))
                .subscribe(System.out::println);
        // if we don't sleep here, the TEST main thread will exit before we see anything.
        sleep(5_000);
    }

}
