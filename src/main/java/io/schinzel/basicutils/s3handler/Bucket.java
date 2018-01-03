package io.schinzel.basicutils.s3handler;

import com.amazonaws.services.s3.transfer.TransferManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Purpose of this class is ...
 * <p>
 * Created by Schinzel on 2018-01-03
 */
public class Bucket {
    /** The name of the bucket */
    private final String mBucketName;
    /** Transfers data to/from S3. */
    private final TransferManager mTransferManager;
    /**
     * A cache for existing buckets on S3. Is necessary as the doesBucketExist
     * method takes a surprisingly long time (Schinzel 2016).
     */
    private static final List<String> EXISTING_BUCKETS_CACHE = new ArrayList<>();


    Bucket(String bucketName, TransferManager transferManager) {
        mBucketName = bucketName;
        mTransferManager = transferManager;
        //If existing buckets cache does not contain the argument bucket name
        if (!EXISTING_BUCKETS_CACHE.contains(bucketName)) {
            //Set if bucket exists on S3 or not
            boolean bucketExistsOnS3 = mTransferManager.getAmazonS3Client()
                    .doesBucketExist(bucketName);
            //If bucket did exist on S3
            if (bucketExistsOnS3) {
                //Add it to cache
                EXISTING_BUCKETS_CACHE.add(bucketName);
            }//else, i.e. bucket did not exist on S3
            else {
                //Throw an error
                throw new RuntimeException("No such bucket '" + bucketName + "'");
            }
        }
    }


    /**
     * The file does not have to exist.
     *
     * @param fileName The name of the file.
     * @return A representation of an S3 file in this bucket.
     */
    public S3File getS3File(String fileName) {
        return new S3File(fileName, mBucketName, mTransferManager);
    }
}
