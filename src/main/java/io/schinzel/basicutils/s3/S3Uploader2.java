package io.schinzel.basicutils.s3;

import lombok.Builder;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * Purpose of this class is ...
 * <p>
 * Created by Schinzel on 2018-06-23
 */
@Accessors(prefix = "m", fluent = true, chain = true)
public class S3Uploader2 {
    @Setter
    String mContent;
    @Setter
    boolean mBackgroundUpload;


    @Builder
    S3Uploader2(String awsAccessKey, String awsSecretKey, String bucketName, String fileName) {

    }


    public S3Uploader2 upload() {
        return this;
    }


    public static void main(String[] args) {
        S3Uploader2 s3Uploader2 = S3Uploader2.builder()
                .awsAccessKey("")
                .awsSecretKey("")
                .bucketName("")
                .fileName("")
                .build();

        s3Uploader2
                .backgroundUpload(false)
                .content("")
                .upload();

    }

}
