package com.neusoft.edu.neullmdev.dto.travel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class LocationParams {
    private String origin;
    private String destination;
}
