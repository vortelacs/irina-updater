package com.irina.updater.model;


import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Setter
@Getter
@Table
public class FileI {

    @Id
    private int id;
    private int field;
    private String hash;
    private String filePath;
    private String product;
    private String channel;
}
