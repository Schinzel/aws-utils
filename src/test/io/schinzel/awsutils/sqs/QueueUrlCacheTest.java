package io.schinzel.awsutils.sqs;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.CreateQueueRequest;
import com.google.common.collect.ImmutableMap;
import io.schinzel.basicutils.RandomUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * @author Schinzel
 */
public class QueueUrlCacheTest {
    private AmazonSQS mSqsClient;
    private String mQueueName;
    private String mQueueUrl;

    @Before
    public void before() {
        QueueUrlCache.getSingleton().mQueueUrlCache.invalidate();
        mQueueName = QueueUrlCacheTest.class.getSimpleName() + "_" + RandomUtil.getRandomString(5) + ".fifo";

        AWSCredentials credentials = new BasicAWSCredentials(PropertiesUtil.AWS_SQS_ACCESS_KEY, PropertiesUtil.AWS_SQS_SECRET_KEY);
        AWSStaticCredentialsProvider credentialsProvider = new AWSStaticCredentialsProvider(credentials);
        //Construct a new sqs client
        mSqsClient = AmazonSQSClientBuilder
                .standard()
                .withCredentials(credentialsProvider)
                .withRegion(Regions.EU_WEST_1)
                .build();

        Map<String, String> queueAttributes = ImmutableMap.<String, String>builder()
                .put("FifoQueue", "true")
                .put("ContentBasedDeduplication", "false")
                .build();
        CreateQueueRequest createFifoQueueRequest = new CreateQueueRequest(mQueueName)
                .withAttributes(queueAttributes);
        mQueueUrl = mSqsClient.createQueue(createFifoQueueRequest)
                .getQueueUrl();
    }

    @After
    public void after() {
        mSqsClient.deleteQueue(mQueueUrl);
        QueueUrlCache.getSingleton().mQueueUrlCache.invalidate();
    }


    @Test
    public void getQueueUrl_OneRequest_CacheSizeOne() {
        QueueUrlCache.getSingleton().getQueueUrl(mQueueName, mSqsClient);
        long cacheSize = QueueUrlCache.getSingleton().mQueueUrlCache.cacheSize();
        assertThat(cacheSize).isEqualTo(1);
    }

    @Test
    public void getQueueUrl_OneRequest_CacheHitsZero() {
        QueueUrlCache.getSingleton().getQueueUrl(mQueueName, mSqsClient);
        long cacheHits = QueueUrlCache.getSingleton().mQueueUrlCache.cacheHits();
        assertThat(cacheHits).isEqualTo(0);
    }


    @Test
    public void getQueueUrl_ThreeRequests_CacheSizeOne() {
        for (int i = 0; i < 3; i++) {
            QueueUrlCache.getSingleton().getQueueUrl(mQueueName, mSqsClient);
        }
        long cacheSize = QueueUrlCache.getSingleton().mQueueUrlCache.cacheSize();
        assertThat(cacheSize).isEqualTo(1);
    }


    @Test
    public void getQueueUrl_ThreeRequests_CacheHitsTtwo() {
        for (int i = 0; i < 3; i++) {
            QueueUrlCache.getSingleton().getQueueUrl(mQueueName, mSqsClient);
        }
        long cacheHits = QueueUrlCache.getSingleton().mQueueUrlCache.cacheHits();
        assertThat(cacheHits).isEqualTo(2);
    }


    @Test
    public void getQueueUrl_UrlComesFromServer_CorrectUrl() {
        String queueUrl = QueueUrlCache.getSingleton().getQueueUrl(mQueueName, mSqsClient);
        assertThat(queueUrl).isEqualTo(mQueueUrl);
    }


    @Ignore
    @Test
    public void getQueueUrl_UrlComesFromCache_CorrectUrl() {
        QueueUrlCache.getSingleton().getQueueUrl(mQueueName, mSqsClient);
        String queueUrl = QueueUrlCache.getSingleton().getQueueUrl(mQueueName, mSqsClient);
        assertThat(queueUrl).isEqualTo(mQueueUrl);
    }

}