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
public class SqsProducerConsumerTest {
    private final QueueUtil mQueue = new QueueUtil(SqsProducerTest.class);

    @After
    public void after() {
        //Delete queue used in test
        mQueue.deleteQueue();
    }




    @Test
    public void sendAndRead_ShortMessage() {
        String messageToWrite = RandomUtil.getRandomString(1);
        String messageRead = mQueue
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
        String messageRead = mQueue
                .send(messageToWrite)
                .read()
                .getBody();
        assertThat(messageRead).isEqualTo(messageToWrite);
    }


    @Test
    public void sendAndRead_FunnyChars() {
        for (FunnyChars funnyChars : FunnyChars.values()) {
            String messageToWrite = funnyChars.getString();
            mQueue.send(messageToWrite);
            SqsMessage messageRead = mQueue.read();
            assertThat(messageRead.getBody()).isEqualTo(messageToWrite);
            messageRead.deleteMessageFromQueue();
        }
    }


}
