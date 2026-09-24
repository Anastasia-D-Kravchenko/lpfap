package pl.edu.pjwstk.lpfap.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "analytics_reports")
public class AnalyticsReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ReportType reportType;

    @Column(nullable = false)
    private LocalDateTime generatedAt = LocalDateTime.now();

    @Column
    private LocalDate periodStart;

    @Column
    private LocalDate periodEnd;

    @Column(columnDefinition = "CLOB")
    private String payload;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "generated_by_id", nullable = false)
    private User generatedBy;

    public AnalyticsReport() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public ReportType getReportType() { return reportType; }
    public void setReportType(ReportType reportType) { this.reportType = reportType; }
    public LocalDateTime getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(LocalDateTime generatedAt) { this.generatedAt = generatedAt; }
    public LocalDate getPeriodStart() { return periodStart; }
    public void setPeriodStart(LocalDate periodStart) { this.periodStart = periodStart; }
    public LocalDate getPeriodEnd() { return periodEnd; }
    public void setPeriodEnd(LocalDate periodEnd) { this.periodEnd = periodEnd; }
    public String getPayload() { return payload; }
    public void setPayload(String payload) { this.payload = payload; }
    public User getGeneratedBy() { return generatedBy; }
    public void setGeneratedBy(User generatedBy) { this.generatedBy = generatedBy; }
}
