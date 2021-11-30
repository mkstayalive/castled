CREATE TABLE `external_apps` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(256) NOT NULL,
  `config` text NOT NULL,
  `team_id` bigint(20) NOT NULL,
  `type` varchar(256) NOT NULL,
  `status` varchar(256) NOT NULL,
  `created_ts` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_ts` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_team_id` (`team_id`),
  KEY `idx_type` (`type`)
);

CREATE TABLE `warehouses` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(256) NOT NULL,
  `config` text NOT NULL,
  `team_id` bigint(20) NOT NULL,
  `type` varchar(256) NOT NULL,
  `status` varchar(256) NOT NULL,
  `created_ts` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_ts` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_team_id` (`team_id`),
  KEY `idx_type` (`type`)
);

CREATE TABLE `pipelines` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  `team_id` bigint(20) NOT NULL,
  `uuid` varchar(255) NOT NULL,
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0',
  `app_id` bigint(20),
  `warehouse_id` bigint(20) NOT NULL,
  `config` text,
  `source_query` text,
  `app_sync_config` text,
  `failure_message` text,
  `status` varchar(50),
  `sync_status` varchar(50),
  `mapping` text,
  `created_ts` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_ts` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `seq_id` bigint(20),
  `schedule` text,
  PRIMARY KEY (`id`),
  KEY `idx_team_id` (`team_id`)
);

CREATE TABLE `pipeline_runs` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `pipeline_id` bigint(20) NOT NULL,
  `status` varchar(50) NOT NULL,
  `failure_message` text,
  `run_stats` text,
  `created_ts` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_ts` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_pipeline_id` (`pipeline_id`)
);