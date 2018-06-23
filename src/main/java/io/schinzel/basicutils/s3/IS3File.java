package io.schinzel.basicutils.s3;

import io.schinzel.basicutils.file.Bytes;

/**
 * Purpose of this class is
 * <p>
 * Created by Schinzel on 2018-06-23
 */
public interface IS3File {
    Bytes read();

    boolean exists();

    S3File delete();

    S3File waitTillUploadDone();

    S3File upload(String fileContent);
}
