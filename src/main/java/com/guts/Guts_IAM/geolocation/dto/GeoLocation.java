package com.guts.Guts_IAM.geolocation.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class GeoLocation {

    private String country;
    private String city;
    private Double latitude;
    private Double longitude;

}
