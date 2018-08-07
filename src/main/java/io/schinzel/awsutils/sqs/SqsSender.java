package io.schinzel.awsutils.sqs;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import io.schinzel.basicutils.RandomUtil;
import io.schinzel.basicutils.thrower.Thrower;
import lombok.Builder;

/**
 * The purpose of this class is to send messages to an AWS SQS queue.
 * <p>
 * Fifo queue is assumed.
 * <p>
 * Created by Schinzel on 2018-07-12
 */
public class SqsSender {

    @Builder(buildMethodName = "send")
    SqsSender(String awsAccessKey, String awsSecretKey, Regions region, String queueName, String message) {
        Thrower.createInstance()
                .throwIfVarEmpty(message, "message")
                .throwIfFalse(queueName.endsWith(".fifo"), "Queue name must end in '.fifo'. Only fifo queues supported");
        AmazonSQS sqsClient = ClientCache
                .getSingleton()
                .getSqsClient(awsAccessKey, awsSecretKey, region);
        //Get the queue url for the argument queue name.
        String queueUrl = QueueCache
                .getSingleton()
                .getQueueUrl(queueName, sqsClient);
        SendMessageRequest sendMsgRequest = new SendMessageRequest()
                .withQueueUrl(queueUrl)
                .withMessageBody(message)
                //Set a group id. As this is not used currently, it is set to a hard coded value
                .withMessageGroupId("my_group_id")
                //Add a unique id to the message which is used to prevent that the message is duplicated
                .withMessageDeduplicationId(getUniqueId());
        sqsClient.sendMessage(sendMsgRequest);
    }


    /**
     * @return A random unique id.
     */
    static String getUniqueId() {
        return String.valueOf(System.nanoTime()) + "_" + RandomUtil.getRandomString(10);
    }


}
