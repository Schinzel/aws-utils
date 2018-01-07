package io.schinzel.basicutils.samples;

import com.google.common.base.Strings;
import io.schinzel.basicutils.RandomUtil;
import io.schinzel.basicutils.configvar.ConfigVar;
import io.schinzel.basicutils.s3handler.S3File;
import io.schinzel.basicutils.s3handler.TransferManagers;
import io.schinzel.basicutils.str.Str;

/**
 * Purpose of this class is show sample usage of the S3File class.
 * <p>
 * Created by Schinzel on 2018-01-03
 */
public class S3HandlerSample {
    private static String AWS_ACCESS_KEY = ConfigVar.create(".env").getValue("AWS_ACCESS_KEY");
    private static String AWS_SECRET_KEY = ConfigVar.create(".env").getValue("AWS_SECRET_KEY");


    public static void main(String[] args) {
        uploadSingleFile();
        boolean parallelUploads = true;
        uploadMultipleFiles(parallelUploads);
        miscOperations();
    }


    /**
     * Upload a single file to S3.
     */
    public static void uploadSingleFile() {
        String bucketName = "schinzel.io";
        String fileName = "myfile.txt";
        String fileContent = "my content";
        //The true for "waitTillDone" argument will stop the code here until the file has been
        // fully uploaded
        S3File.builder()
                .awsAccessKey(AWS_ACCESS_KEY)
                .awsSecretKey(AWS_SECRET_KEY)
                .bucketName(bucketName)
                .fileName(fileName)
                .build()
                .upload(fileContent, true);
        //Terminates threads for file uploading.
        TransferManagers.getInstance().shutdown();
        Str.create()
                .a("Uploaded content ").aq(fileContent)
                .a(" to file ").aq(fileName)
                .a(" in bucket ").aq(bucketName)
                .writeToSystemOut();
    }


    /**
     * Uploads a set of files to S3.
     *
     * @param parallelUploads If true, files are uploaded in parallel. If false each file is
     *                        uploaded completely until the upload of the next file commences.
     */
    public static void uploadMultipleFiles(boolean parallelUploads) {
        String bucketName = "schinzel.io";
        String fileName = "myfile.txt";
        String fileContent = getFileContent();
        boolean waitTillDone = !parallelUploads;
        long start = System.nanoTime();
        for (int i = 0; i < 10; i++) {
            //The true for "waitTillDone" if false will commence the file upload but return
            //immediately to let the code continue and allow parallel uploads
            S3File.builder()
                    .awsAccessKey(AWS_ACCESS_KEY)
                    .awsSecretKey(AWS_SECRET_KEY)
                    .bucketName(bucketName)
                    .fileName(i + "_" + fileName)
                    .build()
                    .upload(fileContent + "__" + i, waitTillDone);
            Str.create("Upload: ").a(i).writeToSystemOut();
        }
        //Terminates threads for file uploading. Note that files that have not been completely
        //uploaded are interrupted.
        TransferManagers.getInstance().shutdown();
        long execTime = (System.nanoTime() - start) / 1_000_000;
        Str.create()
                .a("Uploaded content ").aq(fileContent)
                .a(" to file ").aq(fileName)
                .a(" in bucket ").aq(bucketName)
                .a(" and it took ").af(execTime).a(" millis")
                .writeToSystemOut();
    }


    /**
     * Shows usage of various methods.
     */
    public static void miscOperations() {
        String bucketName = "schinzel.io";
        String fileName = RandomUtil.getRandomString(5) + ".txt";
        String fileContent = "my content " + RandomUtil.getRandomString(5);
        S3File s3File = S3File.builder()
                .awsAccessKey(AWS_ACCESS_KEY)
                .awsSecretKey(AWS_SECRET_KEY)
                .bucketName(bucketName)
                .fileName(fileName)
                .build();
        Str.create()
                .a("Created object for file ").aq(fileName)
                .writeToSystemOut();
        Str.create()
                .a("File exists: ").a(s3File.exists())
                .writeToSystemOut();
        s3File.upload(fileContent, true);
        Str.create("Uploaded content to file").writeToSystemOut();
        Str.create()
                .a("File exists: ").a(s3File.exists())
                .writeToSystemOut();
        Str.create()
                .a("File ").aq(fileName)
                .a(" contains string: ").aq(s3File.getContentAsString())
                .writeToSystemOut();
        s3File.delete();
        Str.create("Deleted file").writeToSystemOut();
        Str.create()
                .a("File exists: ").a(s3File.exists())
                .writeToSystemOut();
        TransferManagers.getInstance().shutdown();
    }


    /**
     * @return A set of character that simulate the content of a file.
     */
    private static String getFileContent() {
        return "my content "
                + RandomUtil.getRandomString(5)
                + " "
                + Strings.repeat("*", 500);
    }

}
