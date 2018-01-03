package io.schinzel.basicutils.samples;

import io.schinzel.basicutils.s3handler.S3File;

/**
 * Purpose of this class is ...
 * <p>
 * Created by Schinzel on 2018-01-03
 */
public class S3HandlerSample {
    public static void main(String[] args) {
        String awsAccessKey = "";
        String awsSecretKey = "";
        S3File s3File = S3File.builder()
                .awsAccessKey(awsAccessKey)
                .awsSecretKey(awsSecretKey)
                .bucketName("io.schinzel")
                .fileName("myfile.txt")
                .build();
        s3File.upload("my content", false);

    }
}
