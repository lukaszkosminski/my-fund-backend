package com.myfund.services.csv;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Service
public class CsvReaderService {

    private final Map<String, CsvParser> parserMap;

    @Autowired
    public CsvReaderService(Map<String, CsvParser> parserMap) {
        this.parserMap = parserMap;
    }

    public void processCsv(String bankName, MultipartFile file) {
        CsvParser parser = parserMap.get(bankName);
        if (parser == null) {
            throw new IllegalArgumentException("Unsupported bank: " + bankName);
        }
        parser.parseCsv(file);
    }
}

