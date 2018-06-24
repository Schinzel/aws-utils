package io.schinzel.basicutils.s3;

import io.schinzel.basicutils.file.Bytes;

/**
 * Purpose of this class is
 * <p>
 * Created by Schinzel on 2018-06-23
 */
public interface IS3File {

    /**
     *
     * @return Returns the content of the  file
     */
    Bytes read();

    /**
     *
     * @return True if the file exists, else false
     */
    boolean exists();


    /**
     * Deletes this file.
     *
     * @return This for chaining.
     */
    S3File delete();

    /**
     * Writes the argument string to the file
     *
     * @param fileContent The content to write.
     * @return This for chaining
     */
    S3File upload(String fileContent);
}
