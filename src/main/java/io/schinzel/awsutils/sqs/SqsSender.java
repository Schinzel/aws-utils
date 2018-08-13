package io.schinzel.awsutils.sqs;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import io.schinzel.basicutils.RandomUtil;
import io.schinzel.basicutils.thrower.Thrower;
import lombok.Builder;

/**
 * The purpose of this class is to send a message to an AWS SQS queue. If a queue with the constructor argument name
 * does not exist, one is created.
 * <p>
 * Fifo queue is assumed.
 * <p>
 * Created by Schinzel on 2018-07-12
 */
public class SqsSender implements IQueueSender {
    private final AmazonSQS mSqsClient;
    private final String mQueueUrl;

    @Builder
    SqsSender(String awsAccessKey, String awsSecretKey, Regions region, String queueName) {
        mSqsClient = ClientCache
                .getSingleton()
                .getSqsClient(awsAccessKey, awsSecretKey, region);
        //Get the queue url for the argument queue name.
        mQueueUrl = QueueUrlCache
                .getSingleton()
                .getQueueUrl(queueName, mSqsClient);

    }


    @Override
    public SqsSender send(String message) {
        Thrower.throwIfVarEmpty(message, "message");
        SendMessageRequest sendMsgRequest = new SendMessageRequest()
                .withQueueUrl(mQueueUrl)
                .withMessageBody(message)
                //Add a unique id to the message which is used to prevent that the message is duplicated.
                //This is a required argument if content-based deduplication has been disabled, which is
                //this class assumes it is.
                .withMessageDeduplicationId(getUniqueId())
                //Set a group id. As this is not used currently used, it is set to a hard coded value.
                //This argument is required if MessageDeduplicationId is set.
                .withMessageGroupId("my_group_id");
        mSqsClient.sendMessage(sendMsgRequest);
        return this;
    }


    /**
     * @return A random unique id.
     */
    private static String getUniqueId() {
        return String.valueOf(System.nanoTime()) + "_" + RandomUtil.getRandomString(10);
    }


}
