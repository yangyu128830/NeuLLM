package com.neusoft.edu.neullmdev.dto.communication;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class EmailLookupParams {

    @JsonAlias({"account", "email_name", "emailName", "name", "recipient"})
    private String account;
}
