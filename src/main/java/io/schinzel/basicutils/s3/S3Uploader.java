package io.schinzel.basicutils.s3;

import lombok.Builder;
import lombok.experimental.Accessors;

/**
 * Purpose of this class is ...
 * <p>
 * Created by Schinzel on 2018-06-23
 */
@Accessors(prefix = "m")
public class S3Uploader {


    @Builder(buildMethodName = "upload")
    S3Uploader(String awsAccessKey, String awsSecretKey, String bucketName, boolean backgroundUpload, String content, String fileName) {

    }

    public static void main(String[] args) {
        S3Uploader.builder()
                .awsAccessKey("s")
                .awsSecretKey("d")
                .backgroundUpload(false)
                .bucketName("bucket")
                .content("")
                .fileName("myfile.txt")
                .upload();
    }



}
