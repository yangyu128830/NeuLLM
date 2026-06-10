package com.neusoft.edu.neullmdev.dto.communication;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class EmailParams {

    @NotBlank(message = "Recipient cannot be blank")
    private String recipient;
    private String subject;
    private String content;
}
