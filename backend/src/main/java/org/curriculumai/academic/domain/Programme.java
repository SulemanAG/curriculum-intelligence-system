package org.curriculumai.academic.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "programmes", uniqueConstraints = {
        @UniqueConstraint(name = "uk_programme_dept_code", columnNames = {"department_id", "code"})
})
public class Programme {

    @Id
    @Column(length = 64)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @Column(nullable = false, length = 50)
    private String code;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(name = "degree_level", length = 50)
    private String degreeLevel;

    @Column(name = "duration_years")
    private Integer durationYears;

    @Column(name = "total_credits_required")
    private Integer totalCreditsRequired;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    public Programme() {}

    public Programme(String id, Department department, String code, String name, String degreeLevel, Integer durationYears, Integer totalCreditsRequired) {
        this.id = id;
        this.department = department;
        this.code = code;
        this.name = name;
        this.degreeLevel = degreeLevel;
        this.durationYears = durationYears;
        this.totalCreditsRequired = totalCreditsRequired;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public Department getDepartment() { return department; }
    public void setDepartment(Department department) { this.department = department; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDegreeLevel() { return degreeLevel; }
    public void setDegreeLevel(String degreeLevel) { this.degreeLevel = degreeLevel; }

    public Integer getDurationYears() { return durationYears; }
    public void setDurationYears(Integer durationYears) { this.durationYears = durationYears; }

    public Integer getTotalCreditsRequired() { return totalCreditsRequired; }
    public void setTotalCreditsRequired(Integer totalCreditsRequired) { this.totalCreditsRequired = totalCreditsRequired; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}

