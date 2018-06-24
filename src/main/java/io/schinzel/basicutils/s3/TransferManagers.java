package io.schinzel.basicutils.s3;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.TransferManagerBuilder;
import io.schinzel.basicutils.collections.Cache;
import lombok.experimental.Accessors;
import lombok.val;

/**
 * The purpose of this class is to create and cache AWS S3 transfer managers.
 * One transfer manager per access key is created and cached.
 *
 * @author Schinzel
 */
@Accessors(prefix = "m")
public class TransferManagers {
    /** Holds a collection of transfer managers. Key is AWS access key and region. */
    final Cache<String, TransferManager> mTransferManagers = new Cache<>();


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
    TransferManager getTransferManager(String awsAccessKey, String awsSecretKey, Regions region) {
        String cache_key = awsAccessKey + "_" + region.getName();
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
                .forEach(k -> mTransferManagers.get(k).shutdownNow(true));
        mTransferManagers.invalidate();
        return this;
    }


    /**
     * @param awsAccessKey The AWS access key
     * @param awsSecretKey The AWS secret key
     * @return An newly created AWS S3 transfer manager
     */
    private static TransferManager createTransferManager(String awsAccessKey, String awsSecretKey, Regions region) {
        val basicAWSCredentials = new BasicAWSCredentials(awsAccessKey, awsSecretKey);
        val awsStaticCredentialsProvider = new AWSStaticCredentialsProvider(basicAWSCredentials);
        AmazonS3 s3client = AmazonS3ClientBuilder
                .standard()
                .withCredentials(awsStaticCredentialsProvider)
                .withRegion(region)
                .build();
        return TransferManagerBuilder
                .standard()
                .withS3Client(s3client)
                .build();
    }


}

