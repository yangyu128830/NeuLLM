package com.neusoft.edu.neullmdev.dto.document;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;

@Data
public class WordWriteParams {
    @JsonAlias({"filename", "file_name", "name", "docName", "documentName"})
    private String fileName;
    private String title;
    private String content;
}
