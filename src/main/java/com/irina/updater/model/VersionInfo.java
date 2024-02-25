package com.irina.updater.model;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;

@AllArgsConstructor
@RequiredArgsConstructor
public class UpdateInfoData {
    private final String userVersion;
    private String latestVersion;
    private final String channel;
    private final String product;
}
