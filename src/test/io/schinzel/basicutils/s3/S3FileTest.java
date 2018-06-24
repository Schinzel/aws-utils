package io.schinzel.basicutils.s3;

import io.schinzel.basicutils.FunnyChars;
import org.junit.Test;

import java.util.Arrays;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;


public class S3FileTest {


    @Test
    public void exists_NonExistingFile_False() {
        boolean exists = S3FileUtil
                .getS3File()
                .exists();
        assertThat(exists).isFalse();
    }


    @Test
    public void exists_ExistingFile_True() {
        S3File s3file = S3FileUtil
                .getS3File()
                .write("some content");
        boolean exists = s3file.exists();
        s3file.delete();
        assertThat(exists).isTrue();
    }


    @Test
    public void delete_ExistingFile_FileShouldNotExist() {
        boolean exists = S3FileUtil.getS3File()
                .write("some content")
                .delete()
                .exists();
        assertThat(exists).isFalse();
    }


    @Test
    public void delete_NonExistingFile_MethodShouldReturnGracefully() {
        assertThatCode(() ->
                S3FileUtil.getS3File().delete()
        ).doesNotThrowAnyException();
    }


    @Test
    public void getContentAsString_FunnyCharsUploaded_DownloadedCharsShouldBeSameAsUploaded() {
        String fileContentToUpload = Arrays
                .stream(FunnyChars.values())
                .map(FunnyChars::getString)
                .collect(Collectors.joining("\n"));
        S3File s3file = S3FileUtil
                .getS3File()
                .write(fileContentToUpload);
        String downloadedFileContent = s3file.read().asString();
        assertThat(downloadedFileContent).isEqualTo(fileContentToUpload);
        s3file.delete();
    }


    @Test
    public void getContentAsString_NonExistingFile_EmptyString() {
        String downloadedFileContent = S3FileUtil
                .getS3File()
                .read()
                .asString();
        assertThat(downloadedFileContent).isEqualTo("");
    }

}