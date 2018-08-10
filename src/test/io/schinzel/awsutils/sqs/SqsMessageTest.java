package io.schinzel.awsutils.sqs;

import io.schinzel.basicutils.RandomUtil;
import org.junit.After;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.assertj.core.api.Assertions.*;

/**
 * @author Schinzel
 */
public class SqsMessageTest {
    private final QueueUtil mQueue = new QueueUtil(SqsSenderTest.class);

    @After
    public void after() {
        //Delete queue used in test
        mQueue.deleteQueue();
    }


    @Test
    public void deleteMessageFromQueue_Add5MessagesDelete1_ShouldBe4Messages() {
        mQueue
                .send(RandomUtil.getRandomString(3))
                .send(RandomUtil.getRandomString(3))
                .send(RandomUtil.getRandomString(3))
                .send(RandomUtil.getRandomString(3))
                .send(RandomUtil.getRandomString(3));
        mQueue.read().deleteMessageFromQueue();
        assertThat(mQueue.getNumberOfMessages()).isEqualTo(4);
    }


    @Test
    public void getBody_CreateMessageWithRandomString_SameRandomString() {
        String messageToWrite = RandomUtil.getRandomString(10);
        String messageRead = mQueue.send(messageToWrite).read().getBody();
        assertThat(messageRead).isEqualTo(messageToWrite);
    }
}