ALTER TABLE irina_updater.versionFiles
    ADD COLUMN version BIGINT NOT NULL DEFAULT 0;


UPDATE irina_updater.versionFiles
SET version =
        ((majorVersion * POWER(2, 48)) +
         (minorVersion * POWER(2, 32)) +
         (revisionVersion * POWER(2, 16)) +
         buildVersion);


ALTER TABLE versionFiles
    DROP COLUMN majorVersion,
    DROP COLUMN minorVersion,
    DROP COLUMN revisionVersion,
    DROP COLUMN buildVersion;
