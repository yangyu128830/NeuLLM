package com.neusoft.edu.neullmdev.dto.hotel;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;

@Data
public class HotelSearchParams {

    @JsonAlias({"city", "destination", "dest", "location", "目的地", "城市"})
    private String city;

    @JsonAlias({"from", "origin", "departure", "出发地", "从"})
    private String origin;

    @JsonAlias({"checkIn", "check_in", "arrivalDate", "startDate", "入住", "入住日期", "date"})
    private String checkInDate;

    @JsonAlias({"checkOut", "check_out", "departureDate", "endDate", "退房", "退房日期"})
    private String checkOutDate;

    @JsonAlias({"guests", "persons", "people", "guestCount", "人数", "几人", "旅行人数"})
    private Integer guests;

    @JsonAlias({"priceRange", "price", "budget", "预算", "价格", "档次"})
    private String priceRange;

    @JsonAlias({"keywords", "preference", "prefer", "偏好", "关键词"})
    private String keywords;
}
