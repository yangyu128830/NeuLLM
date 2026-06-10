package com.neusoft.edu.neullmdev.mapper.hotel;

import com.neusoft.edu.neullmdev.entity.hotel.HotelOrderEntity;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface HotelOrderMapper {

    @Insert("INSERT INTO hotel_order (" +
            "order_id, hotel_name, hotel_address, hotel_phone, " +
            "check_in_date, check_out_date, nights, guests, " +
            "contact_name, contact_phone, contact_email, special_requests, " +
            "status, total_amount, payment_deadline, cancellation_policy, map_link, " +
            "created_at, updated_at" +
            ") VALUES (" +
            "#{orderId}, #{hotelName}, #{hotelAddress}, #{hotelPhone}, " +
            "#{checkInDate}, #{checkOutDate}, #{nights}, #{guests}, " +
            "#{contactName}, #{contactPhone}, #{contactEmail}, #{specialRequests}, " +
            "#{status}, #{totalAmount}, #{paymentDeadline}, #{cancellationPolicy}, #{mapLink}, " +
            "#{createdAt}, #{updatedAt}" +
            ")")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(HotelOrderEntity order);

    @Select("SELECT id, order_id AS orderId, hotel_name AS hotelName, hotel_address AS hotelAddress, "
            + "hotel_phone AS hotelPhone, check_in_date AS checkInDate, check_out_date AS checkOutDate, "
            + "nights, guests, contact_name AS contactName, contact_phone AS contactPhone, "
            + "contact_email AS contactEmail, special_requests AS specialRequests, status, "
            + "total_amount AS totalAmount, payment_deadline AS paymentDeadline, "
            + "cancellation_policy AS cancellationPolicy, map_link AS mapLink, "
            + "created_at AS createdAt, updated_at AS updatedAt "
            + "FROM hotel_order WHERE order_id = #{orderId}")
    HotelOrderEntity selectByOrderId(String orderId);

    @Select("SELECT id, order_id AS orderId, hotel_name AS hotelName, hotel_address AS hotelAddress, "
            + "hotel_phone AS hotelPhone, check_in_date AS checkInDate, check_out_date AS checkOutDate, "
            + "nights, guests, contact_name AS contactName, contact_phone AS contactPhone, "
            + "contact_email AS contactEmail, special_requests AS specialRequests, status, "
            + "total_amount AS totalAmount, payment_deadline AS paymentDeadline, "
            + "cancellation_policy AS cancellationPolicy, map_link AS mapLink, "
            + "created_at AS createdAt, updated_at AS updatedAt "
            + "FROM hotel_order ORDER BY created_at DESC")
    List<HotelOrderEntity> selectAll();

    @Select("SELECT id, order_id AS orderId, hotel_name AS hotelName, hotel_address AS hotelAddress, "
            + "hotel_phone AS hotelPhone, check_in_date AS checkInDate, check_out_date AS checkOutDate, "
            + "nights, guests, contact_name AS contactName, contact_phone AS contactPhone, "
            + "contact_email AS contactEmail, special_requests AS specialRequests, status, "
            + "total_amount AS totalAmount, payment_deadline AS paymentDeadline, "
            + "cancellation_policy AS cancellationPolicy, map_link AS mapLink, "
            + "created_at AS createdAt, updated_at AS updatedAt "
            + "FROM hotel_order WHERE contact_phone = #{phone} ORDER BY created_at DESC")
    List<HotelOrderEntity> selectByPhone(String phone);

    @Update("UPDATE hotel_order SET status = #{status}, updated_at = #{updatedAt} WHERE order_id = #{orderId}")
    int updateStatus(@Param("orderId") String orderId, @Param("status") String status,
                     @Param("updatedAt") java.time.LocalDateTime updatedAt);

    @Delete("DELETE FROM hotel_order WHERE order_id = #{orderId}")
    int deleteByOrderId(String orderId);
}
