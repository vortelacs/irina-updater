package com.irina.updater.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ProductInfoDTO {

    private String product;
    private String channel;
    private Long version;
}
