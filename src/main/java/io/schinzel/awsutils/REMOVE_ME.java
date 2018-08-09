package io.schinzel.awsutils;

import com.amazonaws.regions.Regions;
import io.schinzel.awsutils.sqs.SqsMessage;
import io.schinzel.awsutils.sqs.SqsReader;
import io.schinzel.awsutils.sqs.SqsSender;
import io.schinzel.basicutils.RandomUtil;
import io.schinzel.basicutils.configvar.ConfigVar;
import io.schinzel.basicutils.str.Str;
import io.schinzel.basicutils.timekeeper.Timekeeper;

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
        reader();
    }


    private static void reader() {
        String message = "My message " + RandomUtil.getRandomString(5);
        send(message);
        System.out.println("Wrote '" + message + "'");
        SqsMessage sqsMessage = SqsReader.builder()
                .awsAccessKey(AWS_SQS_ACCESS_KEY)
                .awsSecretKey(AWS_SQS_SECRET_KEY)
                .queueName("my_first_queue.fifo")
                .region(Regions.EU_WEST_1)
                .build()
                .getMessage();
        System.out.println("Message received '" + sqsMessage.getBody() + "'");
        sqsMessage.deleteMessageFromQueue();
        System.out.println("Message deleted");
    }


    private static void send(String message) {
        SqsSender.builder()
                .awsAccessKey(AWS_SQS_ACCESS_KEY)
                .awsSecretKey(AWS_SQS_SECRET_KEY)
                .queueName("my_first_queue.fifo")
                .region(Regions.EU_WEST_1)
                .message(message)
                .send();
    }


    private static void senderPerformance() {
        boolean warmUp = true;
        String message = "My message " + RandomUtil.getRandomString(5);
        if (warmUp) {
            send(message);

        }
        Timekeeper tk = Timekeeper.getSingleton().reset();
        for (int i = 0; i < 10; i++) {
            tk.start("send");
            send(message);
            tk.stop();
        }
        tk.getResults().getStr().writeToSystemOut();
    }
}
