package com.guts.Guts_IAM.unit_testing.common;

import com.guts.Guts_IAM.common.exception.types.ResourceNotFoundException;
import com.guts.Guts_IAM.common.exception.types.UnauthorizedException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {


    @GetMapping("/test/not-found")
    public String notFound() {
        throw new ResourceNotFoundException("resource not found",
                "NOT_FOUND",
                HttpStatus.NOT_FOUND);
    }

    @GetMapping("/test/unauthorized")
    public String unauthorized() {
        throw new UnauthorizedException("No access");
    }

    @GetMapping("/test/generic")
    public String generic() {
        throw new RuntimeException("Boom crash");
    }
}