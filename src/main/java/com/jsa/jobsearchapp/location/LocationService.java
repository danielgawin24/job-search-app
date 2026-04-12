package com.jsa.jobsearchapp.location;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LocationService {

    public List<String> getAllPolishVoivodeships() {
        return List.of(
                "Dolnośląskie", "Kujawsko-pomorskie", "Lubelskie", "Lubuskie",
                "Łódzkie", "Małopolskie", "Mazowieckie", "Opolskie",
                "Podkarpackie", "Podlaskie", "Pomorskie", "Śląskie",
                "Świętokrzyskie", "Warmińsko-mazurskie", "Wielkopolskie", "Zachodniopomorskie"
        );
    }
}
