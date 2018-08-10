package io.schinzel.awsutils.sqs;


import com.google.common.base.Strings;
import io.schinzel.basicutils.FunnyChars;
import io.schinzel.basicutils.RandomUtil;
import org.junit.After;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * The purpose of this class
 *
 * @author Schinzel
 */
public class SqsReaderSenderTest {
    private final QueueUtil mQueue = new QueueUtil(SqsSenderTest.class);

    @After
    public void after() {
        //Delete queue used in test
        mQueue.deleteQueue();
    }


    public SqsReaderSenderTest send(String messageToWrite) {
        SqsSender.builder()
                .awsAccessKey(PropertiesUtil.AWS_SQS_ACCESS_KEY)
                .awsSecretKey(PropertiesUtil.AWS_SQS_SECRET_KEY)
                .queueName(mQueue.getQueueName())
                .region(mQueue.getRegion())
                .message(messageToWrite)
                .send();
        return this;
    }


    public SqsMessage read() {
        return SqsReader.builder()
                .awsAccessKey(PropertiesUtil.AWS_SQS_ACCESS_KEY)
                .awsSecretKey(PropertiesUtil.AWS_SQS_SECRET_KEY)
                .queueName(mQueue.getQueueName())
                .region(mQueue.getRegion())
                .build()
                .getMessage();
    }


    @Test
    public void sendAndRead_ShortMessage() {
        String messageToWrite = RandomUtil.getRandomString(1);
        String messageRead = this
                .send(messageToWrite)
                .read()
                .getBody();
        assertThat(messageRead).isEqualTo(messageToWrite);
    }


    @Test
    public void sendAndRead_LongMessage() {
        String messageToWrite = "my content "
                + RandomUtil.getRandomString(5)
                + " "
                + Strings.repeat("*", 100_000);
        String messageRead = this
                .send(messageToWrite)
                .read()
                .getBody();
        assertThat(messageRead).isEqualTo(messageToWrite);
    }

    
    @Test
    public void sendAndRead_FunnyChars() {
        for (FunnyChars funnyChars : FunnyChars.values()) {
            String messageToWrite = funnyChars.getString();
            this.send(messageToWrite);
            SqsMessage messageRead = this.read();
            assertThat(messageRead.getBody()).isEqualTo(messageToWrite);
            messageRead.deleteMessageFromQueue();
        }
    }


}
