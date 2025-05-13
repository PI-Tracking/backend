package com.github.pi_tracking.backend.repository;

import com.github.pi_tracking.backend.entity.Report;

import org.checkerframework.common.util.count.report.qual.ReportCreation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ReportRepository extends JpaRepository<Report, UUID> {
}
