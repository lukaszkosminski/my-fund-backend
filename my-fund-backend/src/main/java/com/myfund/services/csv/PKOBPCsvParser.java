package com.myfund.services.csv;

import com.myfund.models.User;
import org.springframework.web.multipart.MultipartFile;

public class PKOBPCsvParser implements CsvParser {
    @Override
    public void parse(MultipartFile file, User user, Long budgetId) {
    }
}
