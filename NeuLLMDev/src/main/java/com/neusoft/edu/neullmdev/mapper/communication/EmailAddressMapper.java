package com.neusoft.edu.neullmdev.mapper.communication;

import com.neusoft.edu.neullmdev.entity.communication.EmailAddressEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface EmailAddressMapper {

    @Select("SELECT email_id AS emailId, email_name AS emailName, address "
            + "FROM emailaddress WHERE email_name = #{name} LIMIT 1")
    EmailAddressEntity selectByEmailName(@Param("name") String name);
}
