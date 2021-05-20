package com.example.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.Date;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class Session {
    @JsonProperty(value = "session_id")
    String sessionId;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    Date date;
    @JsonProperty(value = "available_capacity")
    Long availableCapacity;
    @JsonProperty(value = "min_age_limit")
    Long minAgeLimit;
    String vaccine;
    List<String> slots;
    @JsonProperty(value = "available_capacity_dose1")
    Long availableCapacityDose1;
    @JsonProperty(value = "available_capacity_dose2")
    Long availableCapacityDose2;
}
