package io.schinzel.basicutils.s3;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;


public class TransferManagersTest {

    @Test
    public void shutdown_DoneAnUpload_CacheEmpty() {
        S3FileUtil.getS3File()
                .write("file content")
                .delete();
        int cacheSize = TransferManagers.getInstance()
                .shutdown()
                .mTransferManagers
                .cacheSize();
        assertThat(cacheSize).isZero();
    }
}