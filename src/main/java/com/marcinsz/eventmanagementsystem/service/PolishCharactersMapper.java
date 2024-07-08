package com.marcinsz.eventmanagementsystem.service;

import org.springframework.stereotype.Component;
@Component
public class PolishCharactersMapper {
    public String removePolishCharacters(String input) {
        if (input == null) {
            return null;
        }

        return input.replaceAll("[ąĄ]", "a")
                .replaceAll("[ęĘ]", "e")
                .replaceAll("[ćĆ]", "c")
                .replaceAll("[łŁ]", "l")
                .replaceAll("[ńŃ]", "n")
                .replaceAll("[óÓ]", "o")
                .replaceAll("[śŚ]", "s")
                .replaceAll("[źŹżŻ]", "z");
    }
}
