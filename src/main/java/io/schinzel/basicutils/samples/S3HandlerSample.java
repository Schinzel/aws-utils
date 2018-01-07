package io.schinzel.basicutils.samples;

import com.google.common.base.Strings;
import io.schinzel.basicutils.RandomUtil;
import io.schinzel.basicutils.configvar.ConfigVar;
import io.schinzel.basicutils.s3handler.S3File;
import io.schinzel.basicutils.s3handler.TransferManagers;
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
        uploadSingleFile();
        //uploadMultipleFiles(true);
    }


    public static void uploadSingleFile() {
        String bucketName = "schinzel.io";
        String fileName = "myfile.txt";
        String fileContent = "my content";
        S3File.builder()
                .awsAccessKey(AWS_ACCESS_KEY)
                .awsSecretKey(AWS_SECRET_KEY)
                .bucketName(bucketName)
                .fileName(fileName)
                .build()
                .upload(fileContent, true);
        TransferManagers.getInstance().shutdown();
        Str.create()
                .a("Uploaded content ").aq(fileContent)
                .a(" to file ").aq(fileName)
                .a(" in bucket ").aq(bucketName)
                .writeToSystemOut();
    }


    public static void uploadMultipleFiles(boolean parallelUploads) {
        String bucketName = "schinzel.io";
        String fileName = "myfile.txt";
        String fileContent = getFileContent();
        boolean waitTillDone = !parallelUploads;
        long start = System.nanoTime();
        for (int i = 0; i < 10; i++) {
            S3File.builder()
                    .awsAccessKey(AWS_ACCESS_KEY)
                    .awsSecretKey(AWS_SECRET_KEY)
                    .bucketName(bucketName)
                    .fileName(i + "_" + fileName)
                    .build()
                    .upload(fileContent + "__" + i, waitTillDone);
            Str.create("Upload: ").a(i).writeToSystemOut();
        }
        TransferManagers.getInstance().shutdown();
        long execTime = (System.nanoTime() - start) / 1_000_000;
        Str.create()
                .a("Uploaded content ").aq(fileContent)
                .a(" to file ").aq(fileName)
                .a(" in bucket ").aq(bucketName)
                .a(" and it took ").af(execTime).a(" millis")
                .writeToSystemOut();
    }


    private static String getFileContent() {
        return "my content "
                + RandomUtil.getRandomString(5)
                + " "
                + Strings.repeat("*", 500);
    }

}
