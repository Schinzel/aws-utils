package io.schinzel.awsutils.s3file;


import io.schinzel.basicutils.UTF8;
import io.schinzel.basicutils.file.Bytes;
import io.schinzel.basicutils.file.FileReader;
import io.schinzel.basicutils.thrower.Thrower;
import lombok.Builder;
import lombok.experimental.Accessors;
import software.amazon.awssdk.core.async.AsyncRequestBody;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.transfer.s3.S3TransferManager;
import software.amazon.awssdk.transfer.s3.model.DownloadFileRequest;
import software.amazon.awssdk.transfer.s3.model.FileDownload;
import software.amazon.awssdk.transfer.s3.model.Upload;
import software.amazon.awssdk.transfer.s3.model.UploadRequest;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.CompletionException;

/**
 * The purpose of this class is to offer operations on S3 files.
 *
 * @author Schinzel
 */
@Accessors(prefix = "m")
public class S3File implements IS3File {
    /**
     * The name of this file
     */
    private final String mFileName;
    /**
     * The name of the bucket in which this file resides
     */
    private final String mBucketName;
    /**
     * Transfers data to/from S3
     */
    private final S3TransferManager mTransferManager;
    /**
     * S3 client for non-transfer operations
     */
    private final S3Client mS3Client;
    /**
     * If true, write method does the write operation in the in background.
     */
    private final boolean mBackgroundWrite;


    @Builder
    S3File(String awsAccessKey, String awsSecretKey, Region region, String bucketName, String fileName, boolean backgroundWrite) {
        Thrower.createInstance()
                .throwIfVarEmpty(awsAccessKey, "awsAccessKey")
                .throwIfVarEmpty(awsSecretKey, "awsSecretKey")
                .throwIfVarNull(region, "region")
                .throwIfVarEmpty(bucketName, "bucketName")
                .throwIfVarEmpty(fileName, "fileName");
        mFileName = fileName;
        mBucketName = bucketName;
        mBackgroundWrite = backgroundWrite;
        mTransferManager = TransferManagers.getInstance()
                .getTransferManager(awsAccessKey, awsSecretKey, region);
        // Get cached S3Client for non-transfer operations
        mS3Client = S3ClientCache.getSingleton()
                .getS3Client(awsAccessKey, awsSecretKey, region);
        boolean bucketExists = BucketCache.doesBucketExist(mS3Client, bucketName);
        Thrower.throwIfFalse(bucketExists).message("No bucket named '" + bucketName + "' exists");
    }


    /**
     * @return The content of this file as a string. If there was no such file, an empty string is returned.
     */
    @Override
    public Bytes read() {
        try {
            File tempFile = S3File.getTempFile();
            this.downloadFileContentIntoTempFile(tempFile);
            //Read content in temp file and return it
            return FileReader.read(tempFile);
        } catch (Exception e) {
            String exceptionMessage = String.format("Problems when reading S3 file '%s' from bucket '%s'. ", mFileName, mBucketName);
            throw new RuntimeException(exceptionMessage + e.getMessage());
        }
    }


    private static File getTempFile() throws IOException {
        String downloadFileNamePrefix = "s3_destination_temp_file_";
        //Creates a file with the suffix .tmp
        File downloadFile = File.createTempFile(downloadFileNamePrefix, null);
        //File will be deleted on exit of virtual machine
        downloadFile.deleteOnExit();
        return downloadFile;
    }


    private void downloadFileContentIntoTempFile(File tempFile) throws IOException {
        try {
            DownloadFileRequest downloadFileRequest = DownloadFileRequest.builder()
                    .getObjectRequest(GetObjectRequest.builder()
                            .bucket(mBucketName)
                            .key(mFileName)
                            .build())
                    .destination(tempFile.toPath())
                    .build();
            FileDownload download = mTransferManager.downloadFile(downloadFileRequest);
            download.completionFuture().join();
        } catch (CompletionException e) {
            if (e.getCause() instanceof NoSuchKeyException) {
                //If there was no such file
                //Create empty file
                tempFile.createNewFile();
            } else {
                throw e;
            }
        }
    }


    /**
     * @return True if this file exists, else false.
     */
    @Override
    public boolean exists() {
        try {
            mS3Client.headObject(HeadObjectRequest.builder()
                    .bucket(mBucketName)
                    .key(mFileName)
                    .build());
        } catch (NoSuchKeyException e) {
            return false;
        }
        return true;
    }


    /**
     * Delete this file. If file does not exist on S3, method returns gracefully without throwing errors.
     */
    @Override
    public IS3File delete() {
        if (this.exists()) {
            mS3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(mBucketName)
                    .key(mFileName)
                    .build());
        }
        return this;
    }


    /**
     * See [write(String fileContent)]
     *
     * @param fileContent The file content to write
     */
    @Override
    public IS3File write(String fileContent) {
        byte[] contentAsBytes = UTF8.getBytes(fileContent);
        return this.write(contentAsBytes);
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
    public IS3File write(byte[] fileContent) {
        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(mBucketName)
                    .key(mFileName)
                    .contentType(HttpFileHeaders.getFileHeader(mFileName))
                    .cacheControl("public, max-age=2592000")
                    .contentLength((long) fileContent.length)
                    .build();

            Upload upload = mTransferManager.upload(UploadRequest.builder()
                    .putObjectRequest(putObjectRequest)
                    .requestBody(AsyncRequestBody.fromBytes(fileContent))
                    .build());

            if (!mBackgroundWrite) {
                upload.completionFuture().join();
            }
            return this;
        } catch (SdkClientException ex) {
            throw new RuntimeException("Problems uploading to S3! " + ex.getMessage());
        }
    }


}
