package io.schinzel.awsutils.sqs;

import io.schinzel.basicutils.configvar.ConfigVar;

/**
 *
 * @author Schinzel
 */
class PropertiesUtil {
    static String AWS_SQS_ACCESS_KEY = ConfigVar.create(".env").getValue("AWS_SQS_ACCESS_KEY");
    static String AWS_SQS_SECRET_KEY = ConfigVar.create(".env").getValue("AWS_SQS_SECRET_KEY");
}
