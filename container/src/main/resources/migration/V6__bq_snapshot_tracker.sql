CREATE TABLE `bq_snapshot_tracker` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `pipeline_id` varchar(256) NOT NULL,
  `committed` varchar(256),
  `uncommitted` varchar(256),
  `created_ts` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_ts` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `pipeline_id` (`pipeline_id`)
);