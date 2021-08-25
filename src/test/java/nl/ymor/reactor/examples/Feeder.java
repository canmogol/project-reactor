package nl.ymor.reactor.examples;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;

import static java.lang.Thread.sleep;

/**
 * Price Feeder, generates new prices and notifies all the listeners.
 */
public class Feeder {

    private final Random random = new Random();
    private final List<FeedListener> listeners = new ArrayList<>();

    /**
     * Feeder constructor, creates a new price and an notifies the listeners.
     */
    public Feeder() {
        Executors.newSingleThreadExecutor()
                .submit(() -> {
                    while (true) {
                        int newPrice = random.nextInt(100);
                        for (FeedListener listener : listeners) {
                            listener.newPrice(newPrice);
                        }
                        sleep(1_000);
                    }
                });
    }

    /**
     * Adds the listener to the listener list, on the next notification call,
     * this listener will also get the new price.
     *
     * @param listener price listener.
     */
    public void addListener(FeedListener listener) {
        listeners.add(listener);
    }

}
