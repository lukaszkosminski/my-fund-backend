package com.myfund.models.DTOs.mappers;

import com.myfund.models.ApplicationDetails;
import com.myfund.models.DTOs.ApplicationDetailsDTO;

public class ApplicationDetailsMapper {

    public static ApplicationDetailsDTO applicationDetailsToDTO(ApplicationDetails applicationDetails) {
        ApplicationDetailsDTO applicationDetailsDTO = new ApplicationDetailsDTO();
        applicationDetailsDTO.setVersion(applicationDetails.getVersion());
        applicationDetailsDTO.setBuildDate(applicationDetails.getBuildDate());
        return applicationDetailsDTO;
    }
}
