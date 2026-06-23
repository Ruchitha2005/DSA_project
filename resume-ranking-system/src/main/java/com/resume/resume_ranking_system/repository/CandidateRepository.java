package com.resume.resume_ranking_system.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.resume.resume_ranking_system.model.Candidate;

public interface CandidateRepository extends JpaRepository<Candidate, Long> {
}
