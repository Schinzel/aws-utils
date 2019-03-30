package io.schinzel.awsutils.s3file;

import com.amazonaws.regions.Regions;
import io.schinzel.basicutils.RandomUtil;
import io.schinzel.basicutils.configvar.ConfigVar;

/**
 * Util class to create S3File instances.
 * <p>
 * Created by Schinzel on 2018-06-24
 */
class S3FileUtil {
    private static final String AWS_S3_ACCESS_KEY = ConfigVar.create(".env").getValue("AWS_S3_ACCESS_KEY");
    private static final String AWS_S3_SECRET_KEY = ConfigVar.create(".env").getValue("AWS_S3_SECRET_KEY");
    private static final String BUCKET_NAME = "schinzel.io";

    static S3File getS3File() {
        String fileName = RandomUtil.getRandomString(20) + ".txt";
        return S3File.builder()
                .awsAccessKey(AWS_S3_ACCESS_KEY)
                .awsSecretKey(AWS_S3_SECRET_KEY)
                .region(Regions.EU_WEST_1)
                .bucketName(BUCKET_NAME)
                .fileName(fileName)
                .build();
    }

}
