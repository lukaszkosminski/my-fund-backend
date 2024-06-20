package com.myfund.services.csv;

import com.myfund.models.User;
import org.springframework.web.multipart.MultipartFile;

public interface CsvParser {

    void parse(MultipartFile file, User user, Long budgetId);
}
