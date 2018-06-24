package io.schinzel.basicutils.samples;

import com.amazonaws.regions.Regions;
import com.google.common.base.Strings;
import io.schinzel.basicutils.RandomUtil;
import io.schinzel.basicutils.configvar.ConfigVar;
import io.schinzel.basicutils.s3.S3File;
import io.schinzel.basicutils.s3.TransferManagers;
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
        //Single file upload sample
        uploadSingleFile();
        //Multiple files upload samples
        uploadMultipleFiles();
        //Misc operations sample
        miscOperations();
    }


    /**
     * Upload a single file to S3.
     */
    private static void uploadSingleFile() {
        String bucketName = "schinzel.io";
        String fileName = "myfile.txt";
        String fileContent = "my content";
        //The true for "waitTillUploadDone" argument will stop the code here until the file has been
        // fully uploaded
        S3File.builder()
                .awsAccessKey(AWS_ACCESS_KEY)
                .awsSecretKey(AWS_SECRET_KEY)
                .region(Regions.EU_WEST_1)
                .bucketName(bucketName)
                .fileName(fileName)
                .build()
                .waitTillUploadDone()
                .upload(fileContent);
        //Terminates threads for file uploading.
        TransferManagers.getInstance().shutdown();
        Str.create()
                .a("Uploaded content ").aq(fileContent)
                .a(" to file ").aq(fileName)
                .a(" in bucket ").aq(bucketName)
                .writeToSystemOut();
    }


    /**
     * Uploads a set of files to S3. As waitTillUploadDone is not used the uploads are done in parallel.
     */
    private static void uploadMultipleFiles() {
        String bucketName = "schinzel.io";
        String fileName = "myfile.txt";
        String fileContent = getFileContent();
        long start = System.nanoTime();
        for (int i = 0; i < 10; i++) {
            S3File.builder()
                    .awsAccessKey(AWS_ACCESS_KEY)
                    .awsSecretKey(AWS_SECRET_KEY)
                    .region(Regions.EU_WEST_1)
                    .bucketName(bucketName)
                    .fileName(i + "_" + fileName)
                    .build()
                    .upload(fileContent + "__" + i);
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
    private static void miscOperations() {
        String bucketName = "schinzel.io";
        String fileName = RandomUtil.getRandomString(5) + ".txt";
        String fileContent = "my content " + RandomUtil.getRandomString(5);
        S3File s3File = S3File.builder()
                .awsAccessKey(AWS_ACCESS_KEY)
                .awsSecretKey(AWS_SECRET_KEY)
                .region(Regions.EU_WEST_1)
                .bucketName(bucketName)
                .fileName(fileName)
                .build();
        Str.create()
                .a("Created object for file ").aq(fileName)
                .writeToSystemOut();
        Str.create()
                .a("File exists: ").a(s3File.exists())
                .writeToSystemOut();
        //Upload data to file on S3
        s3File
                .waitTillUploadDone()
                .upload(fileContent);
        Str.create("Uploaded content to file").writeToSystemOut();
        Str.create()
                .a("File exists: ").a(s3File.exists())
                .writeToSystemOut();
        String downloadedFileContent = s3File
                .read()
                .asString();
        Str.create()
                .a("File ").aq(fileName)
                .a(" contains string: ").aq(downloadedFileContent)
                .writeToSystemOut();
        //Delete the file
        s3File.delete();
        Str.create("Deleted file").writeToSystemOut();
        Str.create()
                .a("File exists: ").a(s3File.exists())
                .writeToSystemOut();
        //Shut down all instances
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
