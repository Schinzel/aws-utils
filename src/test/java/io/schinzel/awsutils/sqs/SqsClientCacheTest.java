package io.schinzel.awsutils.sqs;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.*;


/**
 * @author Schinzel
 */
public class SqsClientCacheTest {

    @Before
    public void before() {
        SqsClientCache.getSingleton().clearCache();
    }

    @After
    public void after() {
        SqsClientCache.getSingleton().clearCache();
    }


    @Test
    public void getSingleton_CalledTwice_SameObject() {
        SqsClientCache clientCache1 = SqsClientCache.getSingleton();
        SqsClientCache clientCache2 = SqsClientCache.getSingleton();
        assertThat(clientCache1).isEqualTo(clientCache2);
    }


    @Test
    public void getSqsClient_SameClientRequestedThreeTimes_CacheHitTwo() {
        // Get baseline hit count before test
        long initialHits = SqsClientCache.getSingleton().getCacheHits();
        
        for (int i = 0; i < 3; i++) {
            SqsClientCache.getSingleton()
                    .getSqsClient(PropertiesUtil.AWS_SQS_ACCESS_KEY, PropertiesUtil.AWS_SQS_SECRET_KEY, Region.EU_WEST_1);
        }
        
        // Calculate hits during this test
        long hitsAfter = SqsClientCache.getSingleton().getCacheHits();
        long testHits = hitsAfter - initialHits;
        assertThat(testHits).isEqualTo(2);
    }


    @Test
    public void getSqsClient_SameClientRequestedTwice_SameObject() {
        SqsClient amazonSQS1 = SqsClientCache.getSingleton()
                .getSqsClient(PropertiesUtil.AWS_SQS_ACCESS_KEY, PropertiesUtil.AWS_SQS_SECRET_KEY, Region.EU_WEST_1);
        SqsClient amazonSQS2 = SqsClientCache.getSingleton()
                .getSqsClient(PropertiesUtil.AWS_SQS_ACCESS_KEY, PropertiesUtil.AWS_SQS_SECRET_KEY, Region.EU_WEST_1);
        assertThat(amazonSQS1).isEqualTo(amazonSQS2);
    }


    @Test
    public void getSqsClient_SameClientRequestedThreeTime_CacheSizeOne() {
        for (int i = 0; i < 3; i++) {
            SqsClientCache.getSingleton()
                    .getSqsClient(PropertiesUtil.AWS_SQS_ACCESS_KEY, PropertiesUtil.AWS_SQS_SECRET_KEY, Region.EU_WEST_1);
        }
        assertThat(SqsClientCache.getSingleton().getCacheSize()).isEqualTo(1);
    }


    @Test
    public void getSqsClient_IncorrectCredentials_NoException() {
        assertThatCode(() ->
                SqsClientCache.getSingleton()
                        .getSqsClient(PropertiesUtil.AWS_SQS_ACCESS_KEY, "Apa", Region.EU_WEST_1)
        ).doesNotThrowAnyException();
    }


    @Test
    public void getSqsClient_TwoRequestDifferentRegions_CacheSizeTwo() {
        SqsClientCache.getSingleton()
                .getSqsClient(PropertiesUtil.AWS_SQS_ACCESS_KEY, PropertiesUtil.AWS_SQS_SECRET_KEY, Region.EU_WEST_1);
        SqsClientCache.getSingleton()
                .getSqsClient(PropertiesUtil.AWS_SQS_ACCESS_KEY, PropertiesUtil.AWS_SQS_SECRET_KEY, Region.US_EAST_1);
        assertThat(SqsClientCache.getSingleton().getCacheSize()).isEqualTo(2);
    }


    @Test
    public void getSqsClient_EmptyAccessKey_Exception() {
        assertThatExceptionOfType(RuntimeException.class)
                .isThrownBy(() ->
                        SqsClientCache.getSingleton()
                                .getSqsClient("", PropertiesUtil.AWS_SQS_SECRET_KEY, null)
                );
    }

    @Test
    public void getSqsClient_EmptySecretKey_Exception() {
        assertThatExceptionOfType(RuntimeException.class)
                .isThrownBy(() ->
                        SqsClientCache.getSingleton()
                                .getSqsClient(PropertiesUtil.AWS_SQS_ACCESS_KEY, "", null)
                );
    }


    @Test
    public void getSqsClient_NullRegion_Exception() {
        assertThatExceptionOfType(RuntimeException.class)
                .isThrownBy(() ->
                        SqsClientCache.getSingleton()
                                .getSqsClient(PropertiesUtil.AWS_SQS_ACCESS_KEY, PropertiesUtil.AWS_SQS_SECRET_KEY, null)
                );
    }

    @Test
    public void shutdown_AfterCreatingClients_DoesNotThrowException() {
        SqsClientCache.getSingleton()
                .getSqsClient(PropertiesUtil.AWS_SQS_ACCESS_KEY, PropertiesUtil.AWS_SQS_SECRET_KEY, Region.EU_WEST_1);
        
        assertThatCode(() -> SqsClientCache.getSingleton().shutdown())
                .doesNotThrowAnyException();
    }

    @Test
    public void shutdown_AfterShutdown_CacheIsEmpty() {
        // Create some clients first
        SqsClientCache.getSingleton()
                .getSqsClient(PropertiesUtil.AWS_SQS_ACCESS_KEY, PropertiesUtil.AWS_SQS_SECRET_KEY, Region.EU_WEST_1);
        SqsClientCache.getSingleton()
                .getSqsClient(PropertiesUtil.AWS_SQS_ACCESS_KEY, PropertiesUtil.AWS_SQS_SECRET_KEY, Region.US_EAST_1);
        
        assertThat(SqsClientCache.getSingleton().getCacheSize()).isEqualTo(2);
        
        // Shutdown
        SqsClientCache.getSingleton().shutdown();
        
        // Verify cache is cleared
        assertThat(SqsClientCache.getSingleton().getCacheSize()).isEqualTo(0);
    }

    @Test
    public void shutdown_WithNoClients_DoesNotThrowException() {
        // Call shutdown when no clients have been created
        assertThatCode(() -> SqsClientCache.getSingleton().shutdown())
                .doesNotThrowAnyException();
    }

    @Test
    public void shutdown_CalledTwice_DoesNotThrowException() {
        SqsClientCache.getSingleton()
                .getSqsClient(PropertiesUtil.AWS_SQS_ACCESS_KEY, PropertiesUtil.AWS_SQS_SECRET_KEY, Region.EU_WEST_1);
        
        // Call shutdown twice
        assertThatCode(() -> {
            SqsClientCache.getSingleton().shutdown();
            SqsClientCache.getSingleton().shutdown();
        }).doesNotThrowAnyException();
    }

    @Test
    public void getSqsClient_AfterShutdown_CreatesNewClient() {
        // Create client
        SqsClient client1 = SqsClientCache.getSingleton()
                .getSqsClient(PropertiesUtil.AWS_SQS_ACCESS_KEY, PropertiesUtil.AWS_SQS_SECRET_KEY, Region.EU_WEST_1);
        
        // Shutdown
        SqsClientCache.getSingleton().shutdown();
        
        // Create client again - should be a new instance
        SqsClient client2 = SqsClientCache.getSingleton()
                .getSqsClient(PropertiesUtil.AWS_SQS_ACCESS_KEY, PropertiesUtil.AWS_SQS_SECRET_KEY, Region.EU_WEST_1);
        
        // Should be different instances (old one was closed)
        assertThat(client1).isNotSameAs(client2);
        assertThat(SqsClientCache.getSingleton().getCacheSize()).isEqualTo(1);
    }

}