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
public class ClientCacheTest {

    @Before
    public void before() {
        ClientCache.getSingleton().mSqsClientCache.invalidate();
    }

    @After
    public void after() {
        ClientCache.getSingleton().mSqsClientCache.invalidate();
    }


    @Test
    public void getSingleton_CalledTwice_SameObject() {
        ClientCache clientCache1 = ClientCache.getSingleton();
        ClientCache clientCache2 = ClientCache.getSingleton();
        assertThat(clientCache1).isEqualTo(clientCache2);
    }


    @Test
    public void getSqsClient_SameClientRequestedThreeTimes_CacheHitTwo() {
        for (int i = 0; i < 3; i++) {
            ClientCache.getSingleton()
                    .getSqsClient(PropertiesUtil.AWS_SQS_ACCESS_KEY, PropertiesUtil.AWS_SQS_SECRET_KEY, Region.EU_WEST_1);
        }
        assertThat(ClientCache.getSingleton().mSqsClientCache.cacheHits()).isEqualTo(2);
    }


    @Test
    public void getSqsClient_SameClientRequestedTwice_SameObject() {
        SqsClient amazonSQS1 = ClientCache.getSingleton()
                .getSqsClient(PropertiesUtil.AWS_SQS_ACCESS_KEY, PropertiesUtil.AWS_SQS_SECRET_KEY, Region.EU_WEST_1);
        SqsClient amazonSQS2 = ClientCache.getSingleton()
                .getSqsClient(PropertiesUtil.AWS_SQS_ACCESS_KEY, PropertiesUtil.AWS_SQS_SECRET_KEY, Region.EU_WEST_1);
        assertThat(amazonSQS1).isEqualTo(amazonSQS2);
    }


    @Test
    public void getSqsClient_SameClientRequestedThreeTime_CacheSizeOne() {
        for (int i = 0; i < 3; i++) {
            ClientCache.getSingleton()
                    .getSqsClient(PropertiesUtil.AWS_SQS_ACCESS_KEY, PropertiesUtil.AWS_SQS_SECRET_KEY, Region.EU_WEST_1);
        }
        assertThat(ClientCache.getSingleton().mSqsClientCache.cacheSize()).isEqualTo(1);
    }


    @Test
    public void getSqsClient_IncorrectCredentials_NoException() {
        assertThatCode(() ->
                ClientCache.getSingleton()
                        .getSqsClient(PropertiesUtil.AWS_SQS_ACCESS_KEY, "Apa", Region.EU_WEST_1)
        ).doesNotThrowAnyException();
    }


    @Test
    public void getSqsClient_TwoRequestDifferentRegions_CacheSizeTwo() {
        ClientCache.getSingleton()
                .getSqsClient(PropertiesUtil.AWS_SQS_ACCESS_KEY, PropertiesUtil.AWS_SQS_SECRET_KEY, Region.EU_WEST_1);
        ClientCache.getSingleton()
                .getSqsClient(PropertiesUtil.AWS_SQS_ACCESS_KEY, PropertiesUtil.AWS_SQS_SECRET_KEY, Region.US_EAST_1);
        assertThat(ClientCache.getSingleton().mSqsClientCache.cacheSize()).isEqualTo(2);
    }


    @Test
    public void getSqsClient_EmptyAccessKey_Exception() {
        assertThatExceptionOfType(RuntimeException.class)
                .isThrownBy(() ->
                        ClientCache.getSingleton()
                                .getSqsClient("", PropertiesUtil.AWS_SQS_SECRET_KEY, null)
                );
    }

    @Test
    public void getSqsClient_EmptySecretKey_Exception() {
        assertThatExceptionOfType(RuntimeException.class)
                .isThrownBy(() ->
                        ClientCache.getSingleton()
                                .getSqsClient(PropertiesUtil.AWS_SQS_ACCESS_KEY, "", null)
                );
    }


    @Test
    public void getSqsClient_NullRegion_Exception() {
        assertThatExceptionOfType(RuntimeException.class)
                .isThrownBy(() ->
                        ClientCache.getSingleton()
                                .getSqsClient(PropertiesUtil.AWS_SQS_ACCESS_KEY, PropertiesUtil.AWS_SQS_SECRET_KEY, null)
                );
    }

}