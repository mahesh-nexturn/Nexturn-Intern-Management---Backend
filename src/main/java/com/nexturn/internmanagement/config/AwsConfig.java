package com.nexturn.internmanagement.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.sns.SnsClient;

/**
 * AWS clients are only created when configured, so local development works without AWS.
 * Credentials come from the default provider chain (ECS task role in AWS).
 */
@Configuration
public class AwsConfig {

    @Value("${app.aws.region:ap-south-1}")
    private String region;

    @Bean
    @ConditionalOnExpression("!'${app.s3.bucket:}'.isEmpty()")
    public S3Client s3Client() {
        return S3Client.builder().region(Region.of(region)).build();
    }

    @Bean
    @ConditionalOnExpression("!'${app.sns.topic-arn:}'.isEmpty()")
    public SnsClient snsClient() {
        return SnsClient.builder().region(Region.of(region)).build();
    }
}
