package com.myfund.configs;

import com.myfund.services.BudgetService;
import com.myfund.services.csv.CsvParser;
import com.myfund.services.csv.MIlleniumCsvParser;
import com.myfund.services.csv.SantanderCsvParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
public class CsvParserConfig {
    private final BudgetService budgetService;

    @Autowired
    public CsvParserConfig(BudgetService budgetService) {
        this.budgetService = budgetService;
    }

    @Bean
    public Map<String, CsvParser> parserMap() {
        return Map.of("Santander", new SantanderCsvParser(budgetService),
                "Millenium", new MIlleniumCsvParser(budgetService));
    }
}

