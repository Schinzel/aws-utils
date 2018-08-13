package io.schinzel.awsutils.sqs;


/**
 * The purpose of this interface is to send messages to a queue.
 *
 * @author Schinzel
 */
public interface IQueueSender {

    /**
     *
     * @param message The message to send
     * @return This for chaining
     */
    IQueueSender send(String message);
}
