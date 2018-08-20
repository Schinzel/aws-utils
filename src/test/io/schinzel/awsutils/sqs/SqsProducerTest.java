package io.schinzel.awsutils.sqs;

import org.junit.After;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Schinzel
 */
public class SqsProducerTest {
    private final QueueUtil mQueue = new QueueUtil(SqsProducerTest.class);


    @After
    public void after() {
        //Delete queue used in test
        mQueue.deleteQueue();
    }




    @Test
    public void send_AnyMessage_QueueLength1() {
        mQueue.send("hi there!");
        assertThat(mQueue.getNumberOfMessages()).isEqualTo(1);
    }


    @Test
    public void send_5Messages_QueueLength5() {
        mQueue
                .send("hi there!")
                .send("hi there!")
                .send("hi there!")
                .send("hi there!")
                .send("hi there!");
        assertThat(mQueue.getNumberOfMessages()).isEqualTo(5);
    }

}