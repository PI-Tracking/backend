package com.github.pi_tracking.backend.repository;

import com.github.pi_tracking.backend.entity.Report;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ReportRepository extends JpaRepository<Report, UUID> {
}
