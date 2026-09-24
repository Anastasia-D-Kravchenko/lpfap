package pl.edu.pjwstk.lpfap.repository;

import org.springframework.data.repository.ListCrudRepository;
import pl.edu.pjwstk.lpfap.model.AnalyticsReport;

public interface AnalyticsReportRepository extends ListCrudRepository<AnalyticsReport, Long> {
}
