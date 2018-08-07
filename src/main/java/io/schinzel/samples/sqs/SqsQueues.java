package io.schinzel.samples.sqs;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

/**
 * The purpose of this class to show how all queue names can be centralized for code that is easier to read, use and
 * maintain.
 *
 * Implements an interface for easier testing.
 *
 * @author Schinzel
 */
@Accessors(prefix = "m")
@RequiredArgsConstructor
public enum SqsQueues implements IQueue {
    SEND_SMS("sms.fifo");


    @Getter
    private final String mQueueName;
}
