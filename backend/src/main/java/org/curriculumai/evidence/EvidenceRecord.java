package org.curriculumai.evidence;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "evidence_records")
public class EvidenceRecord {

    @Id
    @Column(length = 64)
    private String id;

    @Column(name = "proposal_id", length = 64)
    private String proposalId;

    @Column(name = "run_id", nullable = false, length = 64)
    private String runId;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String publisher;

    @Column(name = "publication_year")
    private Integer publicationYear;

    @Column(name = "clause_or_section")
    private String clauseOrSection;

    @Column(columnDefinition = "TEXT")
    private String url;

    @Column(name = "verified_claim", nullable = false, columnDefinition = "TEXT")
    private String verifiedClaim;

    @Column(name = "retrieval_timestamp", nullable = false)
    private Instant retrievalTimestamp = Instant.now();

    public EvidenceRecord() {
    }

    public EvidenceRecord(String id, String proposalId, String runId, String title, String publisher, Integer publicationYear, String clauseOrSection, String url, String verifiedClaim) {
        this.id = id;
        this.proposalId = proposalId;
        this.runId = runId;
        this.title = title;
        this.publisher = publisher;
        this.publicationYear = publicationYear;
        this.clauseOrSection = clauseOrSection;
        this.url = url;
        this.verifiedClaim = verifiedClaim;
        this.retrievalTimestamp = Instant.now();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getProposalId() {
        return proposalId;
    }

    public void setProposalId(String proposalId) {
        this.proposalId = proposalId;
    }

    public String getRunId() {
        return runId;
    }

    public void setRunId(String runId) {
        this.runId = runId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public Integer getPublicationYear() {
        return publicationYear;
    }

    public void setPublicationYear(Integer publicationYear) {
        this.publicationYear = publicationYear;
    }

    public String getClauseOrSection() {
        return clauseOrSection;
    }

    public void setClauseOrSection(String clauseOrSection) {
        this.clauseOrSection = clauseOrSection;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getVerifiedClaim() {
        return verifiedClaim;
    }

    public void setVerifiedClaim(String verifiedClaim) {
        this.verifiedClaim = verifiedClaim;
    }

    public Instant getRetrievalTimestamp() {
        return retrievalTimestamp;
    }

    public void setRetrievalTimestamp(Instant retrievalTimestamp) {
        this.retrievalTimestamp = retrievalTimestamp;
    }
}

