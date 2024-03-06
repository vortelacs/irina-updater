package com.irina.updater.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class FileInfo {

    private String filePath;
    private byte[] checkSum;

}
