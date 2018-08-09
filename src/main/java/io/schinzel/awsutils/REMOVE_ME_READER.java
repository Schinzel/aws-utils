package io.schinzel.awsutils;

import com.amazonaws.regions.Regions;
import io.schinzel.awsutils.sqs.SqsMessage;
import io.schinzel.awsutils.sqs.SqsReader;
import io.schinzel.basicutils.str.Str;

/**
 * The purpose of this class
 *
 * @author Schinzel
 */
public class REMOVE_ME_READER {

    public static void main(String[] args) {
        Str.create("Reader waiting for message...").writeToSystemOut();
        SqsMessage sqsMessage = SqsReader.builder()
                .awsAccessKey(REMOVE_ME.AWS_SQS_ACCESS_KEY)
                .awsSecretKey(REMOVE_ME.AWS_SQS_SECRET_KEY)
                .queueName("my_first_queue.fifo")
                .region(Regions.EU_WEST_1)
                .build()
                .getMessage();
        Str.create("Got a message! Message: ")
                .aq(sqsMessage.getBody())
                .writeToSystemOut();
        sqsMessage.deleteMessageFromQueue();
        Str.create("Message deleted: ")
                .aq(sqsMessage.getBody())
                .writeToSystemOut();

    }
}
