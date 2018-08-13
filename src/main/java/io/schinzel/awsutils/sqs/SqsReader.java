package io.schinzel.awsutils.sqs;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import lombok.Builder;

import java.util.List;

/**
 * The purpose of this class is to read messages from an AWS SQS queue.
 *
 * @author Schinzel
 */
public class SqsReader implements ISqsReader {
    private static final int VISIBILITY_TIMEOUT_IN_SECONDS = 60;
    private final AmazonSQS mSqsClient;
    private final String mQueueUrl;
    private final ReceiveMessageRequest mReceiveMessageRequest;


    @Builder
    SqsReader(String awsAccessKey, String awsSecretKey, Regions region, String queueName) {
        mSqsClient = ClientCache
                .getSingleton()
                .getSqsClient(awsAccessKey, awsSecretKey, region);
        //Get the queue url for the argument queue name.
        mQueueUrl = QueueUrlCache
                .getSingleton()
                .getQueueUrl(queueName, mSqsClient);
        mReceiveMessageRequest = new ReceiveMessageRequest()
                //URL of the Amazon SQS queue from which messages are received
                .withQueueUrl(mQueueUrl)
                //The maximum number of messages to return.
                .withMaxNumberOfMessages(1)
                //The duration the call waits for a message to arrive in the queue before returning.
                .withWaitTimeSeconds(20)
                //Make the message invisible for x seconds
                .withVisibilityTimeout(VISIBILITY_TIMEOUT_IN_SECONDS);
    }


    /**
     * @return A message. The message is made invisible for a period of time. Has to explicitly be deleted from the
     * queue.
     */
    public SqsMessage getMessage() {
        List<Message> messages;
        do {
            //Get messages. Could be 1 or 0. 0 if there was no visible messages in queue.
            messages = mSqsClient
                    .receiveMessage(mReceiveMessageRequest)
                    .getMessages();
        }//Loop if there was not message
        while (messages.isEmpty());
        //If got here there was 1 message. Get this message
        Message message = messages.get(0);
        //Create and return message
        return new SqsMessage(mSqsClient, mQueueUrl, message);
    }


}
