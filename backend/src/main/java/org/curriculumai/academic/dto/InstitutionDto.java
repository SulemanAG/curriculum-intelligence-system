package org.curriculumai.academic.dto;

import jakarta.validation.constraints.NotBlank;
import org.curriculumai.academic.domain.Institution;

import java.time.Instant;

public class InstitutionDto {

    public record CreateInstitutionRequest(
            @NotBlank(message = "Institution code is mandatory")
            String code,
            @NotBlank(message = "Institution name is mandatory")
            String name,
            String location,
            String country,
            String website,
            String metadataJson
    ) {}

    public record InstitutionResponse(
            String id,
            String code,
            String name,
            String location,
            String country,
            String website,
            String metadataJson,
            Instant createdAt,
            Instant updatedAt
    ) {
        public static InstitutionResponse fromEntity(Institution inst) {
            return new InstitutionResponse(
                    inst.getId(),
                    inst.getCode(),
                    inst.getName(),
                    inst.getLocation(),
                    inst.getCountry(),
                    inst.getWebsite(),
                    inst.getMetadataJson(),
                    inst.getCreatedAt(),
                    inst.getUpdatedAt()
            );
        }
    }
}

