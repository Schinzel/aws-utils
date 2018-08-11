package io.schinzel.awsutils.sqs;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.CreateQueueRequest;
import com.amazonaws.services.sqs.model.GetQueueAttributesRequest;
import com.amazonaws.services.sqs.model.GetQueueAttributesResult;
import com.google.common.collect.ImmutableMap;
import io.schinzel.basicutils.RandomUtil;
import lombok.Getter;
import lombok.experimental.Accessors;

import java.util.Map;

/**
 * The purpose of this class is to be a utility class to help create temporary queues for tests. Creates a queue with
 * a random name.
 *
 * @author Schinzel
 */
@Accessors(prefix = "m")
class QueueUtil {
    @Getter
    private final Regions mRegion = Regions.EU_WEST_1;
    @Getter
    private final AmazonSQS mSqsClient;
    @Getter
    private final String mQueueName;
    @Getter
    private final String mQueueUrl;


    QueueUtil(Class testClass) {
        //Invalidate the cache
        QueueUrlCache.getSingleton().mQueueUrlCache.invalidate();
        //Create a queue name that indicates which test created it and has a random element
        mQueueName = testClass.getSimpleName() + "_" + RandomUtil.getRandomString(5) + ".fifo";
        //Set up AWS credentials
        AWSCredentials credentials = new BasicAWSCredentials(PropertiesUtil.AWS_SQS_ACCESS_KEY, PropertiesUtil.AWS_SQS_SECRET_KEY);
        AWSStaticCredentialsProvider credentialsProvider = new AWSStaticCredentialsProvider(credentials);
        //Construct a new sqs client
        mSqsClient = AmazonSQSClientBuilder
                .standard()
                .withCredentials(credentialsProvider)
                .withRegion(Regions.EU_WEST_1)
                .build();
        //Set queue attributes
        Map<String, String> queueAttributes = ImmutableMap.<String, String>builder()
                .put("FifoQueue", "true")
                .put("ContentBasedDeduplication", "false")
                .build();
        //Create new queue
        CreateQueueRequest createFifoQueueRequest = new CreateQueueRequest(mQueueName)
                .withAttributes(queueAttributes);
        //Get the url of the newly created queue
        mQueueUrl = mSqsClient.createQueue(createFifoQueueRequest)
                .getQueueUrl();
    }


    QueueUtil send(String message) {
        SqsSender.builder()
                .awsAccessKey(PropertiesUtil.AWS_SQS_ACCESS_KEY)
                .awsSecretKey(PropertiesUtil.AWS_SQS_SECRET_KEY)
                .queueName(mQueueName)
                .region(Regions.EU_WEST_1)
                .message(message)
                .send();
        return this;
    }


    public SqsMessage read() {
        return SqsReader.builder()
                .awsAccessKey(PropertiesUtil.AWS_SQS_ACCESS_KEY)
                .awsSecretKey(PropertiesUtil.AWS_SQS_SECRET_KEY)
                .queueName(mQueueName)
                .region(mRegion)
                .build()
                .getMessage();
    }


    int getNumberOfMessages() {
        GetQueueAttributesRequest getQueueAttributesRequest
                = new GetQueueAttributesRequest(mQueueUrl)
                .withAttributeNames("All");
        GetQueueAttributesResult getQueueAttributesResult = mSqsClient
                .getQueueAttributes(getQueueAttributesRequest);
        String numberOfMessage = getQueueAttributesResult.getAttributes().get("ApproximateNumberOfMessages");
        return Integer.valueOf(numberOfMessage);
    }


    void deleteQueue() {
        mSqsClient.deleteQueue(mQueueUrl);
    }
}
