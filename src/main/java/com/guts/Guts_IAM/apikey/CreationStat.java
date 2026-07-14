package com.guts.Guts_IAM.apikey;

import java.time.LocalDateTime;


public class CreationStat {

    private String apiKey;

    private LocalDateTime createdOn;

    public CreationStat(ApiKey apiKey1){
        this.apiKey=apiKey1.getHashedApiKey();
        this.createdOn=apiKey1.getKeyCreatedOn();
    }


}
