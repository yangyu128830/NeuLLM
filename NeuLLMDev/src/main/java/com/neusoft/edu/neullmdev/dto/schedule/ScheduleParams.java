package com.neusoft.edu.neullmdev.dto.schedule;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ScheduleParams {

    @JsonAlias({"title", "subject", "name"})
    private String title;

    @JsonAlias({"datetime", "dateTime", "time", "when"})
    private String datetime;

    private String location;
    private String description;
}
