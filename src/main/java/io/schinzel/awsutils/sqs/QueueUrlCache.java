package io.schinzel.awsutils.sqs;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.CreateQueueRequest;
import com.amazonaws.services.sqs.model.QueueDoesNotExistException;
import com.google.common.collect.ImmutableMap;
import io.schinzel.basicutils.collections.Cache;
import io.schinzel.basicutils.thrower.Thrower;

import java.util.Map;

/**
 * The purpose of this class to cache AWS SQS queue URLs.
 * <p>
 * The cache exists for performance reasons.
 * <p>
 * 2018-08-07 With this cache it takes 15 ms to send a message with SqsSender.
 * Without this cache - and all other code the same - the average send takes 25 ms. Message size 250 chars.
 * Running the code on a EC2 instance. Caches had data when performance was measured.
 *
 * @author Schinzel
 */
class QueueUrlCache {

    private static class Holder {
        static QueueUrlCache INSTANCE = new QueueUrlCache();
    }

    static QueueUrlCache getSingleton() {
        return QueueUrlCache.Holder.INSTANCE;
    }

    /** Queue URL cache */
    final Cache<String, String> mQueueUrlCache = new Cache<>();


    /**
     * If there does not exist a queue with the argument name, one is created.
     *
     * @param queueName The name of the SQS queue
     * @param sqsClient The SQS client. Is required as it is used to look up the queue URL if there is no cache hit.
     * @return The URL for the SQS queue with the argument name.
     */
    String getQueueUrl(String queueName, AmazonSQS sqsClient) {
        Thrower.createInstance()
                .throwIfVarEmpty(queueName, "queueName")
                .throwIfVarNull(sqsClient, "sqsClient");
        if (mQueueUrlCache.has(queueName)) {
            return mQueueUrlCache.get(queueName);
        } else {
            String queueUrl;
            try {
                queueUrl = sqsClient.getQueueUrl(queueName).getQueueUrl();
            } catch (QueueDoesNotExistException e) {
                queueUrl = QueueUrlCache.createQueue(queueName, sqsClient);
            }
            return mQueueUrlCache.putAndGet(queueName, queueUrl);
        }
    }


    /**
     *
     * @param queueName The name of the queue
     * @param sqsClient An AWS SQS client
     * @return The name of the newly created queue
     */
    static synchronized String createQueue(String queueName, AmazonSQS sqsClient) {
        Map<String, String> queueAttributes = ImmutableMap.<String, String>builder()
                .put("FifoQueue", "true")
                .put("ContentBasedDeduplication", "false")
                .build();
        CreateQueueRequest createFifoQueueRequest = new CreateQueueRequest(queueName)
                .withAttributes(queueAttributes);
        return sqsClient
                .createQueue(createFifoQueueRequest)
                .getQueueUrl();
    }

}
