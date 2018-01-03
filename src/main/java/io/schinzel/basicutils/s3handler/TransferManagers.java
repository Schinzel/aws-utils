package io.schinzel.basicutils.s3handler;

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
class TransferManagers {
    /** Holds a collection of transfer managers. Key is AWS access key. */
    private final Cache<String, TransferManager> mTransferManagers = new Cache<>();


    private static class Holder {
        public static TransferManagers INSTANCE = new TransferManagers();
    }


    static TransferManagers getInstance() {
        return Holder.INSTANCE;
    }


    /**
     * @param awsAccessKey The AWS access key
     * @param awsSecretKey The AWS secret key
     * @return A newly created or previously cached transfer manager instance
     */
    TransferManager getTransferManager(String awsAccessKey, String awsSecretKey) {
        return mTransferManagers.has(awsAccessKey)
                ? mTransferManagers.get(awsAccessKey)
                : mTransferManagers.putAndGet(awsAccessKey, createTransferManager(awsAccessKey, awsSecretKey));
    }


    /**
     * @param awsAccessKey The AWS access key
     * @param awsSecretKey The AWS secret key
     * @return An newly created AWS S3 transfer manager
     */
    static TransferManager createTransferManager(String awsAccessKey, String awsSecretKey) {
        val basicAWSCredentials = new BasicAWSCredentials(awsAccessKey, awsSecretKey);
        val awsStaticCredentialsProvider = new AWSStaticCredentialsProvider(basicAWSCredentials);
        AmazonS3 s3client = AmazonS3ClientBuilder
                .standard()
                .withCredentials(awsStaticCredentialsProvider)
                .withRegion(Regions.EU_WEST_1)
                .build();
        //Request list of buckets to verify authentication
        s3client.listBuckets();
        return TransferManagerBuilder.standard().withS3Client(s3client).build();
    }


}

