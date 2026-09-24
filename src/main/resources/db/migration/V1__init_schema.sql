CREATE TABLE shelters (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    name           VARCHAR(255)  NOT NULL,
    address        VARCHAR(500),
    contact_email  VARCHAR(255)  NOT NULL,
    contact_phone  VARCHAR(30)   NOT NULL
);

CREATE TABLE users (
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    email             VARCHAR(255) NOT NULL UNIQUE,
    password_hash     VARCHAR(255) NOT NULL,
    full_name         VARCHAR(255) NOT NULL,
    phone             VARCHAR(30),
    role              VARCHAR(10)  NOT NULL DEFAULT 'USER',
    registration_date DATE         NOT NULL DEFAULT CURRENT_DATE,
    shelter_id        BIGINT REFERENCES shelters(id)
);

CREATE TABLE animals (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    name           VARCHAR(100) NOT NULL,
    species        VARCHAR(20)  NOT NULL,
    breed          VARCHAR(100),
    age            INT          NOT NULL CHECK (age >= 0),
    sex            VARCHAR(10)  NOT NULL,
    description    CLOB,
    medical_status CLOB,
    photo_url      VARCHAR(500),
    status         VARCHAR(20)  NOT NULL DEFAULT 'AVAILABLE',
    intake_date    DATE         NOT NULL DEFAULT CURRENT_DATE,
    shelter_id     BIGINT       NOT NULL REFERENCES shelters(id)
);

CREATE TABLE applications (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    type             VARCHAR(10)  NOT NULL,
    status           VARCHAR(15)  NOT NULL DEFAULT 'PENDING',
    motivation_text  CLOB         NOT NULL,
    submission_date  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    decision_date    TIMESTAMP,
    applicant_id     BIGINT       NOT NULL REFERENCES users(id),
    animal_id        BIGINT       NOT NULL REFERENCES animals(id)
);

CREATE TABLE analytics_reports (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    report_type     VARCHAR(30)  NOT NULL,
    generated_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    period_start    DATE,
    period_end      DATE,
    payload         CLOB,
    generated_by_id BIGINT       NOT NULL REFERENCES users(id)
);

CREATE INDEX idx_animals_status            ON animals(status);
CREATE INDEX idx_applications_applicant    ON applications(applicant_id);
CREATE INDEX idx_applications_animal       ON applications(animal_id);
CREATE INDEX idx_applications_status_date  ON applications(status, submission_date);
