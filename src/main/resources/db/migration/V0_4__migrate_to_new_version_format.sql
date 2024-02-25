ALTER TABLE irina_updater.versionFile
    ADD COLUMN version BIGINT NOT NULL DEFAULT 0;


UPDATE irina_updater.versionFile
SET version =
        ((IF(majorVersion = 1, 1, 0) << 48) |
         (IF(minorVersion = 1, 1, 0) << 32) |
         (IF(revisionVersion = 1, 1, 0) << 16) |
         (IF(buildVersion = 1, 1, 0)));


ALTER TABLE versionFile
    DROP COLUMN majorVersion,
    DROP COLUMN minorVersion,
    DROP COLUMN revisionVersion,
    DROP COLUMN buildVersion;
