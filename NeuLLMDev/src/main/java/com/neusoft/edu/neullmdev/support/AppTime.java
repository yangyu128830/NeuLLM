package com.neusoft.edu.neullmdev.support;

import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * 应用统一时钟：业务语义按北京时间（Asia/Shanghai），避免 Railway 等容器默认 UTC 导致时间偏差。
 */
public final class AppTime {

    public static final String ZONE_ID = "Asia/Shanghai";
    public static final ZoneId ZONE = ZoneId.of(ZONE_ID);

    private AppTime() {
    }

    public static LocalDateTime now() {
        return LocalDateTime.now(ZONE);
    }
}
