package io.schinzel.samples.sqs;

import io.schinzel.awsutils.sqs.SqsSender;
import io.schinzel.basicutils.configvar.ConfigVar;
import com.amazonaws.regions.Regions;
import lombok.Builder;

/**
 * The purpose of this class is to show how SqsSender can be wrapped for easier use.
 *
 * @author Schinzel
 */
public class SqsMyProjectSender {
    private static String AWS_SQS_ACCESS_KEY = ConfigVar.create(".env").getValue("AWS_SQS_ACCESS_KEY");
    private static String AWS_SQS_SECRET_KEY = ConfigVar.create(".env").getValue("AWS_SQS_SECRET_KEY");
    private static Regions REGION = Regions.EU_WEST_1;

    @Builder(buildMethodName = "send")
    SqsMyProjectSender(IQueue queue, String message) {
        SqsSender.builder()
                .awsAccessKey(AWS_SQS_ACCESS_KEY)
                .awsSecretKey(AWS_SQS_SECRET_KEY)
                .region(REGION)
                .queueName(queue.getQueueName())
                .message(message)
                .send();
    }


}
