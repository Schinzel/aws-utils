package io.schinzel.basicutils.s3;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.Upload;
import io.schinzel.basicutils.UTF8;
import io.schinzel.basicutils.file.Bytes;
import io.schinzel.basicutils.file.FileReader;
import io.schinzel.basicutils.str.Str;
import io.schinzel.basicutils.thrower.Thrower;
import io.schinzel.basicutils.timekeeper.Timekeeper;
import lombok.Builder;
import lombok.experimental.Accessors;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

/**
 * The purpose of this class is to offer operations on S3 files.
 *
 * @author Schinzel
 */
@Accessors(prefix = "m")
public class S3File implements IS3File {
    /** The name of this file */
    private final String mFileName;
    /** The name of the bucket in which this file resides */
    private final String mBucketName;
    /** Transfers data to/from S3 */
    private final TransferManager mTransferManager;
    /** If true, write method does the write operation in the in background. */
    private final boolean mBackgroundWrite;


    @Builder
    S3File(String awsAccessKey, String awsSecretKey, Regions region, String bucketName, String fileName, boolean backgroundWrite) {
        Thrower.throwIfVarEmpty(awsAccessKey, "awsAccessKey");
        Thrower.throwIfVarEmpty(awsSecretKey, "awsSecretKey");
        Thrower.throwIfVarNull(region, "region");
        Thrower.throwIfVarEmpty(bucketName, "bucketName");
        Thrower.throwIfVarEmpty(fileName, "fileName");
        mFileName = fileName;
        mBucketName = bucketName;
        mBackgroundWrite = backgroundWrite;
        mTransferManager = TransferManagers.getInstance()
                .getTransferManager(awsAccessKey, awsSecretKey, region);
        boolean bucketExists = BucketCache.doesBucketExist(mTransferManager, bucketName);
        Thrower.throwIfFalse(bucketExists).message("No bucket named '" + bucketName + "' exists");
    }

    public static void main(String[] args) {
        String filename = "apa";
        String bucket = "nucket";
        Timekeeper tk = Timekeeper.getSingleton();
        tk.start("Format");
        for (int i = 0; i < 10000; i++) {
            String.format("Problems when reading S3 file '%s' from bucket '%s'. ", filename, bucket);
        }
        tk.stopAndStart("Str");
        for (int i = 0; i < 10000; i++) {
            Str.create()
                    .a("Problems when reading S3 file ").aq(filename)
                    .a(" from bucket").aq(bucket)
                    .getString();
        }
        tk.stop().getResults().getStr().writeToSystemOut();
    }


    /**
     * @return The content of this file as a string. If there was no such file, an empty string is returned.
     */
    @Override
    public Bytes read() {
        String exceptionMessage = Str.create()
                .a("Problems when reading S3 file ").aq(mFileName)
                .a(" from bucket").aq(mBucketName).a(". ")
                .getString();
        File downloadFile;
        try {
            String downloadFileNamePrefix = "s3_destination_temp_file_";
            //Creates a file with the suffix .tmp
            downloadFile = File.createTempFile(downloadFileNamePrefix, null);
            //File will be deleted on exit of virtual machine
            downloadFile.deleteOnExit();
        } catch (IOException e) {
            throw new RuntimeException(exceptionMessage + "Problems creating temporary file. " + e.getMessage());
        }
        try {
            mTransferManager
                    .download(mBucketName, mFileName, downloadFile)
                    .waitForCompletion();
        } catch (AmazonS3Exception as3e) {
            //If there was no such file
            if (as3e.getStatusCode() == 404) {
                return Bytes.EMPTY;
            }
        } catch (AmazonClientException | InterruptedException e) {
            throw new RuntimeException(exceptionMessage + "Problems downloading file. " + e.getMessage());
        }
        try {
            return FileReader.read(downloadFile);
        } catch (RuntimeException e) {
            throw new RuntimeException(exceptionMessage + "Problems reading temporary file. " + e.getMessage());
        }
    }


    /**
     * @return True if this file exists, else false.
     */
    @Override
    public boolean exists() {
        try {

            mTransferManager
                    .getAmazonS3Client()
                    //If file does not exists, this throws an exception
                    .getObjectMetadata(mBucketName, mFileName);
        } catch (AmazonServiceException e) {
            return false;
        }
        return true;
    }


    /**
     * Delete this file. If file does not exist on S3, method returns gracefully without throwing errors.
     */
    @Override
    public S3File delete() {
        if (this.exists()) {
            //Delete file
            mTransferManager
                    .getAmazonS3Client()
                    .deleteObject(mBucketName, mFileName);
        }
        return this;
    }


    /**
     * Uploads the argument content to this S3 file. If a file already exists, it is overwritten.
     * If constructor argument backgroundUploads is set to true, the method returns after a write-operation is
     * commenced but not completed. If backgroundUploads is set to false or not set, this method returns after the
     * write-operation is complete.
     *
     * @param fileContent The file content to write
     */
    @Override
    public S3File write(String fileContent) {
        byte[] contentAsBytes = UTF8.getBytes(fileContent);
        try {
            ByteArrayInputStream contentsAsStream = new ByteArrayInputStream(contentAsBytes);
            ObjectMetadata metadata = S3File.getMetaData(mFileName, contentAsBytes.length);
            PutObjectRequest putObjectRequest = new PutObjectRequest(mBucketName, mFileName, contentsAsStream, metadata);
            Upload upload = mTransferManager.upload(putObjectRequest);
            if (!mBackgroundWrite) {
                upload.waitForCompletion();
            }
            return this;
        } catch (AmazonClientException | InterruptedException ex) {
            throw new RuntimeException("Problems uploading to S3! " + ex.getMessage());
        }
    }


    /**
     * @param fileName          The name of the file
     * @param fileContentLength The file content length
     * @return A file meta data object for setting meta data in uploads
     */
    private static ObjectMetadata getMetaData(String fileName, int fileContentLength) {
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(fileContentLength);
        metadata.setContentType(HttpFileHeaders.getFileHeader(fileName));
        //Set file to be cached by browser for 30 days
        metadata.setCacheControl("public, max-age=2592000");
        return metadata;
    }
}
