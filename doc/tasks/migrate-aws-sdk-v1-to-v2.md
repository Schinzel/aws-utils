# Task: Migrate AWS SDK from v1 to v2

## Background
The AWS SDK for Java v1 is deprecated and will reach end of support on December 31, 2025. This project currently uses AWS SDK v1 for S3 and SQS operations, which is causing deprecation warnings in dependent projects.

## Objective
Migrate the aws-utils project from AWS SDK v1 to AWS SDK v2 while maintaining backward compatibility for all public APIs.

## Current State
- **AWS SDK Version**: 1.12.773 (v1)
- **Main Classes Using SDK**:
  - `S3File.java` - File operations on S3
  - `TransferManagers.java` - Manages S3 transfer clients
  - `BucketCache.java` - Caches bucket existence checks
  - SQS-related classes in the `sqs` package

## Required Changes

### 1. Update Maven Dependencies
In `pom.xml`, replace:
```xml
<dependency>
    <groupId>com.amazonaws</groupId>
    <artifactId>aws-java-sdk-s3</artifactId>
    <version>1.12.773</version>
</dependency>
<dependency>
    <groupId>com.amazonaws</groupId>
    <artifactId>aws-java-sdk-sqs</artifactId>
    <version>1.12.773</version>
</dependency>
```

With:
```xml
<dependency>
    <groupId>software.amazon.awssdk</groupId>
    <artifactId>s3</artifactId>
    <version>2.29.39</version>
</dependency>
<dependency>
    <groupId>software.amazon.awssdk</groupId>
    <artifactId>s3-transfer-manager</artifactId>
    <version>2.29.39</version>
</dependency>
<dependency>
    <groupId>software.amazon.awssdk</groupId>
    <artifactId>sqs</artifactId>
    <version>2.29.39</version>
</dependency>
```

### 2. Key API Changes to Implement

#### Package Changes
- `com.amazonaws.*` → `software.amazon.awssdk.*`
- `com.amazonaws.regions.Regions` → `software.amazon.awssdk.regions.Region`
- `com.amazonaws.auth.BasicAWSCredentials` → `software.amazon.awssdk.auth.credentials.AwsBasicCredentials`
- `com.amazonaws.services.s3.AmazonS3` → `software.amazon.awssdk.services.s3.S3Client`
- `com.amazonaws.services.s3.transfer.TransferManager` → `software.amazon.awssdk.transfer.s3.S3TransferManager`

#### Client Creation Pattern
Old (v1):
```java
BasicAWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);
AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
    .withCredentials(new AWSStaticCredentialsProvider(credentials))
    .withRegion(region)
    .build();
```

New (v2):
```java
AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);
S3Client s3Client = S3Client.builder()
    .credentialsProvider(StaticCredentialsProvider.create(credentials))
    .region(Region.of(region.getName()))
    .build();
```

#### Exception Handling
- `AmazonServiceException` → `SdkServiceException`
- `AmazonClientException` → `SdkClientException`
- `AmazonS3Exception` → `S3Exception`

### 3. Specific Class Updates

#### TransferManagers.java
- Replace `TransferManager` with `S3TransferManager`
- Update the creation pattern to use SDK v2 builders
- The v2 transfer manager has different APIs for upload/download

#### S3File.java
- Update all S3 operations to use v2 APIs
- `putObject` instead of transfer manager uploads for simple operations
- Handle the new response types
- Update metadata handling (different API in v2)

#### BucketCache.java
- Update bucket existence checks to use v2 API
- Handle the new exception types

### 4. Testing Requirements

After making changes, verify:

1. **Compilation**:
   ```bash
   mvn clean compile -DskipTests
   ```
   Should complete without errors.

2. **Run all tests**:
   ```bash
   mvn test
   ```
   All existing tests should pass.

3. **Check for deprecation warnings**:
   The build should not show any AWS SDK v1 deprecation warnings.

## Important Considerations

1. **Backward Compatibility**: All public APIs must remain unchanged. Only internal implementation should change.

2. **Async Operations**: SDK v2 has built-in async support. Consider if we want to expose async APIs in the future, but for now, keep the synchronous behavior.

3. **Region Handling**: SDK v2 uses `Region` enum instead of `Regions`. Need to handle the conversion properly.

4. **Error Messages**: Ensure error messages remain helpful and similar to current ones.

5. **Performance**: The new SDK should perform similarly or better. No performance regression.

## Additional Tasks

### 5. Update All Dependencies
Update all dependencies to their latest stable versions:
- Check and update `basic-utils` to latest
- Update `lombok` to latest
- Update `junit` to latest (or consider migrating to JUnit 5)
- Update `assertj-core` to latest
- Update all Maven plugins to latest versions

### 6. Version Bump
In `pom.xml`, update the version:
```xml
<version>1.0.7</version>
```

### 7. Update README.md
Add release notes following the existing format. Add after the "# Releases" line and before "## 1.0.6":

```
## 1.0.7
_2025-06-27_
- Migrated from AWS SDK v1 to v2
- Updated all dependencies
```

Note: Follow the existing style - date in italics, followed by bullet points. Keep it very succinct.

## Success Criteria
- [ ] All compilation errors resolved
- [ ] All tests pass
- [ ] No AWS SDK v1 dependencies remain in pom.xml
- [ ] No deprecation warnings about AWS SDK v1
- [ ] Public API remains unchanged
- [ ] Code follows existing project style
- [ ] All dependencies updated to latest stable versions
- [ ] Version bumped to 1.0.7
- [ ] Release notes added to README.md

## Resources
- [AWS SDK v2 Migration Guide](https://docs.aws.amazon.com/sdk-for-java/latest/developer-guide/migration.html)
- [AWS SDK v2 S3 Examples](https://github.com/awsdocs/aws-doc-sdk-examples/tree/main/javav2/example_code/s3)
- [S3 Transfer Manager v2 Documentation](https://docs.aws.amazon.com/sdk-for-java/latest/developer-guide/transfer-manager.html)
