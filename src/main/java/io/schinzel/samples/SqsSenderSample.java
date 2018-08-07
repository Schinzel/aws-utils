package io.schinzel.samples;

import com.amazonaws.regions.Regions;
import io.schinzel.awsutils.sqs.SqsSender;
import io.schinzel.basicutils.configvar.ConfigVar;
import io.schinzel.samples.sqs.SqsQueues;
import io.schinzel.samples.sqs.SqsMyProjectSender;

/**
 * The purpose of this class
 *
 * @author Schinzel
 */
public class SqsSenderSample {

    public static void main(String[] args) {
        sample1();
        sample2();
    }


    public static void sample1() {
        String awsSqsAccessKey = ConfigVar.create(".env").getValue("AWS_SQS_ACCESS_KEY");
        String awsSqsSecretKey = ConfigVar.create(".env").getValue("AWS_SQS_SECRET_KEY");
        SqsSender.builder()
                .awsAccessKey(awsSqsAccessKey)
                .awsSecretKey(awsSqsSecretKey)
                .queueName("my_first_queue.fifo")
                .region(Regions.EU_WEST_1)
                .message("My message")
                .send();
    }


    public static void sample2() {
        SqsMyProjectSender.builder()
                .queue(SqsQueues.SEND_SMS)
                .message("My messsage")
                .send();
    }

}
