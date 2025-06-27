package io.schinzel.awsutils.s3file;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import io.schinzel.basicutils.thrower.Thrower;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * The purpose of this class to hold a cache of S3 clients.
 * <p>
 * The purpose of a client cache is for performance.
 * S3Client instances are heavyweight resources that should be reused rather than 
 * created for every S3File instance.
 *
 * @author Schinzel
 */
class S3ClientCache {

    private static class Holder {
        static S3ClientCache INSTANCE = new S3ClientCache();
    }

    static S3ClientCache getSingleton() {
        return Holder.INSTANCE;
    }


    /** Cache of S3 clients */
    private final Cache<String, S3Client> mS3ClientCache = CacheBuilder.newBuilder()
            .maximumSize(100)
            .expireAfterAccess(1, TimeUnit.HOURS)
            .recordStats()
            .build();
    
    /** Set to track all created clients for proper shutdown */
    private final Set<S3Client> mCreatedClients = ConcurrentHashMap.newKeySet();


    /**
     * @param awsAccessKey An AWS access key
     * @param awsSecretKey An AWS secret key
     * @param region       The region in which to operate
     * @return An Amazon S3 client.
     */
    S3Client getS3Client(String awsAccessKey, String awsSecretKey, Region region) {
        Thrower.createInstance()
                .throwIfVarEmpty(awsAccessKey, "awsAccessKey")
                .throwIfVarEmpty(awsSecretKey, "awsSecretKey")
                .throwIfVarNull(region, "region");
        //Construct a cache key
        String cacheKey = awsAccessKey + region.id();
        //Try to get from cache first
        S3Client cachedClient = mS3ClientCache.getIfPresent(cacheKey);
        if (cachedClient != null) {
            return cachedClient;
        }
        
        //Create new client if not in cache
        AwsBasicCredentials credentials = AwsBasicCredentials.create(awsAccessKey, awsSecretKey);
        StaticCredentialsProvider credentialsProvider = StaticCredentialsProvider.create(credentials);
        S3Client s3Client = S3Client.builder()
                .credentialsProvider(credentialsProvider)
                .region(region)
                .build();
        
        //Add client to cache and track for shutdown
        mS3ClientCache.put(cacheKey, s3Client);
        mCreatedClients.add(s3Client);
        return s3Client;
    }

    /**
     * Shutdown all cached S3 clients. Call this during application shutdown.
     */
    void shutdown() {
        // Close all created S3 clients
        mCreatedClients.forEach(S3Client::close);
        mCreatedClients.clear();
        // Invalidate the cache (this will trigger removal listeners)
        mS3ClientCache.invalidateAll();
    }
    
    /**
     * Package-private method for testing purposes.
     * @return Current cache size
     */
    long getCacheSize() {
        return mS3ClientCache.size();
    }
    
    /**
     * Package-private method for testing purposes.
     * @return Number of cache hits
     */
    long getCacheHits() {
        return mS3ClientCache.stats().hitCount();
    }
    
    /**
     * Package-private method for testing purposes.
     * Clear the cache without closing clients
     */
    void clearCache() {
        mS3ClientCache.invalidateAll();
    }
}