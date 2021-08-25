package nl.ymor.reactor.examples;

/**
 * Listens to the price feed.
 */
public interface FeedListener {
    /**
     * Gets the new price when it's changed.
     *
     * @param price new price.
     */
    void newPrice(int price);
}
