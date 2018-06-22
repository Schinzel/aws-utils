package io.schinzel.basicutils.s3handler;

import org.junit.Test;

import static org.assertj.core.api.Assertions.*;


/**
 * Created by Schinzel on 2018-01-03
 */
public class FileHeadersTest {

    @Test
    public void getFileHeader_Null_Exception() {
        assertThatExceptionOfType(RuntimeException.class).isThrownBy(() ->
                HttpFileHeaders.getFileHeader(null)
        );
    }


    @Test
    public void getFileHeader_EmptyString_Exception() {
        assertThatExceptionOfType(RuntimeException.class).isThrownBy(() ->
                HttpFileHeaders.getFileHeader("")
        );
    }


    @Test
    public void getFileHeader_FileWithoutExtension_Exception() {
        assertThatExceptionOfType(RuntimeException.class).isThrownBy(() ->
                HttpFileHeaders.getFileHeader("FileNameWithoutExtension")
        );
    }


    @Test
    public void getFileHeader_NonExistingExtension_Exception() {
        assertThatExceptionOfType(RuntimeException.class).isThrownBy(() ->
                HttpFileHeaders.getFileHeader("fileName.apa")
        );
    }


    @Test
    public void getFileHeader_ExistingExtension_Exception() {
        String fileHeader = HttpFileHeaders.getFileHeader("fileName.html");
        assertThat(fileHeader).isEqualTo("text/html; charset=UTF-8");
    }

}