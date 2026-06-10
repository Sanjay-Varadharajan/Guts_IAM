package com.guts.Guts_IAM.auth.service;


import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

@Slf4j
@Service
public class BreachedPasswordService {

    private final Set<String> breachedPasswords = new HashSet<>();

    @PostConstruct
    public void loadPasswords() {

        try {

            ClassPathResource resource =
                    new ClassPathResource("common-password.txt");

            try (BufferedReader reader =
                         new BufferedReader(
                                 new InputStreamReader(
                                         resource.getInputStream(),
                                         StandardCharsets.UTF_8))) {

                String line;

                while ((line = reader.readLine()) != null) {

                    line = line.trim();

                    if (!line.isEmpty()) {
                        breachedPasswords.add(line.toLowerCase());
                    }
                }
            }

            log.info("Loaded {} breached passwords",
                    breachedPasswords.size());

        } catch (Exception e) {

            throw new IllegalStateException(
                    "Failed to load breached password file",
                    e
            );
        }
    }

    public boolean isBreached(String password) {

        if (password == null) {
            return false;
        }

        return breachedPasswords.contains(
                password.trim().toLowerCase()
        );
    }
}
