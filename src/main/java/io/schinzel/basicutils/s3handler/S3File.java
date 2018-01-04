package io.schinzel.basicutils.s3handler;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.transfer.Download;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.Upload;
import io.schinzel.basicutils.FileRW;
import io.schinzel.basicutils.Thrower;
import io.schinzel.basicutils.UTF8;
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
public class S3File {
    /** The name of this file. */
    private final String mFileName;
    /** The name of the bucket in which this file resides. */
    private final String mBucketName;
    /** Transfers data to/from S3. */
    private final TransferManager mTransferManager;


    @Builder
    S3File(String awsAccessKey, String awsSecretKey, String bucketName, String fileName) {
        this(fileName, bucketName,
                new AwsAccount(awsAccessKey, awsSecretKey).getTransferManager());
    }


    S3File(String bucketName, String fileName, TransferManager transferManager) {
        Thrower.throwIfFalse(Bucket.doesBucketExist(transferManager, bucketName))
                .message("No bucket named '" + bucketName + "' exists");
        mFileName = fileName;
        mBucketName = bucketName;
        mTransferManager = transferManager;
    }


    /**
     * @return The content of this file as a string. If there was no such file,
     * an empty string is returned.
     */
    public String getContentAsString() {
        File downloadFile;
        try {
            String downloadFileNamePrefix = "downloadFile";
            //Creates a file with the suffix .tmp
            downloadFile = File.createTempFile(downloadFileNamePrefix, null);
            //File will be deleted on exit of virtual machine.
            downloadFile.deleteOnExit();
        } catch (IOException e) {
            throw new RuntimeException("Problems creating temporary file for uploading to S3.");
        }
        try {
            Download download = mTransferManager.download(mBucketName, mFileName, downloadFile);
            download.waitForCompletion();
        } catch (AmazonS3Exception as3e) {
            //If there was no such file
            if (as3e.getStatusCode() == 404) {
                //Return empty string
                return "";
            }
        } catch (AmazonClientException | InterruptedException ex) {
            throw new RuntimeException("Problems when downloading file '" + mFileName + "' to bucket '" + mBucketName + "' " + ex.getMessage());
        }
        try {
            //return FileUtils.readFileToString(downloadFile, Charsets.UTF_8);
            return FileRW.toString(downloadFile);
        } catch (RuntimeException ex) {
            throw new RuntimeException("Problems when reading tmp file when downloading file '" + mFileName + "' from bucket '" + mBucketName + "'. " + ex.getMessage());
        }
    }


    /**
     * @return True if this file exists, else false.
     */
    public boolean exists() {
        try {
            mTransferManager
                    .getAmazonS3Client()
                    .getObjectMetadata(mBucketName, mFileName);
        } catch (AmazonServiceException e) {
            return false;
        }
        return true;
    }


    /**
     * Delete this file. If file does not exist on S3, method returns
     * gracefully without throwing errors.
     */
    public void delete() {
        //Check if file exists
        try {
            mTransferManager
                    .getAmazonS3Client()
                    .getObjectMetadata(mBucketName, mFileName);
        } catch (AmazonServiceException e) {
            //Ends up here if file does not exist
            return;
        }
        //Delete file
        mTransferManager
                .getAmazonS3Client()
                .deleteObject(mBucketName, mFileName);
    }


    /**
     * Uploads the argument content to this S3 file. If a file already exists,
     * it is overwritten.
     *
     * @param fileContent  The file content to uploadAndGetFilename.
     * @param waitTillDone If true, method returns once the argument data has
     *                     been uploaded. If false, method returns without waiting for the uploadAndGetFilename
     *                     to finish.
     */
    public void upload(String fileContent, boolean waitTillDone) {
        byte[] contentAsBytes = UTF8.getBytes(fileContent);
        this.upload(contentAsBytes, waitTillDone);
    }


    /**
     * Uploads the argument file to this S3 file. If a file already exists,
     * it is overwritten.
     *
     * @param fileContent  The file content to uploadAndGetFilename.
     * @param waitTillDone If true, method returns once the argument data has
     *                     been uploaded. If false, method returns without waiting for the uploadAndGetFilename
     *                     to finish.
     */
    public void upload(byte[] fileContent, boolean waitTillDone) {
        try {
            ByteArrayInputStream contentsAsStream = new ByteArrayInputStream(fileContent);
            ObjectMetadata md = new ObjectMetadata();
            md.setContentLength(fileContent.length);
            md.setContentType(HttpFileHeaders.getFileHeader(mFileName));
            //Set file to be cached by browser for 30 days
            md.setCacheControl("public, max-age=2592000");
            PutObjectRequest putObjectRequest = new PutObjectRequest(mBucketName, mFileName, contentsAsStream, md);
            Upload upload = mTransferManager.upload(putObjectRequest);
            if (waitTillDone) {
                upload.waitForUploadResult();
            }
        } catch (AmazonClientException | InterruptedException ex) {
            throw new RuntimeException("Problems uploading to S3! " + ex.getMessage());
        }
    }

}
