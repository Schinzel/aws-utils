package io.schinzel.awsutils;

import com.amazonaws.regions.Regions;
import io.schinzel.awsutils.sqs.SqsProducer;
import io.schinzel.basicutils.configvar.ConfigVar;
import io.schinzel.basicutils.str.Str;

/**
 * Purpose of this class is ...
 * <p>
 * Created by Schinzel on 2018-07-12
 */
public class REMOVE_ME {
    private static String AWS_SQS_ACCESS_KEY = ConfigVar.create(".env").getValue("AWS_SQS_ACCESS_KEY");
    private static String AWS_SQS_SECRET_KEY = ConfigVar.create(".env").getValue("AWS_SQS_SECRET_KEY");


    public static void main(String[] args) {
        Str.create()
                .anl("*******************************************")
                .anl("Queue time!")
                .anl("*******************************************")
                .writeToSystemOut();
        SqsProducer.builder()
                .awsAccessKey(AWS_SQS_ACCESS_KEY)
                .awsSecretKey(AWS_SQS_SECRET_KEY)
                .queueName("my_first_queue.fifo")
                .region(Regions.EU_WEST_1)
                .message("My message ")
                .build();

    }
}
