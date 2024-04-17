package com.myfund.services;

import com.myfund.models.ApplicationDetails;
import com.myfund.models.DTOs.ApplicationDetailsDTO;
import com.myfund.models.DTOs.mappers.ApplicationDetailsMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URISyntaxException;

;

@Service
public class ApplicationDetailsService {
    //
    @Value("${app.version}")
    String appVer;

    @Value("${app.build.date}")
    private String buildDate;

    public ApplicationDetailsDTO getVersion() {
        ApplicationDetails applicationDetails = new ApplicationDetails();
        applicationDetails.setVersion(appVer);
        applicationDetails.setBuildDate(buildDate);
        return ApplicationDetailsMapper.applicationDetailsToDTO(applicationDetails);
    }
}
