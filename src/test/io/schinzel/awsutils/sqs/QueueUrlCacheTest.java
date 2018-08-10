package io.schinzel.awsutils.sqs;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.CreateQueueRequest;
import com.google.common.collect.ImmutableMap;
import io.schinzel.basicutils.RandomUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * @author Schinzel
 */
public class QueueUrlCacheTest {
    private AmazonSQS mSqsClient;
    private String mQueueName;
    private String mFifoQueueUrl;

    @Before
    public void before() {
        mQueueName = QueueUrlCacheTest.class.getSimpleName() + "_" + RandomUtil.getRandomString(5) + ".fifo";
        mSqsClient = ClientCache.getSingleton()
                .getSqsClient(PropertiesUtil.AWS_SQS_ACCESS_KEY, PropertiesUtil.AWS_SQS_SECRET_KEY, Regions.EU_WEST_1);
        QueueUrlCache.getSingleton().mQueueUrlCache.invalidate();
        Map<String, String> queueAttributes = ImmutableMap.<String, String>builder()
                .put("FifoQueue", "true")
                .put("ContentBasedDeduplication", "false")
                .build();
        CreateQueueRequest createFifoQueueRequest = new CreateQueueRequest(mQueueName)
                .withAttributes(queueAttributes);
        mFifoQueueUrl = mSqsClient.createQueue(createFifoQueueRequest)
                .getQueueUrl();
    }

    @After
    public void after() {
        mSqsClient.deleteQueue(mFifoQueueUrl);
    }

    @Test
    public void getQueueUrl_OneRequest_CacheSizeOne() {
        String apa = "apa";
        assertThat(apa).isEqualTo("apa");
    }


    public void getQueueUrl_OneRequest_CacheHitsZero() {
    }


}