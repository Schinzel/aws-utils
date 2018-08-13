package io.schinzel.samples.sqs;

import com.amazonaws.regions.Regions;
import io.schinzel.awsutils.sqs.IMessage;
import io.schinzel.awsutils.sqs.IQueueReader;
import io.schinzel.awsutils.sqs.SqsReader;
import io.schinzel.basicutils.configvar.ConfigVar;
import io.schinzel.basicutils.str.Str;

/**
 * The purpose of this class is to show how a message is read from an AWS SQS queue.
 *
 * @author Schinzel
 */
public class SqsReaderSample {

    public static void main(String[] args) {
        String awsSqsAccessKey = ConfigVar.create(".env").getValue("AWS_SQS_ACCESS_KEY");
        String awsSqsSecretKey = ConfigVar.create(".env").getValue("AWS_SQS_SECRET_KEY");
        //Create reader. Can be reused if desirable.
        IQueueReader sqsReader = SqsReader.builder()
                .awsAccessKey(awsSqsAccessKey)
                .awsSecretKey(awsSqsSecretKey)
                .queueName("111my_first_queue.fifo")
                .region(Regions.EU_WEST_1)
                .build();
        //Get a message from the queue. If there is no message, code will will wait here until there is one.
        //When a message is returned, that will be made invisible in the queue.
        IMessage message = sqsReader.getMessage();
        //Get the body from the message and do something with it
        String body = message.getBody();
        //Delete the message from the queue. If not deleted the message will become visible automatically. After how
        //long time is set by the reader (at time of writing 60 seconds)
        message.deleteMessageFromQueue();
        Str.create("Read message ").aq(body).writeToSystemOut();
    }
}
