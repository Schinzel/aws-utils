package io.schinzel.basicutils.s3handler;

import com.amazonaws.services.s3.transfer.TransferManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Purpose of this class is to check if a bucket exists, and cached the buckets that did exist.
 * <p>
 * Created by Schinzel on 2018-01-03
 */
class BucketCache {
    /**
     * A cache for existing buckets on S3. Is necessary as the doesBucketExist
     * method takes a surprisingly long time (Schinzel 2016).
     */
    private static final List<String> EXISTING_BUCKETS_CACHE = new ArrayList<>();


    static boolean doesBucketExist(TransferManager transferManager, String bucketName) {
        //If buckets cache contains the argument bucket name
        if (EXISTING_BUCKETS_CACHE.contains(bucketName)) {
            return true;
        }
        //Set if bucket exists on S3 or not
        boolean bucketExistsOnS3 = transferManager
                .getAmazonS3Client()
                .doesBucketExistV2(bucketName);
        //If bucket did exist on S3
        if (bucketExistsOnS3) {
            //Add it to cache
            EXISTING_BUCKETS_CACHE.add(bucketName);
        }
        return bucketExistsOnS3;
    }
}
