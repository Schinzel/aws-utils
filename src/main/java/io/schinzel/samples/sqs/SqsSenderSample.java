package io.schinzel.samples.sqs;

import com.amazonaws.regions.Regions;
import io.schinzel.awsutils.sqs.SqsSender;
import io.schinzel.basicutils.configvar.ConfigVar;
import io.schinzel.samples.sqs.wrapper.SqsMyProjectSender;
import io.schinzel.samples.sqs.wrapper.SqsQueues;

/**
 * The purpose of this class is to show how a message is written to an AWS SQS queue.
 *
 * @author Schinzel
 */
public class SqsSenderSample {

    public static void main(String[] args) {
        sampleVanillaUsage();
        sampleWithCustomWrapper();
    }


    /**
     * This sample simply uses the SqsSender class.
     */
    private static void sampleVanillaUsage() {
        String awsSqsAccessKey = ConfigVar.create(".env").getValue("AWS_SQS_ACCESS_KEY");
        String awsSqsSecretKey = ConfigVar.create(".env").getValue("AWS_SQS_SECRET_KEY");
        SqsSender.builder()
                .awsAccessKey(awsSqsAccessKey)
                .awsSecretKey(awsSqsSecretKey)
                .queueName("my_first_queue.fifo")
                .region(Regions.EU_WEST_1)
                .build()
                .send("My message");
    }

    /**
     * This sample relies a custom sample-wrapper around the SqsSender class. This that makes the
     * sending of messages less verbose, credentials centralised and more fail safe with the
     * different queues in an enum.
     */
    private static void sampleWithCustomWrapper() {
        SqsMyProjectSender
                .create(SqsQueues.SEND_SMS)
                .send("My message");
    }

}
