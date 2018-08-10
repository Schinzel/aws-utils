/**
 * The purpose of this package is to offer intuitive and easy to use classes for reading from and writing to
 * AWS SQS queues.
 * <p>
 * Queues used by classes in the package should have the following properties:
 * FIFO - Use FIFO queues. This to guarantee the order of the messages. This limits the number of messages to 300 send,
 * receive, or delete operations per second per queue.
 * <p>
 * Content-based Deduplication should be disabled. If content-based deduplication is enabled it instructs Amazon SQS to
 * use a SHA-256 hash to generate the message deduplication ID using the body of the message. Messages with identical
 * content sent within the deduplication interval are treated as duplicates and only one copy of the message is
 * delivered. The deduplication interval is 5 minutes at the time of writing (2018-08-09). But there could be cases when
 * it is a legitimate that messages with identical message bodies are sent within 5 minutes. And as such content-based
 * deduplication should be disabled.
 * <p>
 * The rest of the queue properties can be left default. 
 */
package io.schinzel.awsutils.sqs;