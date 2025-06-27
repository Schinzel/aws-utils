package io.schinzel.awsutils.s3file;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.transfer.s3.S3TransferManager;
import io.schinzel.basicutils.collections.Cache;
import lombok.experimental.Accessors;

/**
 * The purpose of this class is to create and cache AWS S3 transfer managers.
 * One transfer manager per access key is created and cached.
 *
 * @author Schinzel
 */
@Accessors(prefix = "m")
public class TransferManagers {
    /** Holds a collection of transfer managers. Key is AWS access key and region. */
    final Cache<String, S3TransferManager> mTransferManagers = new Cache<>();


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
        return mTransferManagers.has(cache_key)
                ? mTransferManagers.get(cache_key)
                : mTransferManagers.putAndGet(cache_key, createTransferManager(awsAccessKey, awsSecretKey, region));

    }


    /**
     * Shuts down all transfer managers. Note, this will immediately stop all ongoing file transfers.
     *
     * @return This for chaining
     */
    public TransferManagers shutdown() {
        mTransferManagers.getKeys().stream()
                .forEach(k -> mTransferManagers.get(k).close());
        mTransferManagers.invalidate();
        return this;
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

