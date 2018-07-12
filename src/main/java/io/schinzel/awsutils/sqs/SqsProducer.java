package io.schinzel.awsutils.sqs;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
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
public class SqsProducer {
    private static final String GROUP_ID = "my_group_id";

    @Builder(buildMethodName = "send")
    SqsProducer(String awsAccessKey, String awsSecretKey, Regions region, String queueName, String message) {
        Thrower.throwIfVarEmpty(awsAccessKey, "awsAccessKey");
        Thrower.throwIfVarEmpty(awsSecretKey, "awsSecretKey");
        Thrower.throwIfVarNull(region, "region");
        Thrower.throwIfVarEmpty(queueName, "queueName");
        Thrower.throwIfVarEmpty(message, "message");
        AWSCredentials credentials = new BasicAWSCredentials(awsAccessKey, awsSecretKey);
        AmazonSQS sqsClient = AmazonSQSClientBuilder
                .standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withRegion(region)
                .build();
        String queueUrl = sqsClient.getQueueUrl(queueName).getQueueUrl();
        SendMessageRequest sendMsgRequest = new SendMessageRequest()
                .withQueueUrl(queueUrl)
                .withMessageBody(message);
        sendMsgRequest
                //Set a group id. As this is not used currently, it is set to a hard coded value
                .withMessageGroupId(GROUP_ID)
                //Add a unique if to the message which is used to prevent that the message is duplicated
                .withMessageDeduplicationId(getUniqueId());
        sqsClient.sendMessage(sendMsgRequest);
    }


    /**
     * @return A unique id.
     */
    static String getUniqueId() {
        return String.valueOf(System.nanoTime()) + "_" + RandomUtil.getRandomString(10);
    }


}
