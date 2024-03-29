package com.irina.updater.model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@Entity
@NoArgsConstructor
@Table(name = "fileindex")
public class FileIndex {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private int id;
    @Column(name = "fileHash", columnDefinition = "BINARY(64)")
    private byte[] fileHash;
    @Column(name = "uploadDate")
    @Temporal(TemporalType.TIMESTAMP)
    @CreationTimestamp
    private Date uploadDate;


    public FileIndex(byte[] fileHash){
        this.fileHash = fileHash;
    }


}
