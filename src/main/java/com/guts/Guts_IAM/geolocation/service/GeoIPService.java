package com.guts.Guts_IAM.geolocation.service;

import com.guts.Guts_IAM.geolocation.dto.GeoLocation;
import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.model.CityResponse;
import jakarta.annotation.PostConstruct;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.File;
import java.net.InetAddress;

@Service
public class GeoIPService {

    private DatabaseReader dbReader;

    @PostConstruct
    public void init() {
        try {
            var inputStream = new ClassPathResource("geoip/GeoLite2-City.mmdb").getInputStream();
            dbReader = new DatabaseReader.Builder(inputStream).build();
        } catch (Exception e) {
            throw new RuntimeException("Failed to load GeoIP database", e);
        }
    }


    public GeoLocation getLocation(String ip) {

        try {
            if (ip == null || ip.isEmpty()) {
                return new GeoLocation("UNKNOWN", "UNKNOWN", null, null);
            }

            InetAddress ipAddress = InetAddress.getByName(ip);
            CityResponse response = dbReader.city(ipAddress);

            String country = response.getCountry().getName();
            String city = response.getCity().getName();

            Double lat = response.getLocation().getLatitude();
            Double lon = response.getLocation().getLongitude();

            if (country == null) country = "UNKNOWN";
            if (city == null) city = "UNKNOWN";

            return new GeoLocation(country, city, lat, lon);

        } catch (Exception e) {
            return new GeoLocation("UNKNOWN", "UNKNOWN", null, null);
        }
    }
}