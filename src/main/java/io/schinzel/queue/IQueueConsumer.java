package io.schinzel.queue;


import io.schinzel.awsutils.sqs.SqsMessage;

/**
 * The purpose of this interface is to read messages from a queue.
 *
 * @author Schinzel
 */
public interface IQueueConsumer {

    /**
     * @return A message
     */
    SqsMessage getMessage();
}
