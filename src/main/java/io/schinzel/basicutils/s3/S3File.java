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
import io.schinzel.basicutils.thrower.Thrower;
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
    /** The name of the bucket in which this file resides. */
    private final String mBucketName;
    /** Transfers data to/from S3 */
    private final TransferManager mTransferManager;
    /** If true, upload method does upload in background. */
    private final boolean mBackgroundUpload;


    @Builder
    S3File(String awsAccessKey, String awsSecretKey, Regions region, String bucketName, String fileName, boolean backgroundUpload) {
        Thrower.throwIfVarEmpty(awsAccessKey, "awsAccessKey");
        Thrower.throwIfVarEmpty(awsSecretKey, "awsSecretKey");
        Thrower.throwIfVarNull(region, "region");
        Thrower.throwIfVarEmpty(bucketName, "bucketName");
        Thrower.throwIfVarEmpty(fileName, "fileName");
        mFileName = fileName;
        mBucketName = bucketName;
        mBackgroundUpload = backgroundUpload;
        mTransferManager = TransferManagers.getInstance()
                .getTransferManager(awsAccessKey, awsSecretKey, region);
        boolean bucketExists = BucketCache.doesBucketExist(mTransferManager, bucketName);
        Thrower.throwIfFalse(bucketExists).message("No bucket named '" + bucketName + "' exists");
    }


    /**
     * @return The content of this file as a string. If there was no such file, an empty string is returned.
     */
    @Override
    public Bytes read() {
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
            mTransferManager
                    .download(mBucketName, mFileName, downloadFile)
                    .waitForCompletion();
        } catch (AmazonS3Exception as3e) {
            //If there was no such file
            if (as3e.getStatusCode() == 404) {
                return Bytes.EMPTY;
            }
        } catch (AmazonClientException | InterruptedException ex) {
            throw new RuntimeException("Problems when downloading file '" + mFileName + "' from bucket '" + mBucketName + "' " + ex.getMessage());
        }
        try {
            return FileReader.read(downloadFile);
        } catch (RuntimeException ex) {
            throw new RuntimeException("Problems when reading tmp file when downloading file '" + mFileName + "' from bucket '" + mBucketName + "'. " + ex.getMessage());
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
    @Override
    public S3File delete() {
        //Check if file exists
        try {
            mTransferManager
                    .getAmazonS3Client()
                    .getObjectMetadata(mBucketName, mFileName);
        } catch (AmazonServiceException e) {
            //Ends up here if file does not exist
            return this;
        }
        //Delete file
        mTransferManager
                .getAmazonS3Client()
                .deleteObject(mBucketName, mFileName);
        return this;
    }


    /**
     * Uploads the argument content to this S3 file. If a file already exists, it is overwritten.
     * If constructor argument backgroundUploads is set to true, the method returns after an upload is commenced but
     * not completed.
     *
     * @param fileContent The file content to upload
     */
    @Override
    public S3File upload(String fileContent) {
        byte[] contentAsBytes = UTF8.getBytes(fileContent);
        try {
            ByteArrayInputStream contentsAsStream = new ByteArrayInputStream(contentAsBytes);
            ObjectMetadata metadata = S3File.getMetaData(mFileName, contentAsBytes.length);
            PutObjectRequest putObjectRequest = new PutObjectRequest(mBucketName, mFileName, contentsAsStream, metadata);
            Upload upload = mTransferManager.upload(putObjectRequest);
            if (!mBackgroundUpload) {
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