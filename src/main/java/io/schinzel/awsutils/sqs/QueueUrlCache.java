package io.schinzel.awsutils.sqs;

import com.amazonaws.services.sqs.AmazonSQS;
import io.schinzel.basicutils.collections.Cache;
import io.schinzel.basicutils.thrower.Thrower;

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

    final Cache<String, String> mQueueUrlCache = new Cache<>();

    /**
     * @param queueName The name of the SQS queue
     * @param sqsClient The SQS client. Is required as it is used to look up the queue URL if there is no cache hit.
     * @return The URL for the SQS queue with the argument name.
     */
    String getQueueUrl(String queueName, AmazonSQS sqsClient) {
        Thrower.createInstance()
                .throwIfVarEmpty(queueName, "queueName")
                .throwIfVarNull(sqsClient, "sqsClient");
        return mQueueUrlCache.has(queueName)
                ? mQueueUrlCache.get(queueName)
                : mQueueUrlCache.putAndGet(queueName, sqsClient.getQueueUrl(queueName).getQueueUrl());
    }


}
