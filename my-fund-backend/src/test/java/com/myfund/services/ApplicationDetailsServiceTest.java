package com.myfund.services;

import com.myfund.models.ApplicationDetails;
import com.myfund.models.DTOs.ApplicationDetailsDTO;
import com.myfund.models.DTOs.mappers.ApplicationDetailsMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ApplicationDetailsServiceTest {

    @InjectMocks
    private ApplicationDetailsService applicationDetailsService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(applicationDetailsService, "appVer", "1.0.0");
        ReflectionTestUtils.setField(applicationDetailsService, "buildDate", "2023-10-01T00:00:00");
    }

    @Test
    void getVersion_ShouldReturnCorrectVersion() {
        ApplicationDetailsDTO applicationDetailsDTO = applicationDetailsService.getVersion();
        assertEquals("1.0.0", applicationDetailsDTO.getVersion(), "Version should be 1.0.0");
    }

    @Test
    void getVersion_ShouldReturnCorrectBuildDate() {
        ApplicationDetailsDTO applicationDetailsDTO = applicationDetailsService.getVersion();
        String expectedDate = ApplicationDetailsService.convertToSystemTimezone("2023-10-01T00:00:00");
        assertEquals(expectedDate, applicationDetailsDTO.getBuildDate(), "Build date should be converted to system timezone");
    }

    @Test
    void convertToSystemTimezone_ShouldConvertCorrectly() {
        String utcDateTime = "2023-10-01T00:00:00";
        String systemDateTime = ApplicationDetailsService.convertToSystemTimezone(utcDateTime);
        LocalDateTime localDateTime = LocalDateTime.parse(systemDateTime, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
        assertNotNull(localDateTime, "Converted date time should not be null");
    }

    @Test
    void convertToSystemTimezone_ShouldHandleDifferentTimezones() {
        String utcDateTime = "2023-10-01T00:00:00";
        String systemDateTime = ApplicationDetailsService.convertToSystemTimezone(utcDateTime);
        LocalDateTime localDateTime = LocalDateTime.parse(systemDateTime, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
        assertEquals(localDateTime.atZone(ZoneId.systemDefault()).getOffset(), ZoneId.systemDefault().getRules().getOffset(localDateTime), "Converted date time should be in system timezone");
    }

    @Test
    void getVersion_ShouldMapApplicationDetailsToDTO() {
        ApplicationDetailsDTO applicationDetailsDTO = applicationDetailsService.getVersion();
        assertNotNull(applicationDetailsDTO, "ApplicationDetailsDTO should not be null");
        assertEquals("1.0.0", applicationDetailsDTO.getVersion(), "Version should be mapped correctly");
        String expectedDate = ApplicationDetailsService.convertToSystemTimezone("2023-10-01T00:00:00");
        assertEquals(expectedDate, applicationDetailsDTO.getBuildDate(), "Build date should be mapped correctly");
    }
}