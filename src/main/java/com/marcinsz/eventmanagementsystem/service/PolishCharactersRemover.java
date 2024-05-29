package com.marcinsz.eventmanagementsystem.service;

import org.springframework.stereotype.Component;
@Component
public class PolishCharactersRemover {
    public String removePolishCharacters(String input){
        input = input.replace("ą", "a").replace("Ą", "A");
        input = input.replace("ę", "e").replace("Ę", "E");
        input = input.replace("ć", "c").replace("Ć", "C");
        input = input.replace("ł", "l").replace("Ł", "L");
        input = input.replace("ń", "n").replace("Ń", "N");
        input = input.replace("ó", "o").replace("Ó", "O");
        input = input.replace("ś", "s").replace("Ś", "S");
        input = input.replace("ź", "z").replace("Ź", "Z");
        input = input.replace("ż", "z").replace("Ż", "Z");
        return input;
    }
}
