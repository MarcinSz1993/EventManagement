package com.marcinsz.eventmanagementsystem.csv;

import com.marcinsz.eventmanagementsystem.exception.WrongFileException;

public interface CsvHeadersValidator {
    void validateHeaders(String[] csvHeadersFromFile) throws WrongFileException;
}
