package com.myfund.services.csv;

import org.springframework.web.multipart.MultipartFile;

public interface CsvParser {

    void parseCsv(MultipartFile file);
}
