package com.neusoft.edu.neullmdev.dto.communication;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SmsParams {

    @JsonAlias({"phoneNumber", "phone", "phone_number", "mobile", "mobile_number", "number"})
    private String phoneNumber;

    @JsonAlias({"message", "content", "text", "body"})
    private String message;
}
