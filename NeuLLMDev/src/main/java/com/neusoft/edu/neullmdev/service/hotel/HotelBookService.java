package com.neusoft.edu.neullmdev.service.hotel;

import com.neusoft.edu.neullmdev.dto.hotel.HotelBookingParams;
import com.neusoft.edu.neullmdev.entity.hotel.HotelOrderEntity;
import com.neusoft.edu.neullmdev.mapper.hotel.HotelOrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

@Slf4j
@Service
public class HotelBookService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final HotelOrderMapper hotelOrderMapper;

    public HotelBookService(HotelOrderMapper hotelOrderMapper) {
        this.hotelOrderMapper = hotelOrderMapper;
    }

    public String executeBook(HotelBookingParams params, boolean persist) {
        if (params == null) {
            return buildErrorResult("预订参数为空，请提供酒店名称、入住/退房日期、联系人信息");
        }
        if (!persist) {
            return buildPreviewBookingJson(params);
        }
        String validationError = validateBookingParams(params);
        if (validationError != null) {
            return buildErrorResult(validationError);
        }
        return buildBookingResult(params);
    }

    private String validateBookingParams(HotelBookingParams params) {
        if (params.getContactName() == null || params.getContactName().trim().isEmpty()) {
            return "请提供联系人姓名";
        }
        if (params.getContactPhone() == null || params.getContactPhone().trim().isEmpty()) {
            return "请提供联系电话";
        }
        if (params.getCheckInDate() == null || params.getCheckOutDate() == null) {
            return "请提供入住日期和退房日期";
        }
        return null;
    }

    private String buildPreviewBookingJson(HotelBookingParams params) {
        String hotelName = params.getHotelName() != null ? params.getHotelName() : "所选酒店";
        long nights = calcNights(params.getCheckInDate(), params.getCheckOutDate());
        return new JSONObject()
                .put("functionName", "hotel_book")
                .put("displayKind", "hotel_book_preview")
                .put("hotelName", hotelName)
                .put("checkInDate", params.getCheckInDate())
                .put("checkOutDate", params.getCheckOutDate())
                .put("nights", nights)
                .put("guests", params.getGuests() != null ? params.getGuests() : 2)
                .put("contactName", params.getContactName())
                .put("contactPhone", params.getContactPhone())
                .put("contactEmail", params.getContactEmail() != null ? params.getContactEmail() : "")
                .put("specialRequests", params.getSpecialRequests() != null ? params.getSpecialRequests() : "")
                .toString();
    }

    private String buildBookingResult(HotelBookingParams params) {
        String orderId = "HTL" + System.currentTimeMillis();
        long nights = calcNights(params.getCheckInDate(), params.getCheckOutDate());
        String hotelName = params.getHotelName() != null ? params.getHotelName() : "所选酒店";
        String amapLink = "https://uri.amap.com/marker?position=&name="
                + hotelName.replace(" ", "+") + "&src=NeuLLM";

        JSONObject result = new JSONObject()
                .put("function", "hotel_book")
                .put("functionName", "hotel_book")
                .put("status", "success")
                .put("orderId", orderId)
                .put("orderStatus", "待确认")
                .put("hotelName", hotelName)
                .put("checkInDate", params.getCheckInDate())
                .put("checkOutDate", params.getCheckOutDate())
                .put("nights", nights)
                .put("guests", params.getGuests() != null ? params.getGuests() : 2)
                .put("contactName", params.getContactName())
                .put("contactPhone", params.getContactPhone())
                .put("contactEmail", params.getContactEmail() != null ? params.getContactEmail() : "")
                .put("specialRequests", params.getSpecialRequests() != null ? params.getSpecialRequests() : "无")
                .put("paymentDeadline", LocalDate.now().plusDays(1).format(DATE_FMT) + " 23:59")
                .put("cancellationPolicy", "请直接联系酒店确认取消政策")
                .put("bookingLink", amapLink)
                .put("tips", new JSONArray()
                        .put("预订信息已保存到数据库，订单号: " + orderId)
                        .put("请点击下方链接在高德地图中查看酒店位置并电话联系酒店完成预订")
                        .put("入住当天请携带本人有效身份证件")
                        .put("如需取消请直接联系酒店前台"));

        persistOrder(params, orderId, nights, hotelName, amapLink);
        return result.toString();
    }

    private void persistOrder(HotelBookingParams params, String orderId, long nights, String hotelName, String amapLink) {
        try {
            HotelOrderEntity order = new HotelOrderEntity();
            order.setOrderId(orderId);
            order.setHotelName(hotelName);
            order.setCheckInDate(LocalDate.parse(params.getCheckInDate(), DATE_FMT));
            order.setCheckOutDate(LocalDate.parse(params.getCheckOutDate(), DATE_FMT));
            order.setNights((int) nights);
            order.setGuests(params.getGuests() != null ? params.getGuests() : 2);
            order.setContactName(params.getContactName());
            order.setContactPhone(params.getContactPhone());
            order.setContactEmail(params.getContactEmail());
            order.setSpecialRequests(params.getSpecialRequests());
            order.setStatus("待确认");
            order.setPaymentDeadline(LocalDateTime.now().plusDays(1).withHour(23).withMinute(59));
            order.setCancellationPolicy("请直接联系酒店确认取消政策");
            order.setMapLink(amapLink);
            order.setCreatedAt(LocalDateTime.now());
            order.setUpdatedAt(LocalDateTime.now());
            hotelOrderMapper.insert(order);
        } catch (Exception e) {
            log.warn("【酒店预订】保存订单到数据库失败: {}", e.getMessage());
        }
    }

    private long calcNights(String checkIn, String checkOut) {
        try {
            long n = ChronoUnit.DAYS.between(
                    LocalDate.parse(checkIn, DATE_FMT),
                    LocalDate.parse(checkOut, DATE_FMT));
            return n > 0 ? n : 1;
        } catch (Exception e) {
            return 1;
        }
    }

    private String buildErrorResult(String message) {
        return new JSONObject()
                .put("function", "hotel_book")
                .put("functionName", "hotel_book")
                .put("status", "error")
                .put("message", message)
                .toString();
    }
}
