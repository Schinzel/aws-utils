package io.schinzel.awsutils.sqs;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import io.schinzel.basicutils.RandomUtil;
import io.schinzel.basicutils.collections.Cache;
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
    /**
     * Caches URLs to queues.
     * 2018-08-07 With this cache it takes 15 ms to send a message, i.e. to run all the code in the constructor.
     * Without this cache - and all other code the same - the average send takes 25 ms. Message size 250 chars.
     * Running the code on a EC2 instance. Caches had data when performance was measured.
     */
    private static Cache<String, String> QUEUE_URL_CACHE = new Cache<>();

    @Builder(buildMethodName = "send")
    SqsSender(String awsAccessKey, String awsSecretKey, Regions region, String queueName, String message) {
        Thrower.createInstance()
                .throwIfVarEmpty(awsAccessKey, "awsAccessKey")
                .throwIfVarEmpty(awsSecretKey, "awsSecretKey")
                .throwIfVarNull(region, "region")
                .throwIfVarEmpty(queueName, "queueName")
                .throwIfVarEmpty(message, "message")
                .throwIfFalse(queueName.endsWith(".fifo"), "Queue name must end in '.fifo'. Only fifo queues supported");
        AmazonSQS sqsClient = ClientCache
                .getSingleton()
                .getSqsClient(awsAccessKey, awsSecretKey, region);
        //Get the queue url for the argument queue name.
        String queueUrl = QUEUE_URL_CACHE.has(queueName)
                ? QUEUE_URL_CACHE.get(queueName)
                : QUEUE_URL_CACHE.putAndGet(queueName, sqsClient.getQueueUrl(queueName).getQueueUrl());
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
