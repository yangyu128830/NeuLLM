package com.neusoft.edu.neullmdev.service.travel;

import com.neusoft.edu.neullmdev.dto.travel.LocationParams;
import com.neusoft.edu.neullmdev.entity.travel.LocationEntity;
import com.neusoft.edu.neullmdev.mapper.travel.LocationMapper;
import com.neusoft.edu.neullmdev.model.mcp.ToolResult;
import com.neusoft.edu.neullmdev.tool.common.McpToolSupport;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

@Service
public class LocationService {

    private final LocationMapper locationMapper;

    public LocationService(LocationMapper locationMapper) {
        this.locationMapper = locationMapper;
    }

    public ToolResult lookup(LocationParams params) {
        return McpToolSupport.fromJsonString("get_location_info", buildJson("get_location_info", params));
    }

    public String buildJson(String functionName, LocationParams locationParams) {
        String origin = resolveCoord(locationParams != null ? locationParams.getOrigin() : null);
        String destination = resolveCoord(locationParams != null ? locationParams.getDestination() : null);
        if (origin == null && destination == null) {
            return new JSONObject()
                    .put("functionName", functionName)
                    .put("status", "error")
                    .put("message", "未找到匹配的地点，请检查 origin/destination 名称是否在 locations 表中")
                    .toString();
        }
        JSONObject resultJson = new JSONObject();
        resultJson.put("functionName", functionName);
        if (origin != null) {
            resultJson.put("origin", origin);
        }
        if (destination != null) {
            resultJson.put("destination", destination);
        }
        return resultJson.toString();
    }

    private String resolveCoord(String placeName) {
        if (placeName == null || placeName.isBlank()) {
            return null;
        }
        LocationEntity row = locationMapper.selectByName(placeName.trim());
        if (row == null || row.getLongitude() == null || row.getLatitude() == null) {
            return null;
        }
        return row.getLongitude() + "," + row.getLatitude();
    }
}
