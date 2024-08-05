package com.marcinsz.eventmanagementsystem.csv;

import com.opencsv.exceptions.CsvValidationException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.Reader;
import java.util.Set;

public interface CsvFileParser<T> {
    Set<T> parse(Reader reader, MultipartFile file) throws IOException, CsvValidationException;
}
