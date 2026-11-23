package org.curriculumai.academic.repository;

import org.curriculumai.academic.domain.CurriculumLifecycleStatus;
import org.curriculumai.academic.domain.CurriculumVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CurriculumVersionRepository extends JpaRepository<CurriculumVersion, String> {
    List<CurriculumVersion> findByProgrammeId(String programmeId);
    List<CurriculumVersion> findByProgrammeIdAndLifecycleStatus(String programmeId, CurriculumLifecycleStatus status);
    Optional<CurriculumVersion> findByProgrammeIdAndVersionCode(String programmeId, String versionCode);
    boolean existsByProgrammeIdAndVersionCode(String programmeId, String versionCode);
}

