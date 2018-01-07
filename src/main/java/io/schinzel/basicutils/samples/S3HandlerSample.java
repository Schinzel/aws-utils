package io.schinzel.basicutils.samples;

import io.schinzel.basicutils.RandomUtil;
import io.schinzel.basicutils.configvar.ConfigVar;
import io.schinzel.basicutils.s3handler.S3File;
import io.schinzel.basicutils.str.Str;

/**
 * Purpose of this class is ...
 * <p>
 * Created by Schinzel on 2018-01-03
 */
public class S3HandlerSample {
    private static String AWS_ACCESS_KEY = ConfigVar.create(".env").getValue("AWS_ACCESS_KEY");
    private static String AWS_SECRET_KEY = ConfigVar.create(".env").getValue("AWS_SECRET_KEY");


    public static void main(String[] args) {
        usage_V1();
    }


    public static void usage_V1() {
        String bucketName = "schinzel.io";
        String fileName = "myfile.txt";
        String fileContent = "my content ";
        for (int i = 0; i < 10; i++) {
            fileContent += RandomUtil.getRandomString(500) + "\n";
        }
        long start = System.nanoTime();
        for (int i = 0; i < 10; i++) {
            S3File.builder()
                    .awsAccessKey(AWS_ACCESS_KEY)
                    .awsSecretKey(AWS_SECRET_KEY)
                    .bucketName(bucketName)
                    .fileName(i + "_" + fileName)
                    .build()
                    .upload(fileContent + "__" + i, true);
        }
        //TransferManagers.getInstance().shutdown();
        long execTime = (System.nanoTime() - start) / 1_000_000;
        System.out.println("Upload file '" + fileName + "' to bucket '" + bucketName + "' with content '" + fileContent + "'");
        Str.create("It took ").af(execTime).a(" millis").writeToSystemOut();
    }

}
