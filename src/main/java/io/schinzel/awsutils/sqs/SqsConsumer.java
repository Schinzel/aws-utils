package io.schinzel.awsutils.sqs;

import com.amazonaws.regions.Regions;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;
import software.amazon.awssdk.services.sqs.model.MessageSystemAttributeName;
import io.schinzel.queue.IQueueConsumer;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.Accessors;
import java.util.List;

/**
 * The purpose of this class is to read messages from an AWS SQS queue.
 *
 * @author Schinzel
 */
@Accessors(prefix = "m")
public class SqsConsumer implements IQueueConsumer {
    private static final int VISIBILITY_TIMEOUT_IN_SECONDS = 60;
    private final SqsClient mSqsClient;
    @Getter
    private final String mQueueUrl;
    private final ReceiveMessageRequest mReceiveMessageRequest;


    @SuppressWarnings("unused")
    @Builder
    SqsConsumer(String awsAccessKey, String awsSecretKey, Regions region, String queueName) {
        this(awsAccessKey, awsSecretKey, region, queueName, VISIBILITY_TIMEOUT_IN_SECONDS);
    }

    //Exists for testing
    SqsConsumer(String awsAccessKey, String awsSecretKey, Regions region, String queueName, int visibilityTimeoutInSeconds) {
        mSqsClient = ClientCache
                .getSingleton()
                .getSqsClient(awsAccessKey, awsSecretKey, region);
        //Get the queue url for the argument queue name.
        mQueueUrl = QueueUrlCache
                .getSingleton()
                .getQueueUrl(queueName, mSqsClient);
        mReceiveMessageRequest = ReceiveMessageRequest.builder()
                //URL of the Amazon SQS queue from which messages are received
                .queueUrl(mQueueUrl)
                //Request that returned messages contains the attribute ApproximateReceiveCount
                //which states the number of times a message has been read from the queue but not deleted
                .messageSystemAttributeNames(MessageSystemAttributeName.APPROXIMATE_RECEIVE_COUNT)
                //The maximum number of messages to return.
                .maxNumberOfMessages(1)
                //The duration the call waits for a message to arrive in the queue before returning.
                .waitTimeSeconds(20)
                //Make the message invisible for x seconds
                .visibilityTimeout(visibilityTimeoutInSeconds)
                .build();
    }

    private SqsConsumer(SqsClient sqsClient, String queueUrl, ReceiveMessageRequest receiveMessageRequest) {
        mSqsClient = sqsClient;
        mQueueUrl = queueUrl;
        mReceiveMessageRequest = receiveMessageRequest;
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
                    .messages();
        }//Loop if there was not message
        while (messages.isEmpty());
        //If got here there was 1 message. Get this message
        Message message = messages.get(0);
        //Create and return message
        return SqsMessage.builder()
                .sqsClient(mSqsClient)
                .queueUrl(mQueueUrl)
                .message(message)
                .build();
    }


    /**
     *
     * @return A clone of this consumer
     */
    public SqsConsumer clone() {
        return new SqsConsumer(mSqsClient, mQueueUrl, mReceiveMessageRequest);
    }


    public void close() {
        mSqsClient.close();
    }
}
