package com.myfund.configs;

import com.myfund.models.BankName;
import com.myfund.services.BudgetService;
import com.myfund.services.csv.AbstractCsvParser;
import com.myfund.services.csv.MIlleniumCsvParser;
import com.myfund.services.csv.SantanderCsvParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class CsvParserConfig {
    private final BudgetService budgetService;

    @Autowired
    public CsvParserConfig(BudgetService budgetService) {
        this.budgetService = budgetService;
    }

    @Bean
    public Map<BankName, AbstractCsvParser> parserMap() {
        Map<BankName, AbstractCsvParser> map = new HashMap<>();
        map.put(BankName.MILLENIUM, new MIlleniumCsvParser(budgetService));
        map.put(BankName.SANTANDER, new SantanderCsvParser(budgetService));
        return map;
    }
}

