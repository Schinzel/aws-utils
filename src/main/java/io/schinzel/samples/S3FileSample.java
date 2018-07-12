package io.schinzel.samples;

import com.amazonaws.regions.Regions;
import com.google.common.base.Strings;
import io.schinzel.basicutils.RandomUtil;
import io.schinzel.basicutils.configvar.ConfigVar;
import io.schinzel.awsutils.s3file.S3File;
import io.schinzel.awsutils.s3file.TransferManagers;
import io.schinzel.basicutils.str.Str;

/**
 * Purpose of this class is show sample usage of the S3File class.
 * <p>
 * Created by Schinzel on 2018-01-03
 */
public class S3FileSample {
    private static String AWS_S3_ACCESS_KEY = ConfigVar.create(".env").getValue("AWS_S3_ACCESS_KEY");
    private static String AWS_S3_SECRET_KEY = ConfigVar.create(".env").getValue("AWS_S3_SECRET_KEY");


    public static void main(String[] args) {
        //Single file write sample
        uploadSingleFile();
        //Multiple files write samples
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
        S3File file = S3File.builder()
                .awsAccessKey(AWS_S3_ACCESS_KEY)
                .awsSecretKey(AWS_S3_SECRET_KEY)
                .region(Regions.EU_WEST_1)
                .bucketName(bucketName)
                .fileName(fileName)
                .build()
                //Upload content
                .write(fileContent);
        //Sample clean up, delete file
        file.delete();
        //Terminates threads for file uploading.
        TransferManagers.getInstance().shutdown();
        Str.create()
                .a("Uploaded content ").aq(fileContent)
                .a(" to file ").aq(fileName)
                .a(" in bucket ").aq(bucketName)
                .writeToSystemOut();
    }


    /**
     * Uploads a set of files to S3. As backgroundUpload is used the uploads are done in parallel.
     */
    private static void uploadMultipleFiles() {
        String bucketName = "schinzel.io";
        String fileName = "myfile.txt";
        String fileContent = getFileContent();
        long start = System.nanoTime();
        for (int i = 0; i < 10; i++) {
            S3File.builder()
                    .awsAccessKey(AWS_S3_ACCESS_KEY)
                    .awsSecretKey(AWS_S3_SECRET_KEY)
                    .region(Regions.EU_WEST_1)
                    .bucketName(bucketName)
                    .backgroundWrite(true)
                    .fileName(i + "_" + fileName)
                    .build()
                    .write(fileContent + "__" + i);
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
        String fileContent = getFileContent();
        S3File s3File = S3File.builder()
                .awsAccessKey(AWS_S3_ACCESS_KEY)
                .awsSecretKey(AWS_S3_SECRET_KEY)
                .region(Regions.EU_WEST_1)
                .bucketName(bucketName)
                .fileName(fileName)
                .build()
                //Upload data to file on S3
                .write(fileContent);
        //If file exists
        if (s3File.exists()) {
            //Read file
            s3File.read()
                    .asStr()
                    .writeToSystemOut();
        }
        //Sample clean up, delete file
        s3File.delete();
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
