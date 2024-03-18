package com.irina.updater.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@RequiredArgsConstructor
@Getter
@Setter
public class UpdateRequestDTO {
    private final String userVersion;
    private String latestVersion;
    private final String channel;
    private final String product;
}
