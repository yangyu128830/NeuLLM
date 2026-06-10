package com.neusoft.edu.neullmdev.entity.travel;

import lombok.Data;

/** 对应表 locations。 */
@Data
public class LocationEntity {
    private String locationId;
    private String name;
    private String longitude;
    private String latitude;
}
