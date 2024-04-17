package com.myfund.services;

import com.myfund.models.ApplicationDetails;
import com.myfund.models.DTOs.ApplicationDetailsDTO;
import com.myfund.models.DTOs.mappers.ApplicationDetailsMapper;
;
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
//
//    @Value("${app.version}")
//    private String appVersion;
    public ApplicationDetailsDTO getVersion() throws URISyntaxException {
        String buildVersion = System.getProperty("build.number");

        ApplicationDetails applicationDetails = new ApplicationDetails();
        LocalDateTime jarFileCreationDate = getJarFileCreationDate();
        applicationDetails.setBuildDate(jarFileCreationDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss")));
        applicationDetails.setVersion(buildVersion);
        return ApplicationDetailsMapper.applicationDetailsToDTO(applicationDetails);
    }

    private LocalDateTime getJarFileCreationDate() throws URISyntaxException {
        URL url = ApplicationDetailsService.class.getProtectionDomain().getCodeSource().getLocation();
        File jarFile = Paths.get(url.toURI()).toFile();
        long lastModified = jarFile.lastModified();
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(lastModified), ZoneId.systemDefault());
    }

    private String getJarFileName() throws URISyntaxException {
        URL url = ApplicationDetailsService.class.getProtectionDomain().getCodeSource().getLocation();
        File jarFile = Paths.get(url.toURI()).toFile();
        if (jarFile.isFile()) { // Sprawdzenie, czy ścieżka jest plikiem (plikiem JAR)
            return jarFile.getName(); // Zwraca nazwę pliku JAR
        } else {
            return "Not running from a JAR file"; // Alternatywna wiadomość lub logika
        }
}
//public String getJarFileName() {
//    return new java.io.File(ApplicationDetails.class.getProtectionDomain()
//            .getCodeSource()
//            .getLocation()
//            .getPath())
//            .getName();
//}
}
