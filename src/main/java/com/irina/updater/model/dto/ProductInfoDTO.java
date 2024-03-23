package com.irina.updater.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class ProductInfoDTO {

    private String product;
    private String channel;
    private Long version;
    private List<String> ignoredPaths;

    public ProductInfoDTO(String product, Long version) {
        this.product = product;
        this.version = version;
    }
}
