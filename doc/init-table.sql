CREATE TABLE IF NOT EXISTS `blobstorage` (
  `path` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  `mimeType` varchar(40) NOT NULL,
  `data` longblob NOT NULL,
  `lastMod` datetime NOT NULL,
  PRIMARY KEY (`path`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
