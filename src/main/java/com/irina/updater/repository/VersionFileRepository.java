package com.irina.updater.repository;

import com.irina.updater.model.VersionFile;
import com.irina.updater.model.dto.FileInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VersionFileRepository extends JpaRepository<VersionFile, String> {

    @Query("SELECT MAX(v.version) FROM VersionFile v WHERE v.channel = ?1 AND v.product = ?2")
    String findProductLatestVersion(String channel, String product);

    @Query("SELECT NEW com.irina.updater.model.dto.FileInfo(lf.filePath, fi.fileHash) " +
            "FROM VersionFile lf " +
            "LEFT JOIN FileIndex fi ON lf.fileIndex.id = fi.id " +
            "WHERE lf.version = :version " +
            "AND lf.product = :product " +
            "AND lf.channel = :channel")
    List<FileInfo> getFileInfoList(Long version, String channel, String product);


    @Query("SELECT v.fileIndex.id FROM VersionFile v WHERE v.filePath = :filePath")
    Integer getFileIdByFilePath(@Param("filePath") String filePath);


}
