package com.myfund.services.csv;

import com.myfund.models.BankName;
import com.myfund.models.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CsvReaderServiceTest  {

    @Mock
    private AbstractCsvParser mockParser;

    @Mock
    private MultipartFile mockFile;

    private CsvReaderService csvReaderService;
    private Map<BankName, AbstractCsvParser> parserMap;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        parserMap = new HashMap<>();
        parserMap.put(BankName.MILLENIUM, mockParser);
        csvReaderService = new CsvReaderService(parserMap);
    }

    @Test
    void parseFile_WithValidBankNameAndNullBudgetId() {
        User user = new User();
        BankName bankName = BankName.MILLENIUM;

        csvReaderService.parseFile(bankName, mockFile, user, null);

        verify(mockParser, times(1)).parse(mockFile, user, null);
    }

    @Test
    void parseFile_WithUnsupportedBankNameAndNullBudgetId() {
        User user = new User();
        BankName unsupportedBankName = BankName.SANTANDER;

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            csvReaderService.parseFile(unsupportedBankName, mockFile, user, null);
        });

        assertEquals("Unsupported bank: " + unsupportedBankName, exception.getMessage());
        verify(mockParser, never()).parse(any(), any(), any());
    }

    @Test
    void parseFile_WithValidBankNameNullBudgetIdAndNullFile() {
        User user = new User();
        BankName bankName = BankName.MILLENIUM;

        csvReaderService.parseFile(bankName, null, user, null);

        verify(mockParser, times(1)).parse(null, user, null);
    }

    @Test
    void parseFile_WithValidBankNameNullBudgetIdAndNullUser() {
        BankName bankName = BankName.MILLENIUM;

        csvReaderService.parseFile(bankName, mockFile, null, null);

        verify(mockParser, times(1)).parse(mockFile, null, null);
    }

    @Test
    void parseFile_WithValidBankNameNullBudgetIdAndValidFileAndUser() {
        User user = new User();
        BankName bankName = BankName.MILLENIUM;

        csvReaderService.parseFile(bankName, mockFile, user, null);

        verify(mockParser, times(1)).parse(mockFile, user, null);
    }
}