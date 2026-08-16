package com.example.novaai.repository;

import com.example.novaai.entity.ModelUsage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ModelUsageRepository extends JpaRepository<ModelUsage, UUID> {
}
