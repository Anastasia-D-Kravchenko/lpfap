package pl.edu.pjwstk.lpfap.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "application_photos")
public class ApplicationPhoto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id", nullable = false)
    private Application application;

    @NotBlank
    @Column(nullable = false, length = 500)
    private String url;

    @Size(max = 255)
    @Column(length = 255)
    private String caption;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;

    public ApplicationPhoto() {}
    public ApplicationPhoto(Application application, String url, String caption, int sortOrder) {
        this.application = application; this.url = url; this.caption = caption; this.sortOrder = sortOrder;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Application getApplication() { return application; }
    public void setApplication(Application application) { this.application = application; }
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    public String getCaption() { return caption; }
    public void setCaption(String caption) { this.caption = caption; }
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
}
