package io.schinzel.awsutils.sqs;


/**
 * The purpose of this interface is to read messages from an AWS SQS queue.
 *
 * @author Schinzel
 */
public interface ISqsReader {

    /**
     * @return A message
     */
    SqsMessage getMessage();
}
