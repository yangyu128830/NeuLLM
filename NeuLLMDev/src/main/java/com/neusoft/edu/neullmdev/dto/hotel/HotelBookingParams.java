package com.neusoft.edu.neullmdev.dto.hotel;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class HotelBookingParams {

    @JsonAlias({"id", "hotel_id", "hotelId"})
    private String hotelId;

    @JsonAlias({"name", "hotel_name", "hotelName", "酒店名", "酒店"})
    private String hotelName;

    @JsonAlias({"checkIn", "check_in", "入住日期"})
    private String checkInDate;

    @JsonAlias({"checkOut", "check_out", "退房日期"})
    private String checkOutDate;

    @JsonAlias({"guests", "persons", "people", "人数"})
    private Integer guests;

    @JsonAlias({"contact", "contactName", "name", "联系人", "姓名"})
    private String contactName;

    @JsonAlias({"phone", "mobile", "tel", "电话", "手机"})
    private String contactPhone;

    @JsonAlias({"email", "mail", "邮箱"})
    private String contactEmail;

    @JsonAlias({"notes", "remark", "special", "备注", "特殊需求"})
    private String specialRequests;
}
