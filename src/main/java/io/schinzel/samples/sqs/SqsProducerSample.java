package io.schinzel.samples.sqs;

import com.amazonaws.regions.Regions;
import io.schinzel.queue.IQueueProducer;
import io.schinzel.awsutils.sqs.SqsProducer;
import io.schinzel.basicutils.configvar.ConfigVar;
import io.schinzel.basicutils.str.Str;
import io.schinzel.samples.sqs.wrapper.SqsMyProjectProducer;
import io.schinzel.samples.sqs.wrapper.SqsQueues;

/**
 * The purpose of this class is to show how a message is written to an AWS SQS queue.
 *
 * @author Schinzel
 */
public class SqsProducerSample {

    public static void main(String[] args) {
        sampleVanillaUsage();
        sampleWithCustomWrapper();
    }


    /**
     * This sample simply uses the SqsProducer class.
     */
    private static void sampleVanillaUsage() {
        String awsSqsAccessKey = ConfigVar.create(".env").getValue("AWS_SQS_ACCESS_KEY");
        String awsSqsSecretKey = ConfigVar.create(".env").getValue("AWS_SQS_SECRET_KEY");
        String message = "My message";
        IQueueProducer queueProducer = SqsProducer.builder()
                .awsAccessKey(awsSqsAccessKey)
                .awsSecretKey(awsSqsSecretKey)
                .queueName("my_first_queue.fifo")
                .region(Regions.EU_WEST_1)
                .build();
        queueProducer.send(message);
        Str.create("Sent message ").aq(message).writeToSystemOut();
    }

    /**
     * This sample relies a custom sample-wrapper around the SqsProducer class. This that makes the
     * sending of messages less verbose, credentials centralised and more fail safe with the
     * different queues in an enum.
     */
    private static void sampleWithCustomWrapper() {
        String message = "My message";
        IQueueProducer queueProducer = SqsMyProjectProducer.create(SqsQueues.SEND_SMS);
        queueProducer.send(message);
        Str.create("Sent message ").aq(message).writeToSystemOut();
    }

}
