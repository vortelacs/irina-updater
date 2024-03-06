package com.irina.updater.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@RequiredArgsConstructor
@Getter
@Setter
public class VersionInfo {
    private final String userVersion;
    private String latestVersion;
    private final String channel;
    private final String product;
}
