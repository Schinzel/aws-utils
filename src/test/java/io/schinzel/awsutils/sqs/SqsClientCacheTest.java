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
        SqsSqsClientCache.getSingleton().mSqsClientCache.invalidate();
    }

    @After
    public void after() {
        SqsSqsClientCache.getSingleton().mSqsClientCache.invalidate();
    }


    @Test
    public void getSingleton_CalledTwice_SameObject() {
        SqsClientCache clientCache1 = SqsSqsClientCache.getSingleton();
        SqsClientCache clientCache2 = SqsSqsClientCache.getSingleton();
        assertThat(clientCache1).isEqualTo(clientCache2);
    }


    @Test
    public void getSqsClient_SameClientRequestedThreeTimes_CacheHitTwo() {
        for (int i = 0; i < 3; i++) {
            SqsClientCache.getSingleton()
                    .getSqsClient(PropertiesUtil.AWS_SQS_ACCESS_KEY, PropertiesUtil.AWS_SQS_SECRET_KEY, Region.EU_WEST_1);
        }
        assertThat(SqsClientCache.getSingleton().mSqsClientCache.cacheHits()).isEqualTo(2);
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
        assertThat(SqsClientCache.getSingleton().mSqsClientCache.cacheSize()).isEqualTo(1);
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
        assertThat(SqsClientCache.getSingleton().mSqsClientCache.cacheSize()).isEqualTo(2);
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

}