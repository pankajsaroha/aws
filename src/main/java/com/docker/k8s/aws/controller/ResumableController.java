package com.docker.k8s.aws.controller;

import org.springframework.web.bind.annotation.*;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.time.Duration;
import java.util.List;

@RestController
@RequestMapping("/s3/multipart-upload")
public class ResumableController {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    public ResumableController(S3Client s3Client, S3Presigner s3Presigner) {
        this.s3Client = s3Client;
        this.s3Presigner = s3Presigner;
    }

    @PostMapping("/initiate")
    public String initiateMultipartUpload(@RequestParam String bucket, @RequestParam String key) {
        CreateMultipartUploadResponse response = s3Client.createMultipartUpload(
                CreateMultipartUploadRequest.builder()
                        .bucket(bucket)
                        .key(key)
                        .build()
        );
        return response.uploadId();
    }

    @GetMapping("/presign-url")
    public String generatePresignUrl(@RequestParam String bucket, @RequestParam String key,
                                     @RequestParam String uploadId, @RequestParam int partNumber) {
        UploadPartRequest uploadPartRequest = UploadPartRequest.builder()
                .bucket(bucket)
                .key(key)
                .uploadId(uploadId)
                .partNumber(partNumber)
                .build();

        return s3Presigner.presignUploadPart(r -> r.uploadPartRequest(uploadPartRequest)
                        .signatureDuration(Duration.ofMinutes(15)))
                .url().toString();
    }

    @PostMapping("/complete")
    public String completeMultipartUpload(@RequestParam String bucket, @RequestParam String key,
                                          @RequestParam String uploadId,
                                          @RequestBody List<CompletedPart> completedParts) {
        s3Client.completeMultipartUpload(
                CompleteMultipartUploadRequest.builder()
                        .bucket(bucket)
                        .key(key)
                        .uploadId(uploadId)
                        .multipartUpload(
                                CompletedMultipartUpload.builder()
                                        .parts(completedParts)
                                        .build()
                        )
                        .build()
        );
        return "Resumable upload completed for file: " + key;
    }
}
