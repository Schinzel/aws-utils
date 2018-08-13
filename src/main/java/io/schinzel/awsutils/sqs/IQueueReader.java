package io.schinzel.awsutils.sqs;


/**
 * The purpose of this interface is to read messages from a queue.
 *
 * @author Schinzel
 */
public interface IQueueReader {

    /**
     * @return A message
     */
    SqsMessage getMessage();
}
