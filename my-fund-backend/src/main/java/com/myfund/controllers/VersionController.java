package com.myfund.controllers;

import com.myfund.models.ApplicationDetails;
import com.myfund.models.DTOs.ApplicationDetailsDTO;
import com.myfund.services.ApplicationDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URISyntaxException;

@RestController
@RequestMapping("/api/")
public class VersionController {

    private final ApplicationDetailsService applicationDetailsService;
    @Autowired
    public VersionController(ApplicationDetailsService applicationDetailsService) {
        this.applicationDetailsService = applicationDetailsService;
    }

    @GetMapping("/version")
    public ResponseEntity<ApplicationDetailsDTO> getVersion() throws URISyntaxException {
        return new ResponseEntity<>(applicationDetailsService.getVersion(), HttpStatus.OK);
    }
}

