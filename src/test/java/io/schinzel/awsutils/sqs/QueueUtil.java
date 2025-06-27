package io.schinzel.awsutils.sqs;

import com.amazonaws.regions.Regions;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.GetQueueAttributesRequest;
import software.amazon.awssdk.services.sqs.model.GetQueueAttributesResponse;
import software.amazon.awssdk.services.sqs.model.QueueAttributeName;
import io.schinzel.basicutils.RandomUtil;
import lombok.Getter;
import lombok.experimental.Accessors;

/**
 * The purpose of this class is to be a utility class to help create temporary queues for tests. Creates a queue with
 * a random name.
 *
 * @author Schinzel
 */
@Accessors(prefix = "m")
class QueueUtil {
    private static final Regions REGION = Regions.EU_WEST_1;
    @Getter
    private final SqsClient mSqsClient;
    @Getter
    private final String mQueueName;
    @Getter
    private final String mQueueUrl;


    QueueUtil(Class testClass) {
        //Create a queue name that indicates which test created it and has a random element
        mQueueName = testClass.getSimpleName() + "_" + RandomUtil.getRandomString(5) + ".fifo";
        mSqsClient = ClientCache.getSingleton()
                .getSqsClient(PropertiesUtil.AWS_SQS_ACCESS_KEY, PropertiesUtil.AWS_SQS_SECRET_KEY, REGION);
        mQueueUrl = QueueUrlCache.getSingleton()
                .getQueueUrl(mQueueName, mSqsClient);
        QueueUrlCache.getSingleton().mQueueUrlCache
                .invalidate();
        ClientCache.getSingleton().mSqsClientCache
                .invalidate();
    }


    QueueUtil send(String message) {
        SqsProducer.builder()
                .awsAccessKey(PropertiesUtil.AWS_SQS_ACCESS_KEY)
                .awsSecretKey(PropertiesUtil.AWS_SQS_SECRET_KEY)
                .queueName(mQueueName)
                .region(Regions.EU_WEST_1)
                .build()
                .send(message);
        return this;
    }


    SqsMessage read() {
        return SqsConsumer.builder()
                .awsAccessKey(PropertiesUtil.AWS_SQS_ACCESS_KEY)
                .awsSecretKey(PropertiesUtil.AWS_SQS_SECRET_KEY)
                .queueName(mQueueName)
                .region(REGION)
                .build()
                .getMessage();
    }


    int getNumberOfMessages() {
        GetQueueAttributesRequest getQueueAttributesRequest = GetQueueAttributesRequest.builder()
                .queueUrl(mQueueUrl)
                .attributeNames(QueueAttributeName.ALL)
                .build();
        GetQueueAttributesResponse getQueueAttributesResult = mSqsClient
                .getQueueAttributes(getQueueAttributesRequest);
        String numberOfMessage = getQueueAttributesResult.attributes().get(QueueAttributeName.APPROXIMATE_NUMBER_OF_MESSAGES);
        return Integer.valueOf(numberOfMessage);
    }


    void deleteQueue() {
        mSqsClient.deleteQueue(builder -> builder.queueUrl(mQueueUrl));
    }

}
