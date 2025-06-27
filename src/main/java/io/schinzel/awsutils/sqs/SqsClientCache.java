package io.schinzel.awsutils.sqs;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;
import io.schinzel.basicutils.collections.Cache;
import io.schinzel.basicutils.thrower.Thrower;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The purpose of this class to hold a cache of SQS clients.
 * <p>
 * The purpose of a client cache is for performance.
 * <p>
 * 2018-08-07 With this cache it takes 15 ms to send a message with SqsProducer.
 * Without this cache - and all other code the same - the average send takes 48 ms. Message size 250 chars.
 * Running the code on a EC2 instance. Caches had data when performance was measured.
 *
 * @author Schinzel
 */
class SqsClientCache {

    private static class Holder {
        static SqsClientCache INSTANCE = new SqsClientCache();
    }

    static SqsClientCache getSingleton() {
        return Holder.INSTANCE;
    }


    /** Cache of SQS clients */
    final Cache<String, SqsClient> mSqsClientCache = new Cache<>();
    
    /** Set to track all created clients for proper shutdown */
    private final Set<SqsClient> mCreatedClients = ConcurrentHashMap.newKeySet();


    /**
     * @param awsAccessKey An AWS access key
     * @param awsSecretKey An AWS secret key
     * @param region       The region in which to
     * @return An Amazon SQS client.
     */
    SqsClient getSqsClient(String awsAccessKey, String awsSecretKey, Region region) {
        Thrower.createInstance()
                .throwIfVarEmpty(awsAccessKey, "awsAccessKey")
                .throwIfVarEmpty(awsSecretKey, "awsSecretKey")
                .throwIfVarNull(region, "region");
        //Construct a cache key
        String cacheKey = awsAccessKey + region.id();
        //If the cache has an entry for the cache key
        if (mSqsClientCache.has(cacheKey)) {
            //Get and return the cached instance
            return mSqsClientCache.get(cacheKey);
        } else {
            AwsBasicCredentials credentials = AwsBasicCredentials.create(awsAccessKey, awsSecretKey);
            StaticCredentialsProvider credentialsProvider = StaticCredentialsProvider.create(credentials);
            //Construct a new sqs client
            SqsClient sqsClient = SqsClient.builder()
                    .credentialsProvider(credentialsProvider)
                    .region(region)
                    .build();
            //Add client to cache and track for shutdown
            mSqsClientCache.put(cacheKey, sqsClient);
            mCreatedClients.add(sqsClient);
            return sqsClient;
        }
    }

    /**
     * Shutdown all cached SQS clients. Call this during application shutdown.
     */
    void shutdown() {
        // Close all created SQS clients
        mCreatedClients.forEach(SqsClient::close);
        mCreatedClients.clear();
        // Invalidate the cache
        mSqsClientCache.invalidate();
    }
}
