package io.schinzel.awsutils.sqs;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.AmazonSQSException;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import lombok.Builder;
import lombok.Getter;

import java.util.Collections;
import java.util.List;

/**
 * The purpose of this class is to read messages from an AWS SQS queue.
 *
 * @author Schinzel
 */
public class SqsReader {
    private final AmazonSQS mSqsClient;
    private final String mQueueUrl;
    /** Flag that is true if all systems are working */
    @Getter
    private boolean mAllSystemsWorking = true;

    @Builder
    SqsReader(String awsAccessKey, String awsSecretKey, Regions region, String queueName) {
        mSqsClient = ClientCache
                .getSingleton()
                .getSqsClient(awsAccessKey, awsSecretKey, region);
        //Get the queue url for the argument queue name.
        mQueueUrl = QueueUrlCache
                .getSingleton()
                .getQueueUrl(queueName, mSqsClient);
    }


    public String receive() {
        List<Message> messages = Collections.emptyList();
        do {
            ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest()
                    .withQueueUrl(mQueueUrl)
                    .withMaxNumberOfMessages(1)
                    .withWaitTimeSeconds(20);
            try {
                messages = mSqsClient.receiveMessage(receiveMessageRequest).getMessages();
            } catch (IllegalStateException e) {
                mAllSystemsWorking = false;
                return "";
            } catch (AmazonSQSException awsException) {
                //If the queue does not exist anymore
                if (awsException.getErrorCode().equals("AWS.SimpleQueueService.NonExistentQueue")) {
                    mAllSystemsWorking = false;
                    return "";
                }
            }
        } while (messages.isEmpty());
        Message message = messages.get(0);
        mSqsClient.deleteMessage(mQueueUrl, message.getReceiptHandle());
        return message.getBody();
    }
}
