ALTER TABLE applications ALTER COLUMN status SET DATA TYPE VARCHAR(20);

CREATE TABLE animal_photos (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    animal_id  BIGINT       NOT NULL REFERENCES animals(id) ON DELETE CASCADE,
    url        VARCHAR(500) NOT NULL,
    sort_order INT          NOT NULL DEFAULT 0
);
CREATE INDEX idx_animal_photos_animal ON animal_photos(animal_id);

CREATE TABLE application_photos (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    application_id  BIGINT       NOT NULL REFERENCES applications(id) ON DELETE CASCADE,
    url             VARCHAR(500) NOT NULL,
    caption         VARCHAR(255),
    sort_order      INT          NOT NULL DEFAULT 0
);
CREATE INDEX idx_application_photos_app ON application_photos(application_id);
