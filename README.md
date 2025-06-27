# aws-utils

More intuitive and concise code for AWS S3 and SQS operations.

## Samples

For sample code see:

`aws-utils/src/main/java/io/schinzel/samples/`

To run the S3File samples and tests add the following lines to a `.env` file in project root. 
The X:es and Y:es are replaced with the actual keys.

```
AWS_S3_ACCESS_KEY=XXXX
AWS_S3_SECRET_KEY=YYYY
```

To run the SQS samples and tests add the following lines to a `.env` file in project root. 
The X:es and Y:es are replaced with the actual keys.

```
AWS_SQS_ACCESS_KEY=XXXX
AWS_SQS_SECRET_KEY=YYYY
```

## Migration Guide (v1.x to v2.0)

Version 2.0 completely removes AWS SDK v1 dependencies. The main breaking change is the Region enum:

**Before (v1.x):**
```java
import com.amazonaws.regions.Regions;

S3File.builder()
    .region(Regions.EU_WEST_1)
    .build();
```

**After (v2.0):**
```java
import software.amazon.awssdk.regions.Region;

S3File.builder()
    .region(Region.EU_WEST_1)
    .build();
```

The region names remain the same (e.g., `EU_WEST_1`, `US_EAST_1`), only the import and class name change.


# Releases

## 2.0.0
_2025-06-27_
- **BREAKING CHANGE**: Completely migrated from AWS SDK v1 to v2
  - Changed from `com.amazonaws.regions.Regions` to `software.amazon.awssdk.regions.Region`
- Updated all dependencies to latest versions

## 1.0.7
_2025-06-27_
- Migrated from AWS SDK v1 to v2
- Updated all dependencies

## 1.0.6
_2024-10-09_
- Added support for uploading webp files to S3File. Affected methods:
  - `write(String fileContent)`
  - `write(byte[] fileContent)`
- Updated dependencies

## 1.0.5
_2023-05-22_
- New method added to `S3File`
    - `write(byte[] fileContent)`

## 1.0.4
_2023-03-08_
- Updated dependencies

## 1.0.3
_2022-06-30_
- Updated dependencies

## 1.0.2
_2021-10-22_
- Updated dependencies

## 1.0.1
_2021-10-21_
- Updated dependencies

## 1.0
_2021-01-12_
- Updated dependency
- New methods added to `SqsConsumer`
    - `clone`
    - `close`
    - `getQueueUrl`

## 0.97
_2020-10-21_
- Updated dependency

## 0.96
_2020-02-25_
- Added `Message` method `getNumberOfTimesRead` which returns the number of times a message has been read from the queue but not deleted. This can be used to handle messages (for example logging and deleting) whos handling has failed X times after being read from queue.

## 0.95
_2020-01-29_
- Added `SqsProducer` property `guaranteedOrder`. 
    - If set to true, all messages have the same group id. As the queues are FIFO queues no message can be read until the first is deleted. 
    - If set to false, all messages have a unique group id. This allows the message after the first to be read if the first is invisible. But as the queues are FIFO it is guaranteed that there are no duplicate messages as opposed to standard queues. 

## 0.94
_2020-01-03_
- Updated dependencies

## 0.93
_2019-07-24_
- Updated dependencies

## 0.92
_2019-03-30_
- IS3File methods return IS3File instead of S3File

## 0.9
_2018-07-10_
- First version with `S3File` for more intuitive and concise S3 file operations.