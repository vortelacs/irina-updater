package com.irina.updater.model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@Setter
@Getter
@Entity
@NoArgsConstructor
@Table(name = "versionfiles")
public class VersionFile {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private int id;
    @Column(name = "filePath")
    private String filePath;
    private Long version;
    private String product;
    private String channel;
    @ManyToOne
    @JoinColumn(name = "fileId")
    private FileIndex fileIndex;


    public VersionFile(String filePath, String product, String channel, Long version){
        this.filePath = filePath;
        this.product = product;
        this.channel = channel;
        this.version = version;
    }

}
