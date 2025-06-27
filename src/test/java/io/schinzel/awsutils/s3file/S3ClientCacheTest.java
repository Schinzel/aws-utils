package io.schinzel.awsutils.s3file;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.*;


/**
 * @author Schinzel
 */
public class S3ClientCacheTest {

    @Before
    public void before() {
        S3ClientCache.getSingleton().mS3ClientCache.invalidate();
    }

    @After
    public void after() {
        S3ClientCache.getSingleton().mS3ClientCache.invalidate();
    }


    @Test
    public void getSingleton_CalledTwice_SameObject() {
        S3ClientCache s3ClientCache1 = S3ClientCache.getSingleton();
        S3ClientCache s3ClientCache2 = S3ClientCache.getSingleton();
        assertThat(s3ClientCache1).isEqualTo(s3ClientCache2);
    }


    @Test
    public void getS3Client_SameClientRequestedThreeTimes_CacheHitTwo() {
        for (int i = 0; i < 3; i++) {
            S3ClientCache.getSingleton()
                    .getS3Client("test-access-key", "test-secret-key", Region.EU_WEST_1);
        }
        assertThat(S3ClientCache.getSingleton().mS3ClientCache.cacheHits()).isEqualTo(2);
    }


    @Test
    public void getS3Client_SameClientRequestedTwice_SameObject() {
        S3Client s3Client1 = S3ClientCache.getSingleton()
                .getS3Client("test-access-key", "test-secret-key", Region.EU_WEST_1);
        S3Client s3Client2 = S3ClientCache.getSingleton()
                .getS3Client("test-access-key", "test-secret-key", Region.EU_WEST_1);
        assertThat(s3Client1).isEqualTo(s3Client2);
    }


    @Test
    public void getS3Client_SameClientRequestedThreeTimes_CacheSizeOne() {
        for (int i = 0; i < 3; i++) {
            S3ClientCache.getSingleton()
                    .getS3Client("test-access-key", "test-secret-key", Region.EU_WEST_1);
        }
        assertThat(S3ClientCache.getSingleton().mS3ClientCache.cacheSize()).isEqualTo(1);
    }


    @Test
    public void getS3Client_TwoRequestDifferentRegions_CacheSizeTwo() {
        S3ClientCache.getSingleton()
                .getS3Client("test-access-key", "test-secret-key", Region.EU_WEST_1);
        S3ClientCache.getSingleton()
                .getS3Client("test-access-key", "test-secret-key", Region.US_EAST_1);
        assertThat(S3ClientCache.getSingleton().mS3ClientCache.cacheSize()).isEqualTo(2);
    }


    @Test
    public void getS3Client_EmptyAccessKey_Exception() {
        assertThatExceptionOfType(RuntimeException.class)
                .isThrownBy(() ->
                        S3ClientCache.getSingleton()
                                .getS3Client("", "test-secret-key", Region.EU_WEST_1)
                );
    }

    @Test
    public void getS3Client_EmptySecretKey_Exception() {
        assertThatExceptionOfType(RuntimeException.class)
                .isThrownBy(() ->
                        S3ClientCache.getSingleton()
                                .getS3Client("test-access-key", "", Region.EU_WEST_1)
                );
    }


    @Test
    public void getS3Client_NullRegion_Exception() {
        assertThatExceptionOfType(RuntimeException.class)
                .isThrownBy(() ->
                        S3ClientCache.getSingleton()
                                .getS3Client("test-access-key", "test-secret-key", null)
                );
    }

    @Test
    public void shutdown_DoesNotThrowException() {
        S3ClientCache.getSingleton()
                .getS3Client("test-access-key", "test-secret-key", Region.EU_WEST_1);
        
        assertThatCode(() -> S3ClientCache.getSingleton().shutdown())
                .doesNotThrowAnyException();
    }
}