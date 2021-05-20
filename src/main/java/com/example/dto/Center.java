package com.example.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class Center {
    @JsonProperty(value = "center_id")
    Long centerId;
    String name;
    String address;
    @JsonProperty(value = "state_name")
    String stateName;
    @JsonProperty(value = "district_name")
    String districtName;
    @JsonProperty(value = "block_name")
    String blockName;
    Long pincode;
    @JsonProperty(value = "lat")
    Long latitude;
    @JsonProperty(value = "long")
    Long longitude;
    String from;
    String to;
    List<Session> sessions;
    @JsonProperty(value = "fee_type")
    String feeType;
}
