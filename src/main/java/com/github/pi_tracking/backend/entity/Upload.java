package com.github.pi_tracking.backend.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Upload {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "camera_id", nullable = false)
    @JsonBackReference(value = "camera-uploads")
    private Camera camera;

    @ManyToOne
    @JoinColumn(name = "report_id", nullable = false)
    @JsonBackReference(value = "report-uploads")
    private Report report;
}
