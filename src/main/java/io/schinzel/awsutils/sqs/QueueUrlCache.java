package io.schinzel.awsutils.sqs;

import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.*;
import com.google.common.collect.ImmutableMap;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import io.schinzel.basicutils.thrower.Thrower;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * The purpose of this class to cache AWS SQS queue URLs.
 * <p>
 * The cache exists for performance reasons.
 * <p>
 * 2018-08-07 With this cache it takes 15 ms to send a message with SqsProducer.
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
    private final Cache<String, String> mQueueUrlCache = CacheBuilder.newBuilder()
            .maximumSize(200)
            .expireAfterAccess(2, TimeUnit.HOURS)
            .recordStats()
            .build();


    /**
     * If there does not exist a queue with the argument name, one is created.
     *
     * @param queueName The name of the SQS queue
     * @param sqsClient The SQS client. Is required as it is used to look up the queue URL if there is no cache hit.
     * @return The URL for the SQS queue with the argument name.
     */
    String getQueueUrl(String queueName, SqsClient sqsClient) {
        Thrower.createInstance()
                .throwIfVarEmpty(queueName, "queueName")
                .throwIfVarNull(sqsClient, "sqsClient")
                .throwIfFalse(queueName.endsWith(".fifo"), "Queue name must end in '.fifo'. Only fifo queues supported");
        //Try to get from cache first
        String cachedUrl = mQueueUrlCache.getIfPresent(queueName);
        if (cachedUrl != null) {
            return cachedUrl;
        }
        
        //Not in cache, get or create queue
        String queueUrl;
        try {
            //Get the URL from an existing queue
            GetQueueUrlResponse response = sqsClient.getQueueUrl(GetQueueUrlRequest.builder()
                    .queueName(queueName)
                    .build());
            queueUrl = response.queueUrl();
        } catch (QueueDoesNotExistException e) {
            //If there was no queue with the argument name an exception was thrown
            //Create a queue
            queueUrl = QueueUrlCache.createQueue(queueName, sqsClient);
        }
        //Add queue url to cache and return it
        mQueueUrlCache.put(queueName, queueUrl);
        return queueUrl;
    }


    /**
     *
     * @param queueName The name of the queue
     * @param sqsClient An AWS SQS client
     * @return The name of the newly created queue
     */
    static synchronized String createQueue(String queueName, SqsClient sqsClient) {
        //Compile attributes for queue
        Map<QueueAttributeName, String> queueAttributes = ImmutableMap.<QueueAttributeName, String>builder()
                .put(QueueAttributeName.FIFO_QUEUE, "true")
                .put(QueueAttributeName.CONTENT_BASED_DEDUPLICATION, "false")
                .build();
        //Create a queue request
        CreateQueueRequest createQueueRequest = CreateQueueRequest.builder()
                .queueName(queueName)
                .attributes(queueAttributes)
                .build();
        //Create queue and return the url of the newly created queue
        CreateQueueResponse response = sqsClient.createQueue(createQueueRequest);
        return response.queueUrl();
    }
    
    /**
     * Package-private method for testing purposes.
     * @return Current cache size
     */
    long getCacheSize() {
        return mQueueUrlCache.size();
    }
    
    /**
     * Package-private method for testing purposes.
     * @return Number of cache hits
     */
    long getCacheHits() {
        return mQueueUrlCache.stats().hitCount();
    }
    
    /**
     * Package-private method for testing purposes.
     * Clear the cache for testing
     */
    void clearCache() {
        mQueueUrlCache.invalidateAll();
    }

}
