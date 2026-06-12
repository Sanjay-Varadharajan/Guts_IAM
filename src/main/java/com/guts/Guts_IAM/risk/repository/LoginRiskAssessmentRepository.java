package com.guts.Guts_IAM.risk.repository;

import com.guts.Guts_IAM.risk.model.LoginRiskAssessment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoginRiskAssessmentRepository extends JpaRepository<LoginRiskAssessment,Long> {
}
