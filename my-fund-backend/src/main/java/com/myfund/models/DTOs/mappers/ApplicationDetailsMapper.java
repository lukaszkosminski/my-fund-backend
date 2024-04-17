package com.myfund.models.DTO.mappers;

import com.myfund.models.ApplicationDetails;
import com.myfund.models.DTO.ApplicationDetailsDTO;

public class ApplicationDetailsMapper {

    public static ApplicationDetailsDTO applicationDetailsToDTO(ApplicationDetails applicationDetails) {
        ApplicationDetailsDTO applicationDetailsDTO = new ApplicationDetailsDTO();
        applicationDetailsDTO.setVersion(applicationDetails.getVersion());
        applicationDetailsDTO.setBuildDate(applicationDetails.getBuildDate());
        return applicationDetailsDTO;
    }
}
