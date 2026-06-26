package com.guts.Guts_IAM.devtest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TimeoutTest {

    @GetMapping("/test/timeout")
    public ResponseEntity<String> timeout() throws InterruptedException {

        Thread.sleep(10000);

        return ResponseEntity.ok("Done");
    }
}
