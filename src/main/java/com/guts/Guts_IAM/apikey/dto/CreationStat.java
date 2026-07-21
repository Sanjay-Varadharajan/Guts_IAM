package com.guts.Guts_IAM.apikey.dto;

import com.guts.Guts_IAM.apikey.model.ApiKey;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CreationStat {

    private String apiKey;

    private LocalDateTime createdOn;

    public CreationStat(ApiKey apiKey1){
        this.apiKey=apiKey1.getHashedApiKey();
        this.createdOn=apiKey1.getKeyCreatedOn();
    }


}
