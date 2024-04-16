package com.myfund.service;

import com.myfund.model.ApplicationDetails;
import com.myfund.model.DTO.ApplicationDetailsDTO;
import com.myfund.model.DTO.mappers.ApplicationDetailsMapper;
import org.springframework.stereotype.Service;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Service
public class ApplicationDetailsService {

    private LocalDateTime getJarFileCreationDate() throws URISyntaxException {
        URL url = ApplicationDetailsService.class.getProtectionDomain().getCodeSource().getLocation();
        File jarFile = Paths.get(url.toURI()).toFile();
        long lastModified = jarFile.lastModified();
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(lastModified), ZoneId.systemDefault());
    }

    public ApplicationDetailsDTO getVersion() throws URISyntaxException {
        ApplicationDetails applicationDetails = new ApplicationDetails();
        LocalDateTime jarFileCreationDate = getJarFileCreationDate();
        applicationDetails.setVersion("myfund_" + jarFileCreationDate.format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")));
        return ApplicationDetailsMapper.applicationDetailsToDTO(applicationDetails);
    }
}
