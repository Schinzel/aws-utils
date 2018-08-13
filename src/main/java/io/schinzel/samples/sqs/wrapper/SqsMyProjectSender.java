package io.schinzel.samples.sqs.wrapper;

import com.amazonaws.regions.Regions;
import io.schinzel.awsutils.sqs.IQueueSender;
import io.schinzel.awsutils.sqs.SqsSender;
import io.schinzel.basicutils.configvar.ConfigVar;

/**
 * The purpose of this class is to show how SqsSender can be wrapped for easier use.
 *
 * @author Schinzel
 */
public class SqsMyProjectSender implements IQueueSender {
    private static final String AWS_SQS_ACCESS_KEY = ConfigVar.create(".env").getValue("AWS_SQS_ACCESS_KEY");
    private static final String AWS_SQS_SECRET_KEY = ConfigVar.create(".env").getValue("AWS_SQS_SECRET_KEY");
    private static final Regions REGION = Regions.EU_WEST_1;
    private final IQueueSender mQueueSender;


    public static SqsMyProjectSender create(io.schinzel.samples.sqs.wrapper.IQueueName queue) {
        return new SqsMyProjectSender(queue);
    }

    private SqsMyProjectSender(io.schinzel.samples.sqs.wrapper.IQueueName queue) {
        mQueueSender = SqsSender.builder()
                .awsAccessKey(AWS_SQS_ACCESS_KEY)
                .awsSecretKey(AWS_SQS_SECRET_KEY)
                .region(REGION)
                .queueName(queue.getQueueName())
                .build();
    }


    @Override
    public IQueueSender send(String message) {
        return mQueueSender.send(message);
    }
}
