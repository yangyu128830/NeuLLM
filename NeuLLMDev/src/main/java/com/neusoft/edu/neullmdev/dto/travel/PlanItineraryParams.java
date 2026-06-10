package com.neusoft.edu.neullmdev.dto.travel;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PlanItineraryParams {
    @JsonAlias({"dest", "city", "destination", "location", "想去的地方", "目的地"})
    private String destination;

    @JsonAlias({"day", "dayCount", "numDays", "num", "几天", "多少天", "旅行天数"})
    private Integer days;

    @JsonAlias({"budgetAmount", "budgetNum", "fee", "预算金额", "花多少钱"})
    private Integer budget;

    @JsonAlias({"interest", "prefer", "偏好", "兴趣", "喜好", "style", "type"})
    private String interests;
}
