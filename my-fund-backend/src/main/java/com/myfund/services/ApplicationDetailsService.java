package com.myfund.services;

import com.myfund.models.ApplicationDetails;
import com.myfund.models.DTOs.ApplicationDetailsDTO;
import com.myfund.models.DTOs.mappers.ApplicationDetailsMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;


@Service
public class ApplicationDetailsService {

    @Value("${app.version}")
    String appVer;

    @Value("${app.build.date}")
    private String buildDate;

    public ApplicationDetailsDTO getVersion() {
        ApplicationDetails applicationDetails = new ApplicationDetails();
        applicationDetails.setVersion(appVer);
        applicationDetails.setBuildDate(convertToSystemTimezone(buildDate));
        return ApplicationDetailsMapper.toDTO(applicationDetails);
    }

    public static String convertToSystemTimezone(String dateTime) {
        LocalDateTime localDateTime = LocalDateTime.parse(dateTime, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
        String formattedDateTime = localDateTime.atZone(ZoneId.of("UTC"))
                .withZoneSameInstant(ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
        return formattedDateTime;
    }
}
