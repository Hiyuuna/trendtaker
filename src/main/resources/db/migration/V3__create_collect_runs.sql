CREATE TABLE collect_runs
(
    id                    BIGINT        NOT NULL AUTO_INCREMENT,
    status                VARCHAR(20)   NOT NULL,
    started_at            DATETIME(6)   NOT NULL,
    finished_at           DATETIME(6),
    discovered            INT,
    tracked               INT,
    failed                INT,
    stopped_by_rate_limit BOOLEAN,
    error                 VARCHAR(1000),
    PRIMARY KEY (id),
    KEY idx_collect_runs_started_at (started_at)
);
