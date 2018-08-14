package io.schinzel.awsutils.sqs;

import com.amazonaws.regions.Regions;
import io.schinzel.basicutils.RandomUtil;
import io.schinzel.basicutils.Sandman;
import org.junit.After;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

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
        //Send 5 messages
        mQueue
                .send(RandomUtil.getRandomString(3))
                .send(RandomUtil.getRandomString(3))
                .send(RandomUtil.getRandomString(3))
                .send(RandomUtil.getRandomString(3))
                .send(RandomUtil.getRandomString(3));
        //Delete one message
        mQueue.read().deleteMessageFromQueue();
        assertThat(mQueue.getNumberOfMessages()).isEqualTo(4);
    }


    @Test
    public void deleteMessageFromQueue_SnoozeUntilAfterVisibilityTimeout_Exception() {
        //Add a message to a test queue
        mQueue.send("Any message");
        //Read a message from the test queue
        int visibilityTimeoutInSeconds = 1;
        SqsMessage message = new SqsReader(
                PropertiesUtil.AWS_SQS_ACCESS_KEY,
                PropertiesUtil.AWS_SQS_SECRET_KEY,
                Regions.EU_WEST_1,
                mQueue.getQueueName(),
                visibilityTimeoutInSeconds).getMessage();
        //Snooze until the after the visibility timeout has expired
        Sandman.snoozeMillis(1200);
        //An exception should be thrown as we try to delete the message after it has become visible in queue again
        assertThatExceptionOfType(RuntimeException.class)
                .isThrownBy(() -> message.deleteMessageFromQueue())
                .withMessageStartingWith("Could not delete message as it has become visible");
    }


    @Test
    public void getBody_CreateMessageWithRandomString_SameRandomString() {
        String messageToWrite = RandomUtil.getRandomString(10);
        String messageRead = mQueue.send(messageToWrite).read().getBody();
        assertThat(messageRead).isEqualTo(messageToWrite);
    }
}