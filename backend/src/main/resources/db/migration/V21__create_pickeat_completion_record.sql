-- -------------------------------
-- 기존 테이블 정리
-- -------------------------------
RENAME TABLE pickeat TO old_pickeat;
RENAME TABLE participant TO old_participant;
RENAME TABLE restaurant TO old_restaurant;
RENAME TABLE restaurant_like TO old_restaurant_like;
RENAME TABLE pickeat_result TO old_pickeat_result;

-- -------------------------------
-- 새로운 테이블 생성
-- -------------------------------
CREATE TABLE `pickeat_record`
(
    `id`         bigint       NOT NULL AUTO_INCREMENT,
    `created_at` datetime(6)  NOT NULL,
    `updated_at` datetime(6)  NOT NULL,
    `deleted_at` datetime(6)  DEFAULT NULL COMMENT 'Soft-delete timestamp',
    `code`       varchar(255) NOT NULL,
    `name`       varchar(255) NOT NULL,
    `room_id`    bigint       DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `UK_pickeat_record_code` (`code`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE `pickeat_result`
(
    `id`                bigint                                                  NOT NULL AUTO_INCREMENT,
    `created_at`        datetime(6)                                             NOT NULL,
    `updated_at`        datetime(6)                                             NOT NULL,
    `deleted_at`        datetime(6)                                             DEFAULT NULL COMMENT 'Soft-delete timestamp',
    `pickeat_record_id` bigint                                                  NOT NULL,
    `code`              varchar(255)                                            NOT NULL,
    `name`              varchar(255)                                            NOT NULL,
    `food_category`     varchar(255)                                            NOT NULL,
    `road_address_name` varchar(255)                                            NOT NULL,
    `place_url`         varchar(255)                                            NOT NULL,
    `tags`              varchar(255)                                            DEFAULT NULL,
    `picture_key`       varchar(255)                                            DEFAULT NULL,
    `picture_url`       varchar(255)                                            DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `UK_result_pickeat_record_id` (`pickeat_record_id`),
    UNIQUE KEY `UK_result_code` (`code`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;
