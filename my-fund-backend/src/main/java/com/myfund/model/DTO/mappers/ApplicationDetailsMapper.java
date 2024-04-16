package com.myfund.model.DTO.mappers;

import com.myfund.model.ApplicationDetails;
import com.myfund.model.DTO.ApplicationDetailsDTO;

public class ApplicationDetailsMapper {

    public static ApplicationDetailsDTO applicationDetailsToDTO(ApplicationDetails applicationDetails) {
        ApplicationDetailsDTO applicationDetailsDTO = new ApplicationDetailsDTO();
        applicationDetailsDTO.setVersion(applicationDetails.getVersion());
        return applicationDetailsDTO;
    }
}
