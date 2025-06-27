package io.schinzel.awsutils.s3file;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.transfer.s3.S3TransferManager;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.experimental.Accessors;
import java.util.concurrent.TimeUnit;

/**
 * The purpose of this class is to create and cache AWS S3 transfer managers.
 * One transfer manager per access key is created and cached.
 *
 * @author Schinzel
 */
@Accessors(prefix = "m")
public class TransferManagers {
    /** Holds a collection of transfer managers. Key is AWS access key and region. */
    private final Cache<String, S3TransferManager> mTransferManagers = CacheBuilder.newBuilder()
            .maximumSize(50)
            .expireAfterAccess(1, TimeUnit.HOURS)
            .recordStats()
            .build();


    private static class Holder {
        static TransferManagers INSTANCE = new TransferManagers();
    }


    public static TransferManagers getInstance() {
        return Holder.INSTANCE;
    }


    /**
     * @param awsAccessKey The AWS access key
     * @param awsSecretKey The AWS secret key
     * @return A newly created or previously cached transfer manager instance
     */
    S3TransferManager getTransferManager(String awsAccessKey, String awsSecretKey, Region region) {
        String cache_key = awsAccessKey + "_" + region.id();
        S3TransferManager cached = mTransferManagers.getIfPresent(cache_key);
        if (cached != null) {
            return cached;
        }
        
        // Create new transfer manager if not in cache
        S3TransferManager newTransferManager = createTransferManager(awsAccessKey, awsSecretKey, region);
        mTransferManagers.put(cache_key, newTransferManager);
        return newTransferManager;
    }


    /**
     * Shuts down all transfer managers. Note, this will immediately stop all ongoing file transfers.
     *
     * @return This for chaining
     */
    public TransferManagers shutdown() {
        // Close all cached transfer managers
        mTransferManagers.asMap().values().forEach(S3TransferManager::close);
        // Invalidate the cache (this will trigger removal listeners)
        mTransferManagers.invalidateAll();
        return this;
    }
    
    /**
     * Package-private method for testing purposes.
     * @return Current cache size
     */
    long getCacheSize() {
        return mTransferManagers.size();
    }
    
    /**
     * Package-private method for testing purposes.
     * Clear the cache for testing
     */
    void clearCache() {
        mTransferManagers.invalidateAll();
    }


    /**
     * @param awsAccessKey The AWS access key
     * @param awsSecretKey The AWS secret key
     * @return An newly created AWS S3 transfer manager
     */
    private static S3TransferManager createTransferManager(String awsAccessKey, String awsSecretKey, Region region) {
        AwsBasicCredentials awsCredentials = AwsBasicCredentials.create(awsAccessKey, awsSecretKey);
        StaticCredentialsProvider credentialsProvider = StaticCredentialsProvider.create(awsCredentials);
        S3AsyncClient s3AsyncClient = S3AsyncClient.builder()
                .credentialsProvider(credentialsProvider)
                .region(region)
                .build();
        return S3TransferManager.builder()
                .s3Client(s3AsyncClient)
                .build();
    }


}

