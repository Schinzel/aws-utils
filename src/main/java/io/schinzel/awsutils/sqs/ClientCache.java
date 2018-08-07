package io.schinzel.awsutils.sqs;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import io.schinzel.basicutils.collections.Cache;

/**
 * The purpose of this class to hold a cache of clients.
 * <p>
 * The purpose of a client cache is for performance.
 * <p>
 * 2018-08-07 With this cache it takes 15 ms to send a message, i.e. to run all the code in the constructor
 * in the SqsSender.
 * Without this cache - and all other code the same - the average send takes 48 ms. Message size 250 chars.
 * Running the code on a EC2 instance. Caches had data when performance was measured.
 *
 * @author Schinzel
 */
class ClientCache {
    private static class Holder {
        public static ClientCache INSTANCE = new ClientCache();
    }

    public static ClientCache getSingleton() {
        return Holder.INSTANCE;
    }

    /** Cache of SQS clients */
    private Cache<String, AmazonSQS> mSqsClientCache = new Cache<>();


    /**
     * @param awsAccessKey A AWS access key
     * @param awsSecretKey A AWS secret key
     * @param region       The region in which to
     * @return An Amazon SQS client.
     */
    AmazonSQS getSqsClient(String awsAccessKey, String awsSecretKey, Regions region) {
        //Construct a cache key
        String cacheKey = awsAccessKey + region.getName();
        //If the cache has an entry for the cache key
        if (mSqsClientCache.has(cacheKey)) {
            //Get and return the caches instance
            return mSqsClientCache.get(cacheKey);
        } else {
            AWSCredentials credentials = new BasicAWSCredentials(awsAccessKey, awsSecretKey);
            AWSStaticCredentialsProvider credentialsProvider = new AWSStaticCredentialsProvider(credentials);
            //Construct a new sqs client
            AmazonSQS sqsClient = AmazonSQSClientBuilder
                    .standard()
                    .withCredentials(credentialsProvider)
                    .withRegion(region)
                    .build();
            //Add client to cache
            mSqsClientCache.put(cacheKey, sqsClient);
            return sqsClient;
        }
    }
}
