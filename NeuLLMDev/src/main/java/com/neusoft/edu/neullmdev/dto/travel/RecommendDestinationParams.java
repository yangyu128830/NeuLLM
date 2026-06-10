package com.neusoft.edu.neullmdev.dto.travel;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class RecommendDestinationParams {
    @JsonAlias({"preference", "interests", "pref", "preferences"})
    private String preferences;

    @JsonAlias({"cost", "priceLevel"})
    private String budget;

    @JsonAlias({"day", "tripDays"})
    private Integer days;
}
