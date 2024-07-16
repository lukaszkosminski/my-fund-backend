package com.myfund.models.DTOs.mappers;

import com.myfund.models.ApplicationDetails;
import com.myfund.models.DTOs.ApplicationDetailsDTO;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ApplicationDetailsMapperTest {

    @Test
    void applicationDetailsToDTO_ShouldMapVersionCorrectly() {
        ApplicationDetails applicationDetails = new ApplicationDetails();
        applicationDetails.setVersion("1.0.0");
        applicationDetails.setBuildDate("2023-10-01T00:00:00");

        ApplicationDetailsDTO applicationDetailsDTO = ApplicationDetailsMapper.applicationDetailsToDTO(applicationDetails);

        assertEquals("1.0.0", applicationDetailsDTO.getVersion(), "Version should be mapped correctly");
    }

    @Test
    void applicationDetailsToDTO_ShouldMapBuildDateCorrectly() {
        ApplicationDetails applicationDetails = new ApplicationDetails();
        applicationDetails.setVersion("1.0.0");
        applicationDetails.setBuildDate("2023-10-01T00:00:00");

        ApplicationDetailsDTO applicationDetailsDTO = ApplicationDetailsMapper.applicationDetailsToDTO(applicationDetails);

        assertEquals("2023-10-01T00:00:00", applicationDetailsDTO.getBuildDate(), "Build date should be mapped correctly");
    }

    @Test
    void applicationDetailsToDTO_ShouldHandleNullVersion() {
        ApplicationDetails applicationDetails = new ApplicationDetails();
        applicationDetails.setVersion(null);
        applicationDetails.setBuildDate("2023-10-01T00:00:00");

        ApplicationDetailsDTO applicationDetailsDTO = ApplicationDetailsMapper.applicationDetailsToDTO(applicationDetails);

        assertNull(applicationDetailsDTO.getVersion(), "Version should be null");
    }

    @Test
    void applicationDetailsToDTO_ShouldHandleNullBuildDate() {
        ApplicationDetails applicationDetails = new ApplicationDetails();
        applicationDetails.setVersion("1.0.0");
        applicationDetails.setBuildDate(null);

        ApplicationDetailsDTO applicationDetailsDTO = ApplicationDetailsMapper.applicationDetailsToDTO(applicationDetails);

        assertNull(applicationDetailsDTO.getBuildDate(), "Build date should be null");
    }
}