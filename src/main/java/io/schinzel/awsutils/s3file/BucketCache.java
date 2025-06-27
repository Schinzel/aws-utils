package io.schinzel.awsutils.s3file;

import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;

import java.util.ArrayList;
import java.util.List;

/**
 * Purpose of this class is to check if a bucket exists, and cache the buckets that do exist.
 * <p>
 * The purpose of this is so that a clear error message can be thrown if a bucket does not exist.
 * The reason that the existing buckets are cached is that the checking if a bucket exists takes
 * surprisingly long time, much longer than an file write.
 * <p>
 * Below is a performance measurement done 2018-01-07 on an approximately 200 Mbit/s connection
 * which shows how slow the bucket checking operation is. The measurement is done when nothing has
 * been cached.
 * <p>
 * Name:root Tot:2,433ms Avg:2,433.82ms Hits:1
 * sublaps
 * ┗━ Name:CreateObject Root:73% Parent:73% Tot:1,765ms Avg:1,765.14ms Hits:1
 * --┗━ sublaps
 * ----┗━ Name:GetTransferManager Root:36% Parent:49% Tot:865ms Avg:865.15ms Hits:1
 * ----┗━ Name:CheckIfBucketExists Root:37% Parent:51% Tot:896ms Avg:896.29ms Hits:1
 * ┗━ Name:Upload Root:7% Parent:7% Tot:163ms Avg:163.74ms Hits:1
 * ┗━ Name:Download Root:10% Parent:10% Tot:241ms Avg:241.40ms Hits:1
 * ┗━ Name:Delete Root:5% Parent:5% Tot:123ms Avg:123.89ms Hits:1
 * <p>
 * Created by Schinzel on 2018-01-03
 */
class BucketCache {
    /** Cache for existing buckets on S3. */
    private static final List<String> EXISTING_BUCKETS_CACHE = new ArrayList<>();


    /**
     * @param bucketName      The name of a bucket
     * @return True if the argument bucket exists, else false
     */
    static boolean doesBucketExist(S3Client s3Client, String bucketName) {
        //If buckets cache contains the argument bucket name
        if (EXISTING_BUCKETS_CACHE.contains(bucketName)) {
            return true;
        }
        //Set if bucket exists on S3 or not
        boolean bucketExistsOnS3;
        try {
            s3Client.headBucket(HeadBucketRequest.builder()
                    .bucket(bucketName)
                    .build());
            bucketExistsOnS3 = true;
        } catch (NoSuchBucketException e) {
            bucketExistsOnS3 = false;
        }
        //If bucket did exist on S3
        if (bucketExistsOnS3) {
            //Add it to cache
            EXISTING_BUCKETS_CACHE.add(bucketName);
        }
        return bucketExistsOnS3;
    }
}
