package io.schinzel.queue;


/**
 * The purpose of this class is to represent a message from a queue.
 *
 * @author Schinzel
 */
public interface IMessage {
    /**
     * @return The body of the message
     */
    String getBody();

    /**
     * @return The number of times a message has been read from the queue but not deleted
     */
    int getNumberOfTimesRead();

    /**
     * @return This for chaining
     */
    IMessage deleteMessageFromQueue();

}
