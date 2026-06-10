package com.neusoft.edu.neullmdev.mapper.travel;

import com.neusoft.edu.neullmdev.entity.travel.LocationEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface LocationMapper {

    @Select("SELECT location_id AS locationId, name, longitude, latitude "
            + "FROM locations WHERE name = #{name} LIMIT 1")
    LocationEntity selectByName(@Param("name") String name);
}
