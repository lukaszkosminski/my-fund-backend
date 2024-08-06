package com.myfund.models;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
@Getter
@Setter
public class ApplicationDetails {

    private String version;

    private String buildDate;

}
