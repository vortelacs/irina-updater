package com.irina.updater.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
public class ProductRequest {
    private String name;
    private Map<String, FileEntry> files;
}
