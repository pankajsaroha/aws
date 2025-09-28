package com.docker.k8s.aws.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Configuration
public class AwsS3Config {

    @Value("${aws.region}")
    public String region;

    @Bean
    public Region awsRegion() {
        return Region.of(region);
    }

    @Bean
    public AwsBasicCredentials awsBasicCredentials(
            @Value("${aws.accessKeyId}") String accessKeyId,
            @Value("${aws.secretAccessKey}") String secretAccessKey) {
        return AwsBasicCredentials.create(accessKeyId, secretAccessKey);
    }

    @Bean
    public S3Client s3Client(Region region, AwsBasicCredentials credentials) {
        return S3Client.builder()
                .region(region)
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .build();
    }

    @Bean
    public S3Presigner s3Presigner(Region region, AwsBasicCredentials credentials) {
        return S3Presigner.builder()
                .region(region)
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .build();
    }
}
