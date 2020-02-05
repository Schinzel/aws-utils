package io.schinzel.awsutils.sqs;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.AmazonSQSException;
import com.amazonaws.services.sqs.model.Message;
import io.schinzel.basicutils.str.Str;
import io.schinzel.queue.IMessage;
import lombok.Builder;
import lombok.experimental.Accessors;

/**
 * The purpose of this class is to represent an AWS SQS message.
 *
 * @author Schinzel
 */
@Builder
@Accessors(prefix = "m")
public class SqsMessage implements IMessage {
    private final AmazonSQS mSqsClient;
    private final String mQueueUrl;
    private final Message mMessage;


    /**
     * @return The body of the message
     */
    @Override
    public String getBody() {
        return mMessage.getBody();
    }


    /**
     * @return The number of times a message has been read from the queue but not deleted
     */
    @Override
    public int getNumberOfTimesRead() {
        String numberOfTimesReadAsString = mMessage.getAttributes().get("ApproximateReceiveCount");
        if (numberOfTimesReadAsString == null) {
            throw new RuntimeException("The attribute 'ApproximateReceiveCount' was missing from the returned message");
        }
        int returnValue;
        try {
            returnValue = Integer.parseInt(numberOfTimesReadAsString);
        } catch (NumberFormatException e) {
            throw new RuntimeException("The value '" + numberOfTimesReadAsString + "' for attribute 'ApproximateReceiveCount' could not be parsed into a int");
        }
        return returnValue;
    }


    /**
     * The delete has to be done while the message is invisible in queue. If this method is invoked after
     * the message has become visible an exception is thrown.
     *
     * @return Deletes the message from the queue
     */
    @Override
    public SqsMessage deleteMessageFromQueue() {
        try {
            mSqsClient.deleteMessage(mQueueUrl, mMessage.getReceiptHandle());
        } catch (AmazonSQSException e) {
            //If the error was that the message has become visible in queue again
            if (e.getMessage().contains("The receipt handle has expired")) {
                //Throw a clear error message
                Str.create()
                        .a("Could not delete message as it has become visible in queue again. ")
                        .a("Message id: ").aq(mMessage.getMessageId()).asp()
                        .a("Body: ").aq(mMessage.getBody()).asp()
                        .throwRuntime();
            } else {
                //rethrow message
                throw e;
            }
        }
        return this;
    }

}
