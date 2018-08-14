package io.schinzel.awsutils.sqs;

import org.junit.After;
import org.junit.Ignore;
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
        QueueUrlCache.getSingleton().mQueueUrlCache.invalidate();
    }


    @Test
    public void getQueueUrl_OneRequest_CacheSizeOne() {
        QueueUrlCache.getSingleton().getQueueUrl(mQueue.getQueueName(), mQueue.getSqsClient());
        long cacheSize = QueueUrlCache.getSingleton().mQueueUrlCache.cacheSize();
        assertThat(cacheSize).isEqualTo(1);
    }

    @Test
    public void getQueueUrl_OneRequest_CacheHitsZero() {
        QueueUrlCache.getSingleton().getQueueUrl(mQueue.getQueueName(), mQueue.getSqsClient());
        long cacheHits = QueueUrlCache.getSingleton().mQueueUrlCache.cacheHits();
        assertThat(cacheHits).isEqualTo(0);
    }


    @Test
    public void getQueueUrl_ThreeRequests_CacheSizeOne() {
        for (int i = 0; i < 3; i++) {
            QueueUrlCache.getSingleton().getQueueUrl(mQueue.getQueueName(), mQueue.getSqsClient());
        }
        long cacheSize = QueueUrlCache.getSingleton().mQueueUrlCache.cacheSize();
        assertThat(cacheSize).isEqualTo(1);
    }


    @Test
    public void getQueueUrl_ThreeRequests_CacheHitsTtwo() {
        for (int i = 0; i < 3; i++) {
            QueueUrlCache.getSingleton().getQueueUrl(mQueue.getQueueName(), mQueue.getSqsClient());
        }
        long cacheHits = QueueUrlCache.getSingleton().mQueueUrlCache.cacheHits();
        assertThat(cacheHits).isEqualTo(2);
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


}