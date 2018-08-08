package io.schinzel.awsutils.sqs;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.Message;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;

/**
 * The purpose of this class is to represent an AWS SQS message.
 *
 * @author Schinzel
 */
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public class SqsMessage {
    private final AmazonSQS mSqsClient;
    private final String mQueueUrl;
    private final Message mMessage;


    /**
     * @return The body of the message
     */
    public String getBody() {
        return mMessage.getBody();
    }


    /**
     *
     * @return Deletes the message from the queue.
     */
    public SqsMessage deleteMessageFromQueue() {
        mSqsClient.deleteMessage(mQueueUrl, mMessage.getReceiptHandle());
        return this;
    }

}
