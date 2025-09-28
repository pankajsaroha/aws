package com.docker.k8s.aws.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RestController
@RequestMapping("/s3")
public class MultipartController {

    private final S3Client s3Client;

    public MultipartController(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    @PostMapping("/multipart-upload")
    public String multipartUpload(@RequestParam("file") MultipartFile file, @RequestParam String bucket) throws IOException {
        String key = file.getOriginalFilename();

        //1. Initiate Upload
        CreateMultipartUploadResponse response = s3Client.createMultipartUpload(
                CreateMultipartUploadRequest.builder()
                        .bucket(bucket)
                        .key(key)
                        .build()
        );
        String uploadId = response.uploadId();

        //2. Split file into chunks (say 5 MB)
        byte[] bytes = file.getBytes();
        int partSize = 5 * 1024 * 1024; // 5 MB
        int partNumber = 1;

        ExecutorService executor = Executors.newFixedThreadPool(5); // tune pool size

        List<CompletableFuture<CompletedPart>> futures = new ArrayList<>();

        for (int offset = 0; offset < bytes.length; offset += partSize, partNumber++) {
            int finalPartNumber = partNumber;
            int size = Math.min(partSize, bytes.length - offset);
            byte[] partData = Arrays.copyOfRange(bytes, offset, offset + size);

            CompletableFuture<CompletedPart> future = CompletableFuture.supplyAsync(() -> {
                UploadPartResponse uploadPartResponse = s3Client.uploadPart(
                        UploadPartRequest.builder()
                                .bucket(bucket)
                                .key(key)
                                .uploadId(uploadId)
                                .partNumber(finalPartNumber)
                                .build(),
                        RequestBody.fromBytes(partData)
                );
                return CompletedPart.builder()
                        .partNumber(finalPartNumber)
                        .eTag(uploadPartResponse.eTag())
                        .build();
            }, executor);

            futures.add(future);
        }

        List<CompletedPart> completedParts = futures.stream()
                .map(CompletableFuture::join)
                .sorted(Comparator.comparingInt(CompletedPart::partNumber)) // ensure correct order
                .toList();

        executor.shutdown();

        //3. Complete Upload
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
        return "Multipart upload completed for file: " + key;
    }
}
