package io.schinzel.basicutils.s3handler;

import com.amazonaws.services.s3.transfer.TransferManager;
import lombok.experimental.Accessors;

/**
 * Purpose of this class is ...
 * <p>
 * Created by Schinzel on 2018-01-03
 */
@Accessors(prefix = "m")
public class AwsAccount {
    private final String mAwsAccessKey;
    private final String mAwsSecretKey;


    public AwsAccount(String awsAccessKey, String awsSecretKey) {
        mAwsAccessKey = awsAccessKey;
        mAwsSecretKey = awsSecretKey;
    }


    TransferManager getTransferManager() {
        return TransferManagers.getInstance()
                .getTransferManager(mAwsAccessKey, mAwsSecretKey);
    }


    public Bucket getBucket(String bucketName) {
        return new Bucket(bucketName, this.getTransferManager());
    }


}