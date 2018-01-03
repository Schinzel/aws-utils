package io.schinzel.s3handler;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.transfer.Download;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.Upload;
import io.schinzel.basicutils.UTF8;
import lombok.Builder;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

/**
 * The purpose of this class is to offer operations on S3 files.
 *
 * @author Schinzel
 */
@Builder
public class S3File {
    /** The name of this file. */
    final String mFileName;
    /** The name of the bucket in which this file resides. */
    final String mBucketName;
    /** Transfers data to/from S3. */
    final TransferManager mTransferManager;


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
            //File will be deleted on exit of vitrual machine.
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
            return FileReader.toString(downloadFile);
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
            md.setContentType(FileHeaders.getFileHeader(mFileName));
            //Set file to be cached by browser for 30 days
            md.setCacheControl("public, max-age=2592000");
            Upload upload = mTransferManager.upload(new PutObjectRequest(mBucketName, mFileName, contentsAsStream, md));
            if (waitTillDone) {
                try {
                    upload.waitForUploadResult();
                } catch (AmazonClientException | InterruptedException ex) {
                    throw new RuntimeException("Problems when uploading file. Error message: " + ex.getMessage());
                }
            }
        } catch (AmazonClientException ex) {
            throw new RuntimeException("Problems uploading to S3! " + ex.getMessage());
        }
    }

}
