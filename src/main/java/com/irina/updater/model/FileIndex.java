package com.irina.updater.model;


import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Table
@Getter
@Setter
@AllArgsConstructor
public class VersionFiles {

    @Id
    private int id;

    private long fileHash;

    @Temporal(TemporalType.TIMESTAMP)
    private Date uploadDate;

}
