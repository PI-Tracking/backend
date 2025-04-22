package com.github.pi_tracking.backend.repository;

import com.github.pi_tracking.backend.entity.Upload;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UploadRepository extends JpaRepository<Upload, UUID> {

    Upload findByVideoId(String videoId);
}
