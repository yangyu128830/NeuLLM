package com.neusoft.edu.neullmdev.dto.travel;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class WeatherParams {
    @JsonAlias({"city", "place", "address"})
    private String location;
    @JsonAlias({"adCode", "cityCode"})
    private String adcode;
}
