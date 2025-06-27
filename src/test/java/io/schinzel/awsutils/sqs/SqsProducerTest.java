package io.schinzel.awsutils.sqs;

import software.amazon.awssdk.regions.Region;
import org.junit.After;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

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


    @Test
    public void getUniqueId_1000_No() {
        Set<String> set = new HashSet<>();
        for (int i = 0; i < 1000; i++) {
            String uniqueId = SqsProducer.getUniqueId();
            boolean wasUniqueId = set.add(uniqueId);
            assertThat(wasUniqueId).isTrue();

        }
    }

    /**
     * PropertiesUtil.AWS_SQS_ACCESS_KEY, PropertiesUtil.AWS_SQS_SECRET_KEY
     */
    @Test
    public void builderGuaranteedOrder_true_true(){
        boolean guaranteedOrder = SqsProducer.builder()
                .awsAccessKey(PropertiesUtil.AWS_SQS_ACCESS_KEY)
                .awsSecretKey(PropertiesUtil.AWS_SQS_SECRET_KEY)
                .region(Region.EU_WEST_1)
                .queueName("any_name.fifo")
                .guaranteedOrder(true)
                .build()
                .mGuaranteedOrder;
        assertThat(guaranteedOrder).isTrue();
    }

    @Test
    public void builderGuaranteedOrder_false_false(){
        boolean guaranteedOrder = SqsProducer.builder()
                .awsAccessKey(PropertiesUtil.AWS_SQS_ACCESS_KEY)
                .awsSecretKey(PropertiesUtil.AWS_SQS_SECRET_KEY)
                .region(Region.EU_WEST_1)
                .queueName("any_name.fifo")
                .guaranteedOrder(false)
                .build()
                .mGuaranteedOrder;
        assertThat(guaranteedOrder).isFalse();

    }

    @Test
    public void builderGuaranteedOrder_notSet_true(){
        boolean guaranteedOrder = SqsProducer.builder()
                .awsAccessKey(PropertiesUtil.AWS_SQS_ACCESS_KEY)
                .awsSecretKey(PropertiesUtil.AWS_SQS_SECRET_KEY)
                .region(Region.EU_WEST_1)
                .queueName("any_name.fifo")
                .build()
                .mGuaranteedOrder;
        assertThat(guaranteedOrder).isTrue();
    }

}