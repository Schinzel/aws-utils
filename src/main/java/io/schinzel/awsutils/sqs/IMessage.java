package io.schinzel.awsutils.sqs;


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
     * @return This for chaining
     */
    IMessage deleteMessageFromQueue();
}
