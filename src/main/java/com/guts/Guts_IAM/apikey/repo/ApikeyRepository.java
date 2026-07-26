package com.guts.Guts_IAM.apikey.repo;

import com.guts.Guts_IAM.apikey.model.ApiKey;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ApikeyRepository extends JpaRepository<ApiKey,Long> {

    ApiKey findByhashedApiKey(String compareHash);

    long countByUserId(long userId);

    Optional<ApiKey> findByHashedApiKey(String hash);

}
