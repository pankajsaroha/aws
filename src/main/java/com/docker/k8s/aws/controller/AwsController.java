package com.docker.k8s.aws.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AwsController {

    @GetMapping("/deploy")
    public ResponseEntity<String> getResponse() {
        return ResponseEntity.ok("Deployed and running ...");
    }
}
