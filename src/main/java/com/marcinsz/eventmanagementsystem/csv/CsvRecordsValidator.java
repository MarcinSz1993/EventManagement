package com.marcinsz.eventmanagementsystem.csv;

import com.marcinsz.eventmanagementsystem.exception.WrongFileException;

public interface CsvRecordsValidator<T> {
    void validateRecords(T record) throws WrongFileException;
}
