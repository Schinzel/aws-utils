package io.schinzel.queue;


/**
 * The purpose of this interface is to send messages to a queue.
 *
 * @author Schinzel
 */
public interface IQueueProducer {

    /**
     *
     * @param message The message to send
     * @return This for chaining
     */
    IQueueProducer send(String message);
}
