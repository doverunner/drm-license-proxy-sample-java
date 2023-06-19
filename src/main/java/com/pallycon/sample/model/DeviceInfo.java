package com.pallycon.sample.model;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record DeviceInfo(String deviceId,
                         String deviceModel,
                         String osVersion,
                         String sessionId,
                         boolean isChromecdm) {

}
