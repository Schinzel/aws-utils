package io.schinzel.awsutils.sqs;

import software.amazon.awssdk.services.sqs.model.GetQueueAttributesRequest;
import software.amazon.awssdk.services.sqs.model.GetQueueAttributesResponse;
import software.amazon.awssdk.services.sqs.model.QueueAttributeName;
import io.schinzel.basicutils.RandomUtil;
import org.junit.After;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * @author Schinzel
 */
public class QueueUrlCacheTest {
    private final QueueUtil mQueue = new QueueUtil(QueueUrlCacheTest.class);


    @After
    public void after() {
        //Delete queue used in test
        mQueue.deleteQueue();
        //Clear cache
        QueueUrlCache.getSingleton().clearCache();
    }


    @Test
    public void getQueueUrl_OneRequest_CacheSizeOne() {
        QueueUrlCache.getSingleton().getQueueUrl(mQueue.getQueueName(), mQueue.getSqsClient());
        long cacheSize = QueueUrlCache.getSingleton().getCacheSize();
        assertThat(cacheSize).isEqualTo(1);
    }

    @Test
    public void getQueueUrl_OneRequest_CacheHitsZero() {
        // Get baseline hit count before test
        long initialHits = QueueUrlCache.getSingleton().getCacheHits();
        
        QueueUrlCache.getSingleton().getQueueUrl(mQueue.getQueueName(), mQueue.getSqsClient());
        
        // Calculate hits during this test
        long hitsAfter = QueueUrlCache.getSingleton().getCacheHits();
        long testHits = hitsAfter - initialHits;
        assertThat(testHits).isEqualTo(0);
    }


    @Test
    public void getQueueUrl_ThreeRequests_CacheSizeOne() {
        for (int i = 0; i < 3; i++) {
            QueueUrlCache.getSingleton().getQueueUrl(mQueue.getQueueName(), mQueue.getSqsClient());
        }
        long cacheSize = QueueUrlCache.getSingleton().getCacheSize();
        assertThat(cacheSize).isEqualTo(1);
    }


    @Test
    public void getQueueUrl_ThreeRequests_CacheHitsTwo() {
        // Get baseline hit count before test
        long initialHits = QueueUrlCache.getSingleton().getCacheHits();
        
        for (int i = 0; i < 3; i++) {
            QueueUrlCache.getSingleton().getQueueUrl(mQueue.getQueueName(), mQueue.getSqsClient());
        }
        
        // Calculate hits during this test
        long hitsAfter = QueueUrlCache.getSingleton().getCacheHits();
        long testHits = hitsAfter - initialHits;
        assertThat(testHits).isEqualTo(2);
    }


    @Test
    public void getQueueUrl_UrlComesFromServer_CorrectUrl() {
        String queueUrl = QueueUrlCache.getSingleton().getQueueUrl(mQueue.getQueueName(), mQueue.getSqsClient());
        assertThat(queueUrl).isEqualTo(mQueue.getQueueUrl());
    }


    @Test
    public void getQueueUrl_UrlComesFromCache_CorrectUrl() {
        QueueUrlCache.getSingleton().getQueueUrl(mQueue.getQueueName(), mQueue.getSqsClient());
        String queueUrl = QueueUrlCache.getSingleton().getQueueUrl(mQueue.getQueueName(), mQueue.getSqsClient());
        assertThat(queueUrl).isEqualTo(mQueue.getQueueUrl());
    }

    @Test
    public void createQueue_ContentBasedDeduplicationDisabled() {
        String contentBasedDeduplicationAsString = this.createQueueAndGetProperty("ContentBasedDeduplication");
        Boolean contentBasedDeduplication = Boolean.valueOf(contentBasedDeduplicationAsString);
        assertThat(contentBasedDeduplication).isFalse();
    }


    @Test
    public void createQueue_IsFifoQueue() {
        String isFifoAsString = this.createQueueAndGetProperty("FifoQueue");
        Boolean isFifo = Boolean.valueOf(isFifoAsString);
        assertThat(isFifo).isTrue();
    }


    private String createQueueAndGetProperty(String propertyKey) {
        String queueName = QueueUrlCache.class.getSimpleName() + "_" + RandomUtil.getRandomString(5) + ".fifo";
        String queueUrl = QueueUrlCache.createQueue(queueName, mQueue.getSqsClient());
        GetQueueAttributesRequest getQueueAttributesRequest = GetQueueAttributesRequest.builder()
                .queueUrl(queueUrl)
                .attributeNames(QueueAttributeName.ALL)
                .build();
        GetQueueAttributesResponse getQueueAttributesResult = mQueue.getSqsClient()
                .getQueueAttributes(getQueueAttributesRequest);
        String propertyValue = getQueueAttributesResult.attributes().get(QueueAttributeName.fromValue(propertyKey));
        mQueue.getSqsClient().deleteQueue(builder -> builder.queueUrl(queueUrl));
        return propertyValue;
    }
}