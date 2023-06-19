package com.pallycon.sample.model;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record LicenseResponse(String expireDate,
                              String persistent,
                              String licenseDuration,
                              String rentalDuration,
                              String playbackDuration,
                              boolean renewal,
                              long renewalDuration,
                              String license,
                              DeviceInfo deviceInfo) {

}
