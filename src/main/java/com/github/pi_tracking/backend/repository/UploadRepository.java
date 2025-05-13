package com.github.pi_tracking.backend.repository;

import com.github.pi_tracking.backend.entity.Upload;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface UploadRepository extends JpaRepository<Upload, UUID> {

}
