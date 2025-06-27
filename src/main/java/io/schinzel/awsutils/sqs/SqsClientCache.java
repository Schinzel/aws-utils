package io.schinzel.awsutils.sqs;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;
import io.schinzel.basicutils.thrower.Thrower;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

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
    private final Cache<String, SqsClient> mSqsClientCache = CacheBuilder.newBuilder()
            .maximumSize(100)
            .expireAfterAccess(1, TimeUnit.HOURS)
            .recordStats()
            .build();
    
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
        //Try to get from cache first
        SqsClient cachedClient = mSqsClientCache.getIfPresent(cacheKey);
        if (cachedClient != null) {
            return cachedClient;
        }
        
        //Create new client if not in cache
        AwsBasicCredentials credentials = AwsBasicCredentials.create(awsAccessKey, awsSecretKey);
        StaticCredentialsProvider credentialsProvider = StaticCredentialsProvider.create(credentials);
        SqsClient sqsClient = SqsClient.builder()
                .credentialsProvider(credentialsProvider)
                .region(region)
                .build();
        
        //Add client to cache and track for shutdown
        mSqsClientCache.put(cacheKey, sqsClient);
        mCreatedClients.add(sqsClient);
        return sqsClient;
    }

    /**
     * Shutdown all cached SQS clients. Call this during application shutdown.
     */
    void shutdown() {
        // Close all created SQS clients
        mCreatedClients.forEach(SqsClient::close);
        mCreatedClients.clear();
        // Invalidate the cache (this will trigger removal listeners)
        mSqsClientCache.invalidateAll();
    }
    
    /**
     * Package-private method for testing purposes.
     * @return Current cache size
     */
    long getCacheSize() {
        return mSqsClientCache.size();
    }
    
    /**
     * Package-private method for testing purposes.
     * @return Number of cache hits
     */
    long getCacheHits() {
        return mSqsClientCache.stats().hitCount();
    }
    
    /**
     * Package-private method for testing purposes.
     * Clear the cache without closing clients
     */
    void clearCache() {
        mSqsClientCache.invalidateAll();
    }
    
    /**
     * Package-private method for testing purposes.
     * Clear cache stats for test isolation
     */
    void clearStats() {
        // Unfortunately Guava doesn't provide a way to reset stats
        // We'll need to work around this in tests
    }
}
