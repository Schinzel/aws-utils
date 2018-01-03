package io.schinzel.basicutils.samples;

import io.schinzel.basicutils.configvar.ConfigVar;
import io.schinzel.basicutils.s3handler.AwsAccount;
import io.schinzel.basicutils.s3handler.S3File;

/**
 * Purpose of this class is ...
 * <p>
 * Created by Schinzel on 2018-01-03
 */
public class S3HandlerSample {
    static String AWS_ACCESS_KEY = ConfigVar.create(".env").getValue("AWS_ACCESS_KEY");
    static String AWS_SECRET_KEY = ConfigVar.create(".env").getValue("AWS_SECRET_KEY");


    public static void main(String[] args) {
        usage_V1();
        usage_V2();
    }


    public static void usage_V1() {
        S3File s3File = S3File.builder()
                .awsAccessKey(AWS_ACCESS_KEY)
                .awsSecretKey(AWS_SECRET_KEY)
                .bucketName("io.schinzel")
                .fileName("myfile1.txt")
                .build();
        s3File.upload("my content", false);
    }


    public static void usage_V2() {
        new AwsAccount(AWS_ACCESS_KEY, AWS_SECRET_KEY)
                .getBucket("io.schinzel")
                .getS3File("myfile2.txt")
                .upload("my content", false);

    }
}
