CREATE TABLE `error_reports` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `pipeline_id` bigint(20) NOT NULL,
  `pipeline_run_id` bigint(20) NOT NULL,
  `report` mediumtext NOT NULL,
  `created_ts` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `refresh_ts` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_ts` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_pipeline_run_id` (`pipeline_run_id`)
);