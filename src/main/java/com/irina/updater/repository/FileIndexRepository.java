package com.irina.updater.repository;

import com.irina.updater.model.FileIndex;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FileIndexRepository extends JpaRepository<FileIndex, Integer> {
}
