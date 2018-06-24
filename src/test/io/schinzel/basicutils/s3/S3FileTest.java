package io.schinzel.basicutils.s3;

import com.amazonaws.regions.Regions;
import io.schinzel.basicutils.FunnyChars;
import io.schinzel.basicutils.RandomUtil;
import io.schinzel.basicutils.configvar.ConfigVar;
import org.junit.Test;

import java.util.Arrays;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;


public class S3FileTest {
    private static final String AWS_ACCESS_KEY = ConfigVar.create(".env").getValue("AWS_ACCESS_KEY");
    private static final String AWS_SECRET_KEY = ConfigVar.create(".env").getValue("AWS_SECRET_KEY");
    private static final String BUCKET_NAME = "schinzel.io";


    private S3File getFile(String fileName) {
        return S3File.builder()
                .awsAccessKey(AWS_ACCESS_KEY)
                .awsSecretKey(AWS_SECRET_KEY)
                .region(Regions.EU_WEST_1)
                .bucketName(BUCKET_NAME)
                .fileName(fileName)
                .build();
    }


    private String getRandomFileName() {
        return RandomUtil.getRandomString(20) + ".txt";
    }


    @Test
    public void exists_NonExistingFile_False() {
        boolean exists = this.getFile("this_file_does_not_exist").exists();
        assertThat(exists).isFalse();
    }


    @Test
    public void exists_ExistingFile_True() {
        S3File s3file = this.getFile(getRandomFileName())
                .write("some content");
        boolean exists = s3file.exists();
        s3file.delete();
        assertThat(exists).isTrue();
    }


    @Test
    public void delete_ExistingFile_FileShouldNotExist() {
        boolean exists = this.getFile(getRandomFileName())
                .write("some content")
                .delete()
                .exists();
        assertThat(exists).isFalse();
    }


    @Test
    public void delete_NonExistingFile_MethodShouldReturnGracefully() {
        assertThatCode(() ->
                this.getFile("this_file_does_not_exist").delete()
        ).doesNotThrowAnyException();
    }


    @Test
    public void getContentAsString_FunnyCharsUploaded_DownloadedCharsShouldBeSameAsUploaded() {
        String fileContentToUpload = Arrays
                .stream(FunnyChars.values())
                .map(FunnyChars::getString)
                .collect(Collectors.joining("\n"));
        S3File s3file = this.getFile(getRandomFileName())
                .write(fileContentToUpload);
        String downloadedFileContent = s3file.read().asString();
        assertThat(downloadedFileContent).isEqualTo(fileContentToUpload);
        s3file.delete();
    }


    @Test
    public void getContentAsString_NonExistingFile_EmptyString() {
        String downloadedFileContent = this.getFile(getRandomFileName())
                .read()
                .asString();
        assertThat(downloadedFileContent).isEqualTo("");
    }

}