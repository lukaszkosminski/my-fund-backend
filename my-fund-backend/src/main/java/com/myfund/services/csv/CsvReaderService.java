package com.myfund.services.csv;

import com.myfund.models.BankName;
import com.myfund.models.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Service
public class CsvReaderService {

    private final Map<BankName, AbstractCsvParser> parserMap;

    @Autowired
    public CsvReaderService(Map<BankName, AbstractCsvParser> parserMap) {
        this.parserMap = parserMap;
    }

    public void parseFile(BankName bankName, MultipartFile file, User user, Long budgetId) {
        AbstractCsvParser parser = parserMap.get(bankName);
        if (parser == null) {
            throw new IllegalArgumentException("Unsupported bank: " + bankName);
        }
        parser.parse(file, user, budgetId);
    }
}

