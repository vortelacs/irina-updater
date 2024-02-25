
CREATE TABLE IF NOT EXISTS `fileIndex` (
                                           `id` int(11) NOT NULL AUTO_INCREMENT,
                                           `fileHash` binary(64) NOT NULL,
                                           `uploadDate` datetime NOT NULL DEFAULT current_timestamp(),
                                           PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=231 DEFAULT CHARSET=utf8mb4;


CREATE TABLE IF NOT EXISTS `versionFiles` (
                                              `id` int(11) NOT NULL AUTO_INCREMENT,
                                              `fileId` int(11) NOT NULL,
                                              `filePath` varchar(1000) NOT NULL,
                                              `majorVersion` int(11) NOT NULL,
                                              `minorVersion` int(11) NOT NULL,
                                              `revisionVersion` int(11) NOT NULL,
                                              `buildVersion` int(11) NOT NULL,
                                              `product` varchar(50) NOT NULL,
                                              `channel` varchar(50) NOT NULL,
                                              PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1773 DEFAULT CHARSET=utf8mb4;