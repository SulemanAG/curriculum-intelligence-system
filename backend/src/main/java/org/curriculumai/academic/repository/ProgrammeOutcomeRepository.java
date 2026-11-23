package org.curriculumai.academic.repository;

import org.curriculumai.academic.domain.ProgrammeOutcome;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProgrammeOutcomeRepository extends JpaRepository<ProgrammeOutcome, String> {
    List<ProgrammeOutcome> findByProgrammeIdOrderByCodeAsc(String programmeId);
    Optional<ProgrammeOutcome> findByProgrammeIdAndCode(String programmeId, String code);
    boolean existsByProgrammeIdAndCode(String programmeId, String code);
}

