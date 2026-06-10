package com.neusoft.edu.neullmdev.entity.hotel;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** 对应表 hotel_order。 */
@Data
public class HotelOrderEntity {

    private Long id;
    private String orderId;
    private String hotelName;
    private String hotelAddress;
    private String hotelPhone;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private Integer nights;
    private Integer guests;
    private String contactName;
    private String contactPhone;
    private String contactEmail;
    private String specialRequests;
    private String status;
    private Double totalAmount;
    private LocalDateTime paymentDeadline;
    private String cancellationPolicy;
    private String mapLink;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
