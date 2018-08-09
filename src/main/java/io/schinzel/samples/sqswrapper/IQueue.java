package io.schinzel.samples.sqswrapper;

/**
 * The purpose of this interface is to return the name of a AWS SQS queue.
 *
 * @author Schinzel
 */
public interface IQueue {
    String getQueueName();
}
