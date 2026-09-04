/*
 Navicat Premium Data Transfer

 Source Server         : v2
 Source Server Type    : MySQL
 Source Server Version : 80040 (8.0.40)
 Source Host           : localhost:3306
 Source Schema         : defect_detection

 Target Server Type    : MySQL
 Target Server Version : 80040 (8.0.40)
 File Encoding         : 65001

 Date: 08/12/2025 13:09:01
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for annotation_data
-- ----------------------------
DROP TABLE IF EXISTS `annotation_data`;
CREATE TABLE `annotation_data`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `raw_image_id` int NOT NULL,
  `task_id` int NULL DEFAULT NULL,
  `category` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `category_id` tinyint NULL DEFAULT NULL,
  `x` decimal(10, 4) NOT NULL,
  `y` decimal(10, 4) NOT NULL,
  `width` decimal(10, 4) NOT NULL,
  `height` decimal(10, 4) NOT NULL,
  `confidence` decimal(6, 3) NULL DEFAULT 1.000,
  `is_difficult` tinyint NULL DEFAULT 0,
  `annotator_id` int NULL DEFAULT NULL,
  `annotator_name` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `annotation_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `is_verified` tinyint NULL DEFAULT 0,
  `verifier_id` int NULL DEFAULT NULL,
  `verify_time` datetime NULL DEFAULT NULL,
  `is_deleted` tinyint NULL DEFAULT 0,
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_raw_image`(`raw_image_id` ASC) USING BTREE,
  INDEX `idx_task`(`task_id` ASC) USING BTREE,
  INDEX `idx_category`(`category_id` ASC) USING BTREE,
  CONSTRAINT `annotation_data_ibfk_1` FOREIGN KEY (`raw_image_id`) REFERENCES `raw_image` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 47 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of annotation_data
-- ----------------------------
INSERT INTO `annotation_data` VALUES (30, 53, NULL, '合格', 1, 0.0000, 0.0000, 0.0000, 0.0000, 1.000, 0, 1, '标注员', '2025-11-27 04:56:49', 0, NULL, NULL, 1, NULL);
INSERT INTO `annotation_data` VALUES (32, 55, NULL, '裂痕', 2, 0.0000, 0.0000, 0.0000, 0.0000, 1.000, 0, 1, '标注员', '2025-12-08 02:19:37', 0, NULL, NULL, 1, NULL);
INSERT INTO `annotation_data` VALUES (33, 56, NULL, '合格', 1, 0.0000, 0.0000, 0.0000, 0.0000, 1.000, 0, 1, '标注员', '2025-12-08 02:20:27', 0, NULL, NULL, 1, NULL);
INSERT INTO `annotation_data` VALUES (35, 58, NULL, '裂痕', 2, 0.0000, 0.0000, 0.0000, 0.0000, 1.000, 0, 1, '标注员', '2025-12-08 02:47:17', 0, NULL, NULL, 1, NULL);
INSERT INTO `annotation_data` VALUES (36, 59, NULL, '裂痕', 2, 224.3333, 212.6667, 21.0000, 29.0000, 1.000, 0, 1, '标注员', '2025-12-08 02:57:19', 0, NULL, NULL, 1, NULL);
INSERT INTO `annotation_data` VALUES (37, 55, NULL, '裂痕', 2, 290.3333, 308.8229, 47.0000, 26.0000, 1.000, 0, 1, '标注员', '2025-12-08 02:59:11', 0, NULL, NULL, 0, NULL);
INSERT INTO `annotation_data` VALUES (38, 60, NULL, '裂痕', 2, 331.3333, 513.8229, 157.0000, 41.0000, 1.000, 0, 1, '标注员', '2025-12-08 02:59:49', 0, NULL, NULL, 0, NULL);
INSERT INTO `annotation_data` VALUES (39, 61, NULL, '裂痕', 2, 0.0000, 0.0000, 0.0000, 0.0000, 1.000, 0, 1, '标注员', '2025-12-08 03:10:55', 0, NULL, NULL, 1, NULL);
INSERT INTO `annotation_data` VALUES (40, 63, NULL, '裂痕', 2, 215.3333, 207.6667, 115.0000, 127.0000, 1.000, 0, 1, '标注员', '2025-12-08 03:25:38', 0, NULL, NULL, 1, NULL);
INSERT INTO `annotation_data` VALUES (41, 63, NULL, '划痕', 3, 422.3333, 57.6667, 111.0000, 93.0000, 1.000, 0, 1, '标注员', '2025-12-08 03:25:40', 0, NULL, NULL, 1, NULL);
INSERT INTO `annotation_data` VALUES (42, 63, NULL, '裂痕', 2, 226.3333, 263.6667, 115.0000, 101.0000, 1.000, 0, 1, '标注员', '2025-12-08 03:26:12', 0, NULL, NULL, 1, NULL);
INSERT INTO `annotation_data` VALUES (43, 63, NULL, '划痕', 3, 428.3333, 75.6667, 57.0000, 42.0000, 1.000, 0, 1, '标注员', '2025-12-08 03:26:13', 0, NULL, NULL, 1, NULL);
INSERT INTO `annotation_data` VALUES (44, 64, NULL, '裂痕', 2, 217.3333, 327.6667, 83.0000, 137.0000, 1.000, 0, 1, '标注员', '2025-12-08 03:52:19', 0, NULL, NULL, 1, NULL);
INSERT INTO `annotation_data` VALUES (45, 64, NULL, '划痕', 3, 234.3333, 163.6667, 87.0000, 80.0000, 1.000, 0, 1, '标注员', '2025-12-08 03:52:20', 0, NULL, NULL, 1, NULL);
INSERT INTO `annotation_data` VALUES (46, 62, NULL, '裂痕', 2, 315.3333, 736.6042, 64.0000, 56.0000, 1.000, 0, 1, '标注员', '2025-12-08 04:12:15', 0, NULL, NULL, 0, NULL);

-- ----------------------------
-- Table structure for api
-- ----------------------------
DROP TABLE IF EXISTS `api`;
CREATE TABLE `api`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `is_deleted` decimal(1, 0) NULL DEFAULT 0,
  `create_time` datetime NULL DEFAULT NULL,
  `create_id` int NULL DEFAULT NULL,
  `create_name` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `update_time` datetime NULL DEFAULT NULL,
  `update_id` int NULL DEFAULT NULL,
  `validity_period` int NULL DEFAULT -1,
  `validity_times` int NULL DEFAULT -1,
  `permission_level` tinyint NULL DEFAULT 0,
  `api_key` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `status` decimal(1, 0) NULL DEFAULT 1,
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `counts` int NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `api_key`(`api_key` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of api
-- ----------------------------
INSERT INTO `api` VALUES (1, 1, '2025-11-22 19:12:50', 1, '???1', '2025-11-22 19:12:50', 1, -1, -1, 0, 'APIKEY1', 1, NULL, 0);
INSERT INTO `api` VALUES (2, 1, '2025-11-22 19:12:50', 2, '???2', '2025-11-22 19:12:50', 2, -1, -1, 0, 'APIKEY2', 1, NULL, 0);
INSERT INTO `api` VALUES (3, 1, NULL, NULL, '???1', NULL, NULL, 1, 2, 1, 'ma-xVrI7ZoBlH', 1, NULL, 0);
INSERT INTO `api` VALUES (4, 1, NULL, NULL, '???1', NULL, NULL, 1, 2, 1, 'ma-L9zTzYCmDS', 1, NULL, 0);
INSERT INTO `api` VALUES (5, 1, NULL, NULL, '???1', NULL, NULL, 2, 1, 1, 'ma-7T1lYTB0x0', 1, NULL, 0);
INSERT INTO `api` VALUES (6, 1, NULL, NULL, '???1', NULL, NULL, 1, 2, 1, 'ma-zgFFwcdAFY', 1, NULL, 0);
INSERT INTO `api` VALUES (7, 0, NULL, NULL, 'admin1', NULL, NULL, -1, -1, 1, 'ma-6jW2ipsalB', 1, '', 0);
INSERT INTO `api` VALUES (8, 0, NULL, NULL, 'admin1', NULL, NULL, -1, -1, 1, 'ma-a2JJGosMzM', 1, '', 0);

-- ----------------------------
-- Table structure for defection
-- ----------------------------
DROP TABLE IF EXISTS `defection`;
CREATE TABLE `defection`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `l` decimal(10, 4) NULL DEFAULT NULL,
  `h` decimal(10, 4) NULL DEFAULT NULL,
  `x` decimal(10, 4) NULL DEFAULT NULL,
  `y` decimal(10, 4) NULL DEFAULT NULL,
  `score` decimal(6, 3) NULL DEFAULT NULL,
  `detect_id` int NULL DEFAULT NULL,
  `category` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `category_id` tinyint NULL DEFAULT NULL,
  `severity_level` int NULL DEFAULT NULL,
  `repair_suggestion` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `data_source` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'detection',
  `annotation_id` int NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 35 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of defection
-- ----------------------------
INSERT INTO `defection` VALUES (1, 1.2340, 5.6780, 0.1230, 0.4560, 0.789, 1, '????1', 1, NULL, NULL, 'detection', NULL);
INSERT INTO `defection` VALUES (2, 3.4560, 7.8900, 0.7890, 0.1230, 0.789, 2, '????2', 2, NULL, NULL, 'detection', NULL);
INSERT INTO `defection` VALUES (3, 190.9610, 170.6960, 96.0200, 85.6480, 0.902, 3, '点蚀', 3, 5, '严重程度: 5级 | 未知缺陷类型,建议人工检查 | 缺陷面积: 32596.2789', 'detection', NULL);
INSERT INTO `defection` VALUES (4, 44.8600, 101.8220, 177.5270, 149.0880, 0.797, 3, '点蚀', 3, 5, '严重程度: 5级 | 未知缺陷类型,建议人工检查 | 缺陷面积: 4567.7349', 'detection', NULL);
INSERT INTO `defection` VALUES (5, 66.8290, 27.8920, 105.5160, 185.9950, 0.565, 3, '点蚀', 3, 4, '严重程度: 4级 | 未知缺陷类型,建议人工检查 | 缺陷面积: 1863.9945', 'detection', NULL);
INSERT INTO `defection` VALUES (6, 171.9290, 155.1520, 86.0350, 78.4230, 0.881, 4, '划痕', 4, 4, '严重程度: 4级 | 中度划痕,需要打磨后重新喷涂 | 缺陷面积: 26675.1282', 'detection', NULL);
INSERT INTO `defection` VALUES (7, 297.0000, 287.0000, 148.5000, 143.5000, 0.500, 5, '划痕', 4, 3, '严重程度: 3级 | 中度划痕,需要打磨后重新喷涂 | 缺陷面积: 85239.0000', 'detection', NULL);
INSERT INTO `defection` VALUES (8, 473.0000, 491.0720, 236.5000, 245.5360, 0.431, 6, '划痕', 4, 3, '严重程度: 3级 | 中度划痕,需要打磨后重新喷涂 | 缺陷面积: 232277.0560', 'detection', NULL);
INSERT INTO `defection` VALUES (9, 662.1210, 327.9330, 2692.9380, 3868.0330, 0.812, 7, '划痕', 4, 4, '严重程度: 4级 | 中度划痕,需要打磨后重新喷涂 | 缺陷面积: 217131.3259', 'detection', NULL);
INSERT INTO `defection` VALUES (10, 334.4300, 853.0690, 167.2150, 2906.8520, 0.443, 7, '斑块', 5, 4, '严重程度: 4级 | 未知缺陷类型,建议人工检查 | 缺陷面积: 285291.8657', 'detection', NULL);
INSERT INTO `defection` VALUES (11, 171.9290, 155.1520, 86.0350, 78.4230, 0.881, 8, '划痕', 4, 4, '严重程度: 4级 | 中度划痕,需要打磨后重新喷涂 | 缺陷面积: 26675.1282', 'detection', NULL);
INSERT INTO `defection` VALUES (12, 40.5320, 114.2670, 179.7330, 57.4730, 0.944, 37, '斑块', 5, 5, '严重程度: 5级 | 未知缺陷类型,建议人工检查 | 缺陷面积: 4631.4700', 'detection', NULL);
INSERT INTO `defection` VALUES (13, 44.2940, 67.4840, 51.2540, 36.2960, 0.911, 37, '斑块', 5, 5, '严重程度: 5级 | 未知缺陷类型,建议人工检查 | 缺陷面积: 2989.1363', 'detection', NULL);
INSERT INTO `defection` VALUES (14, 89.4100, 86.9500, 78.1170, 156.5240, 0.896, 37, '斑块', 5, 5, '严重程度: 5级 | 未知缺陷类型,建议人工检查 | 缺陷面积: 7774.1995', 'detection', NULL);
INSERT INTO `defection` VALUES (15, 30.2150, 23.4650, 184.8750, 188.2670, 0.847, 37, '斑块', 5, 5, '严重程度: 5级 | 未知缺陷类型,建议人工检查 | 缺陷面积: 708.9950', 'detection', NULL);
INSERT INTO `defection` VALUES (16, 16.9380, 39.8040, 8.4690, 19.9020, 0.570, 37, '斑块', 5, 4, '严重程度: 4级 | 未知缺陷类型,建议人工检查 | 缺陷面积: 674.2002', 'detection', NULL);
INSERT INTO `defection` VALUES (17, 77.1890, 69.3140, 117.5020, 42.2080, 0.514, 37, '斑块', 5, 4, '严重程度: 4级 | 未知缺陷类型,建议人工检查 | 缺陷面积: 5350.2783', 'detection', NULL);
INSERT INTO `defection` VALUES (18, 75.6560, 70.1060, 116.3540, 41.4020, 0.496, 37, '斑块', 5, 4, '严重程度: 4级 | 未知缺陷类型,建议人工检查 | 缺陷面积: 5303.9395', 'detection', NULL);
INSERT INTO `defection` VALUES (19, 171.9290, 155.1520, 86.0350, 78.4230, 0.881, 39, '划痕', 4, 4, '严重程度: 4级 | 中度划痕,需要打磨后重新喷涂 | 缺陷面积: 26675.1282', 'detection', NULL);
INSERT INTO `defection` VALUES (20, 286.5890, 586.8590, 403.4430, 293.5700, 0.887, 40, '点蚀', 3, 5, '严重程度: 5级 | 未知缺陷类型,建议人工检查 | 缺陷面积: 168187.3340', 'detection', NULL);
INSERT INTO `defection` VALUES (21, 659.1500, 716.5460, 688.3220, 857.2420, 0.494, 41, '划痕', 4, 3, '严重程度: 3级 | 中度划痕,需要打磨后重新喷涂 | 缺陷面积: 472311.2959', 'detection', NULL);
INSERT INTO `defection` VALUES (22, 659.1500, 716.5460, 688.3220, 857.2420, 0.494, 42, '划痕', 4, 3, '严重程度: 3级 | 中度划痕,需要打磨后重新喷涂 | 缺陷面积: 472311.2959', 'detection', NULL);
INSERT INTO `defection` VALUES (23, 248.0000, 227.7370, 124.0000, 113.9260, 0.765, 43, '划痕', 4, 4, '严重程度: 4级 | 中度划痕,需要打磨后重新喷涂 | 缺陷面积: 56478.7760', 'detection', NULL);
INSERT INTO `defection` VALUES (24, 123.2630, 269.0000, 171.6330, 134.5000, 0.516, 44, '点蚀', 3, 4, '严重程度: 4级 | 未知缺陷类型,建议人工检查 | 缺陷面积: 33157.7470', 'detection', NULL);
INSERT INTO `defection` VALUES (25, 104.3350, 269.0000, 52.5130, 134.5000, 0.404, 44, '斑块', 5, 4, '严重程度: 4级 | 未知缺陷类型,建议人工检查 | 缺陷面积: 28066.1150', 'detection', NULL);
INSERT INTO `defection` VALUES (26, 215.9820, 201.0370, 107.9910, 101.6710, 0.823, 45, '点蚀', 3, 5, '严重程度: 5级 | 未知缺陷类型,建议人工检查 | 缺陷面积: 43420.3733', 'detection', NULL);
INSERT INTO `defection` VALUES (27, 165.9900, 150.2770, 82.9950, 76.4970, 0.631, 46, '划痕', 4, 4, '严重程度: 4级 | 中度划痕,需要打磨后重新喷涂 | 缺陷面积: 24944.4792', 'detection', NULL);
INSERT INTO `defection` VALUES (28, 269.0000, 243.7080, 134.5000, 123.1210, 0.904, 47, '点蚀', 3, 5, '严重程度: 5级 | 未知缺陷类型,建议人工检查 | 缺陷面积: 65557.4520', 'detection', NULL);
INSERT INTO `defection` VALUES (29, 230.0000, 205.7600, 115.0000, 104.3790, 0.605, 48, '点蚀', 3, 4, '严重程度: 4级 | 未知缺陷类型,建议人工检查 | 缺陷面积: 47324.8000', 'detection', NULL);
INSERT INTO `defection` VALUES (30, 165.0200, 167.0000, 82.5100, 83.5000, 0.856, 49, '划痕', 4, 4, '严重程度: 4级 | 中度划痕,需要打磨后重新喷涂 | 缺陷面积: 27558.3400', 'detection', NULL);
INSERT INTO `defection` VALUES (31, 138.9020, 127.8000, 69.4510, 63.9000, 0.913, 50, '划痕', 4, 4, '严重程度: 4级 | 中度划痕,需要打磨后重新喷涂 | 缺陷面积: 17751.6756', 'detection', NULL);
INSERT INTO `defection` VALUES (32, 113.0000, 101.5050, 56.5000, 52.1800, 0.464, 51, '点蚀', 3, 4, '严重程度: 4级 | 未知缺陷类型,建议人工检查 | 缺陷面积: 11470.0650', 'detection', NULL);
INSERT INTO `defection` VALUES (33, 125.8570, 136.0000, 65.1200, 68.0000, 0.784, 52, '划痕', 4, 4, '严重程度: 4级 | 中度划痕,需要打磨后重新喷涂 | 缺陷面积: 17116.5520', 'detection', NULL);
INSERT INTO `defection` VALUES (34, 230.0000, 205.7600, 115.0000, 104.3790, 0.605, 55, '裂痕', 6, 4, '严重程度: 4级 | 未知缺陷类型,建议人工检查 | 缺陷面积: 47324.8000', 'detection', NULL);

-- ----------------------------
-- Table structure for defection_category
-- ----------------------------
DROP TABLE IF EXISTS `defection_category`;
CREATE TABLE `defection_category`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `count` int NULL DEFAULT NULL,
  `create_time` datetime NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of defection_category
-- ----------------------------
INSERT INTO `defection_category` VALUES (1, '????1', 5, NULL);
INSERT INTO `defection_category` VALUES (2, '????2', 10, NULL);
INSERT INTO `defection_category` VALUES (3, '点蚀', 0, '2025-11-22 19:30:11');
INSERT INTO `defection_category` VALUES (4, '划痕', 0, '2025-11-26 16:42:57');
INSERT INTO `defection_category` VALUES (5, '斑块', 0, '2025-11-26 16:56:59');
INSERT INTO `defection_category` VALUES (6, '裂痕', 0, '2025-12-03 11:41:12');

-- ----------------------------
-- Table structure for detect_log
-- ----------------------------
DROP TABLE IF EXISTS `detect_log`;
CREATE TABLE `detect_log`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `defections_sum` tinyint NULL DEFAULT 0,
  `time` datetime NULL DEFAULT NULL,
  `work_order_id` int NULL DEFAULT NULL,
  `storage_path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `data_source` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'detection',
  `raw_image_id` int NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `name`(`name` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 56 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of detect_log
-- ----------------------------
INSERT INTO `detect_log` VALUES (3, 'ccba35b2_1076_44c0_a4fd_6092896533cd_20251122193010.jpeg', 3, '2025-11-22 19:30:11', 1, './detectPicture\\1\\ccba35b2_1076_44c0_a4fd_6092896533cd_20251122193010.jpeg', 'detection', NULL);
INSERT INTO `detect_log` VALUES (4, 'c5067878_eb5e_4b0b_97e7_6e25a69f0f72_20251126164256.jpeg', 1, '2025-11-26 16:42:57', 1, './detectPicture\\1\\c5067878_eb5e_4b0b_97e7_6e25a69f0f72_20251126164256.jpeg', 'detection', NULL);
INSERT INTO `detect_log` VALUES (5, '294dd2ac_37c3_4efe_ae2c_80f0cc222780_20251126164440.jpeg', 1, '2025-11-26 16:44:40', 1, './detectPicture\\1\\294dd2ac_37c3_4efe_ae2c_80f0cc222780_20251126164440.jpeg', 'detection', NULL);
INSERT INTO `detect_log` VALUES (6, '2d5e21f1_c0f8_4d59_b8dd_3033978252ab_20251126164610.jpeg', 1, '2025-11-26 16:46:10', 1, './detectPicture\\1\\2d5e21f1_c0f8_4d59_b8dd_3033978252ab_20251126164610.jpeg', 'detection', NULL);
INSERT INTO `detect_log` VALUES (7, '5f69bc65_4cf7_45b2_8857_a23c86d34883_20251126165658.jpeg', 2, '2025-11-26 16:56:59', 1, './detectPicture\\1\\5f69bc65_4cf7_45b2_8857_a23c86d34883_20251126165658.jpeg', 'detection', NULL);
INSERT INTO `detect_log` VALUES (8, 'c944160a_d2b6_40a0_96f5_8410d2086261_20251126170348.jpeg', 1, '2025-11-26 17:03:48', 1, './detectPicture\\1\\c944160a_d2b6_40a0_96f5_8410d2086261_20251126170348.jpeg', 'detection', NULL);
INSERT INTO `detect_log` VALUES (9, 'f8770eb9_b082_4467_8cc2_2e54f45b5110_20251126170458.jpeg', 0, '2025-11-26 17:04:58', 1, './detectPicture\\1\\f8770eb9_b082_4467_8cc2_2e54f45b5110_20251126170458.jpeg', 'detection', NULL);
INSERT INTO `detect_log` VALUES (10, '7ebcb928_3430_4d18_86b0_4b1d77f54ce2_20251126170650.jpeg', 0, '2025-11-26 17:06:51', 1, './detectPicture\\1\\7ebcb928_3430_4d18_86b0_4b1d77f54ce2_20251126170650.jpeg', 'detection', NULL);
INSERT INTO `detect_log` VALUES (11, 'e0022b64_6a7a_48a2_8b97_58250cd21b47_20251126170741.jpeg', 0, '2025-11-26 17:07:41', 1, './detectPicture\\1\\e0022b64_6a7a_48a2_8b97_58250cd21b47_20251126170741.jpeg', 'detection', NULL);
INSERT INTO `detect_log` VALUES (12, 'eb855bf5_0f7e_471e_b698_e71d357fa12b_20251126170822.jpeg', 0, '2025-11-26 17:08:22', 1, './detectPicture\\1\\eb855bf5_0f7e_471e_b698_e71d357fa12b_20251126170822.jpeg', 'detection', NULL);
INSERT INTO `detect_log` VALUES (13, '9cd53773_873c_4188_ba96_295d61dce049_20251126170902.jpeg', 0, '2025-11-26 17:09:02', 1, './detectPicture\\1\\9cd53773_873c_4188_ba96_295d61dce049_20251126170902.jpeg', 'detection', NULL);
INSERT INTO `detect_log` VALUES (14, '26c32676_2a54_4d08_ae73_f5c74653e989_20251126171021.jpeg', 0, '2025-11-26 17:10:21', 2, './detectPicture\\2\\26c32676_2a54_4d08_ae73_f5c74653e989_20251126171021.jpeg', 'detection', NULL);
INSERT INTO `detect_log` VALUES (15, 'c01d2b61_21b2_4ee0_875a_563f2bbf455f_20251126190653.jpeg', 0, '2025-11-26 19:06:54', 2, './detectPicture\\2\\c01d2b61_21b2_4ee0_875a_563f2bbf455f_20251126190653.jpeg', 'detection', NULL);
INSERT INTO `detect_log` VALUES (16, '6815f115_7f88_4dff_875d_8bc67aa00fa4_20251126190933.jpeg', 0, '2025-11-26 19:09:34', 2, './detectPicture\\2\\6815f115_7f88_4dff_875d_8bc67aa00fa4_20251126190933.jpeg', 'detection', NULL);
INSERT INTO `detect_log` VALUES (17, '43ef1837_4dd6_4109_abae_d4814c941a74_20251126192347.jpeg', 0, '2025-11-26 19:23:48', 2, './detectPicture\\2\\43ef1837_4dd6_4109_abae_d4814c941a74_20251126192347.jpeg', 'detection', NULL);
INSERT INTO `detect_log` VALUES (18, '08e37f8a_6672_4e73_be7e_9842da1611dc_20251126192347.jpeg', 0, '2025-11-26 19:23:48', 2, './detectPicture\\2\\08e37f8a_6672_4e73_be7e_9842da1611dc_20251126192347.jpeg', 'detection', NULL);
INSERT INTO `detect_log` VALUES (19, 'a259af11_51b0_436f_b9ad_c7a586beaa9d_20251126192457.jpeg', 0, '2025-11-26 19:24:58', 2, './detectPicture\\2\\a259af11_51b0_436f_b9ad_c7a586beaa9d_20251126192457.jpeg', 'detection', NULL);
INSERT INTO `detect_log` VALUES (20, '45bb55ae_abb4_4d1e_8c56_7c9b46eddfad_20251126192457.jpeg', 0, '2025-11-26 19:24:58', 2, './detectPicture\\2\\45bb55ae_abb4_4d1e_8c56_7c9b46eddfad_20251126192457.jpeg', 'detection', NULL);
INSERT INTO `detect_log` VALUES (21, 'f4e7005f_d3a2_4293_886d_20b663d8ea0c_20251126192520.jpeg', 0, '2025-11-26 19:25:21', 2, './detectPicture\\2\\f4e7005f_d3a2_4293_886d_20b663d8ea0c_20251126192520.jpeg', 'detection', NULL);
INSERT INTO `detect_log` VALUES (22, '4d77863e_7ced_433e_841c_712021187479_20251126192520.jpeg', 0, '2025-11-26 19:25:21', 2, './detectPicture\\2\\4d77863e_7ced_433e_841c_712021187479_20251126192520.jpeg', 'detection', NULL);
INSERT INTO `detect_log` VALUES (23, 'c33d71b6_c452_4ecd_a436_1f153190fc8d_20251126192548.jpeg', 0, '2025-11-26 19:25:49', 2, './detectPicture\\2\\c33d71b6_c452_4ecd_a436_1f153190fc8d_20251126192548.jpeg', 'detection', NULL);
INSERT INTO `detect_log` VALUES (24, 'cdcd12d3_1acd_4fa4_98ca_954d7855bc36_20251126192548.jpeg', 0, '2025-11-26 19:25:49', 2, './detectPicture\\2\\cdcd12d3_1acd_4fa4_98ca_954d7855bc36_20251126192548.jpeg', 'detection', NULL);
INSERT INTO `detect_log` VALUES (25, '757ae723_5b30_4090_b27e_e55b1a913d73_20251126193318.jpeg', 0, '2025-11-26 19:33:19', 2, './detectPicture\\2\\757ae723_5b30_4090_b27e_e55b1a913d73_20251126193318.jpeg', 'detection', NULL);
INSERT INTO `detect_log` VALUES (26, 'cfc3388e_dade_46de_9070_f256927a4afd_20251126193318.jpeg', 0, '2025-11-26 19:33:19', 2, './detectPicture\\2\\cfc3388e_dade_46de_9070_f256927a4afd_20251126193318.jpeg', 'detection', NULL);
INSERT INTO `detect_log` VALUES (27, '4195aadd_9868_4dea_a4cd_ab1d00af0ca6_20251126193507.jpeg', 0, '2025-11-26 19:35:07', 2, './detectPicture\\2\\4195aadd_9868_4dea_a4cd_ab1d00af0ca6_20251126193507.jpeg', 'detection', NULL);
INSERT INTO `detect_log` VALUES (28, '5fc3c036_f90a_44be_993a_00379f602f3d_20251126193507.jpeg', 0, '2025-11-26 19:35:07', 2, './detectPicture\\2\\5fc3c036_f90a_44be_993a_00379f602f3d_20251126193507.jpeg', 'detection', NULL);
INSERT INTO `detect_log` VALUES (29, '27d547cd_4291_42d2_b5f1_308291b90899_20251126193538.jpeg', 0, '2025-11-26 19:35:38', 2, './detectPicture\\2\\27d547cd_4291_42d2_b5f1_308291b90899_20251126193538.jpeg', 'detection', NULL);
INSERT INTO `detect_log` VALUES (30, '29dd79ec_c248_4634_87b2_9fe4989e4fe4_20251126193538.jpeg', 0, '2025-11-26 19:35:38', 2, './detectPicture\\2\\29dd79ec_c248_4634_87b2_9fe4989e4fe4_20251126193538.jpeg', 'detection', NULL);
INSERT INTO `detect_log` VALUES (31, '6e764a56_2c2c_4fc8_9d9d_a43355a8a09a_20251126193842.jpeg', 0, '2025-11-26 19:38:42', 2, './detectPicture\\2\\6e764a56_2c2c_4fc8_9d9d_a43355a8a09a_20251126193842.jpeg', 'detection', NULL);
INSERT INTO `detect_log` VALUES (32, '2d347197_b423_4319_9ed2_497341b1afca_20251126193919.jpeg', 0, '2025-11-26 19:39:20', 2, './detectPicture\\2\\2d347197_b423_4319_9ed2_497341b1afca_20251126193919.jpeg', 'detection', NULL);
INSERT INTO `detect_log` VALUES (33, '2a7bd6c9_3876_455e_8ad2_58c62336bb18_20251126193939.jpeg', 0, '2025-11-26 19:39:40', 2, './detectPicture\\2\\2a7bd6c9_3876_455e_8ad2_58c62336bb18_20251126193939.jpeg', 'detection', NULL);
INSERT INTO `detect_log` VALUES (34, '3ec95f8a_2a37_4d26_ba7c_d280091d2ac2_20251126194005.jpeg', 0, '2025-11-26 19:40:05', 2, './detectPicture\\2\\3ec95f8a_2a37_4d26_ba7c_d280091d2ac2_20251126194005.jpeg', 'detection', NULL);
INSERT INTO `detect_log` VALUES (35, '3ddf1948_7b4c_4201_aca5_012258e848f2_20251126202058.jpeg', 0, '2025-11-26 20:20:58', 2, './detectPicture\\2\\3ddf1948_7b4c_4201_aca5_012258e848f2_20251126202058.jpeg', 'detection', NULL);
INSERT INTO `detect_log` VALUES (36, '29e30aaa_7315_49cf_8e6c_bec4e4824876_20251126202139.jpeg', 0, '2025-11-26 20:21:39', 2, './detectPicture\\2\\29e30aaa_7315_49cf_8e6c_bec4e4824876_20251126202139.jpeg', 'detection', NULL);
INSERT INTO `detect_log` VALUES (37, '8f5e6777_2d49_4d3a_85f2_5d45392e8920_20251126202400.jpeg', 7, '2025-11-26 20:24:01', 2, './detectPicture\\2\\8f5e6777_2d49_4d3a_85f2_5d45392e8920_20251126202400.jpeg', 'detection', NULL);
INSERT INTO `detect_log` VALUES (38, '8ec505be_232b_4187_b875_408b9bc6923c_20251126202437.jpeg', 0, '2025-11-26 20:24:37', 2, './detectPicture\\2\\8ec505be_232b_4187_b875_408b9bc6923c_20251126202437.jpeg', 'detection', NULL);
INSERT INTO `detect_log` VALUES (39, '329a17ef_6483_446e_8f6f_8821ab2d14af_20251126202514.jpeg', 1, '2025-11-26 20:25:14', 2, './detectPicture\\2\\329a17ef_6483_446e_8f6f_8821ab2d14af_20251126202514.jpeg', 'detection', NULL);
INSERT INTO `detect_log` VALUES (40, '4a74be1d_1324_4e79_a257_9ce28aac2462_20251126202539.jpeg', 1, '2025-11-26 20:25:40', 2, './detectPicture\\2\\4a74be1d_1324_4e79_a257_9ce28aac2462_20251126202539.jpeg', 'detection', NULL);
INSERT INTO `detect_log` VALUES (41, '818d7724_4a56_4562_8514_46bf15caf228_20251126202616.jpeg', 1, '2025-11-26 20:26:16', 2, './detectPicture\\2\\818d7724_4a56_4562_8514_46bf15caf228_20251126202616.jpeg', 'detection', NULL);
INSERT INTO `detect_log` VALUES (42, 'bd31554f_7331_4971_a574_a586397dee9e_20251127102130.jpeg', 1, '2025-11-27 10:21:30', 2, './detectPicture\\2\\bd31554f_7331_4971_a574_a586397dee9e_20251127102130.jpeg', 'detection', NULL);
INSERT INTO `detect_log` VALUES (43, 'e1d1fc02_0c8b_4baf_8133_b1cf552b8562_20251127102740.jpeg', 1, '2025-11-27 10:27:41', 2, './detectPicture\\2\\e1d1fc02_0c8b_4baf_8133_b1cf552b8562_20251127102740.jpeg', 'detection', NULL);
INSERT INTO `detect_log` VALUES (44, 'faf744ed_e25e_43d4_a84d_58ed155f73bd_20251127102855.jpeg', 2, '2025-11-27 10:28:55', 2, './detectPicture\\2\\faf744ed_e25e_43d4_a84d_58ed155f73bd_20251127102855.jpeg', 'detection', NULL);
INSERT INTO `detect_log` VALUES (45, 'c28835a1_0011_425d_adef_ae95d5afba97_20251127103205.jpeg', 1, '2025-11-27 10:32:05', 2, './detectPicture\\2\\c28835a1_0011_425d_adef_ae95d5afba97_20251127103205.jpeg', 'detection', NULL);
INSERT INTO `detect_log` VALUES (46, '7dbfa968_0e8b_44a4_9511_3a19616b5b36_20251127103313.jpeg', 1, '2025-11-27 10:33:13', 2, './detectPicture\\2\\7dbfa968_0e8b_44a4_9511_3a19616b5b36_20251127103313.jpeg', 'detection', NULL);
INSERT INTO `detect_log` VALUES (47, 'eabb1c18_ff89_42c1_99f6_2c738574b05c_20251127103346.jpeg', 1, '2025-11-27 10:33:47', 2, './detectPicture\\2\\eabb1c18_ff89_42c1_99f6_2c738574b05c_20251127103346.jpeg', 'detection', NULL);
INSERT INTO `detect_log` VALUES (48, 'eb6a3340_d5cf_4764_b9a0_2b28831d71ce_20251127103434.jpeg', 1, '2025-11-27 10:34:34', 2, './detectPicture\\2\\eb6a3340_d5cf_4764_b9a0_2b28831d71ce_20251127103434.jpeg', 'detection', NULL);
INSERT INTO `detect_log` VALUES (49, 'f031d0fd_5bfd_402a_b174_c8cc9b626ea7_20251127104429.jpeg', 1, '2025-11-27 10:44:30', 2, './detectPicture\\2\\f031d0fd_5bfd_402a_b174_c8cc9b626ea7_20251127104429.jpeg', 'detection', NULL);
INSERT INTO `detect_log` VALUES (50, '128feb1a_46d7_431c_88b4_faabbd336c83_20251127104540.jpeg', 1, '2025-11-27 10:45:40', 2, './detectPicture\\2\\128feb1a_46d7_431c_88b4_faabbd336c83_20251127104540.jpeg', 'detection', NULL);
INSERT INTO `detect_log` VALUES (51, 'e357e679_95ec_4568_b98a_cfd216f18e27_20251127104643.jpeg', 1, '2025-11-27 10:46:44', 2, './detectPicture\\2\\e357e679_95ec_4568_b98a_cfd216f18e27_20251127104643.jpeg', 'detection', NULL);
INSERT INTO `detect_log` VALUES (52, 'ccaf6386_ac70_430a_ab69_6c6805ee81bb_20251127104756.jpeg', 1, '2025-11-27 10:47:56', 2, './detectPicture\\2\\ccaf6386_ac70_430a_ab69_6c6805ee81bb_20251127104756.jpeg', 'detection', NULL);
INSERT INTO `detect_log` VALUES (53, '13898960_23ec_4acf_9292_228df17fdd5e_20251127124927.jpeg', 0, '2025-11-27 12:49:27', 2, './detectPicture\\2\\13898960_23ec_4acf_9292_228df17fdd5e_20251127124927.jpeg', 'detection', NULL);
INSERT INTO `detect_log` VALUES (54, 'ccf7a331_3c2a_473a_9c1d_3fd6aeb70f61_20251201121839.jpeg', 0, '2025-12-01 12:18:40', 2, './detectPicture\\2\\ccf7a331_3c2a_473a_9c1d_3fd6aeb70f61_20251201121839.jpeg', 'detection', NULL);
INSERT INTO `detect_log` VALUES (55, '69434beb_e131_45ea_9ed7_3b8fec4ba784_20251203114111.jpeg', 1, '2025-12-03 11:41:12', 2, './detectPicture\\2\\69434beb_e131_45ea_9ed7_3b8fec4ba784_20251203114111.jpeg', 'detection', NULL);

-- ----------------------------
-- Table structure for device
-- ----------------------------
DROP TABLE IF EXISTS `device`;
CREATE TABLE `device`  (
  `id` smallint NOT NULL AUTO_INCREMENT,
  `type` tinyint NOT NULL,
  `mac` char(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `ip` char(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `is_deleted` decimal(1, 0) NULL DEFAULT 0,
  `create_time` datetime NULL DEFAULT NULL,
  `create_id` int NULL DEFAULT NULL,
  `update_time` datetime NULL DEFAULT NULL,
  `update_id` int NULL DEFAULT NULL,
  `is_Connect` tinyint(1) NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of device
-- ----------------------------
INSERT INTO `device` VALUES (1, 1, '00:11:22:33:44:55', '127.0.0.1', 'IP???', '????????', 0, '2025-11-22 19:12:50', 1, '2025-11-22 19:12:50', 1, 0);
INSERT INTO `device` VALUES (2, 2, 'AA:BB:CC:DD:EE:FF', '192.168.1.2', '??2', '??2', 0, '2025-11-22 19:12:50', 2, '2025-11-22 19:12:50', 2, 0);

-- ----------------------------
-- Table structure for manager
-- ----------------------------
DROP TABLE IF EXISTS `manager`;
CREATE TABLE `manager`  (
  `id` smallint NOT NULL AUTO_INCREMENT,
  `account` varchar(11) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `password` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `phone_number` char(11) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `name` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `is_Online` tinyint(1) NULL DEFAULT 0,
  `warnings_open` tinyint(1) NULL DEFAULT 0,
  `warnings_level` tinyint NULL DEFAULT 0,
  `phone_Way` tinyint(1) NULL DEFAULT 0,
  `email_way` tinyint(1) NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `account`(`account` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of manager
-- ----------------------------
INSERT INTO `manager` VALUES (1, 'admin1', 'e10adc3949ba59abbe56e057f20f883e', '12345678901', 'admin1', 'admin1@example.com', 1, 0, 0, 0, 0);
INSERT INTO `manager` VALUES (2, 'admin2', 'e10adc3949ba59abbe56e057f20f883e', '98765432109', 'admin2', 'admin2@example.com', 0, 0, 0, 0, 0);

-- ----------------------------
-- Table structure for model
-- ----------------------------
DROP TABLE IF EXISTS `model`;
CREATE TABLE `model`  (
  `id` smallint NOT NULL AUTO_INCREMENT,
  `name` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `param_storage_path` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `is_deleted` decimal(1, 0) NULL DEFAULT 0,
  `create_time` datetime NULL DEFAULT NULL,
  `create_id` int NULL DEFAULT NULL,
  `update_time` datetime NULL DEFAULT NULL,
  `update_id` int NULL DEFAULT NULL,
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of model
-- ----------------------------
INSERT INTO `model` VALUES (1, '??1', '/path/to/param1', 0, '2025-11-22 19:12:50', 1, '2025-11-22 19:12:50', 1, '??1');
INSERT INTO `model` VALUES (2, '????', '/path/to/param2', 0, '2025-11-22 19:12:50', 2, '2025-11-22 19:12:50', 2, '??2');

-- ----------------------------
-- Table structure for operator
-- ----------------------------
DROP TABLE IF EXISTS `operator`;
CREATE TABLE `operator`  (
  `id` smallint NOT NULL AUTO_INCREMENT,
  `job_id` int NULL DEFAULT NULL,
  `login_pwd` char(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `op_pwd` char(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `is_deleted` decimal(1, 0) NULL DEFAULT 0,
  `create_time` datetime NULL DEFAULT NULL,
  `create_id` int NULL DEFAULT NULL,
  `update_time` datetime NULL DEFAULT NULL,
  `update_id` int NULL DEFAULT NULL,
  `create_name` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `name` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `work_order_id` smallint NULL DEFAULT NULL,
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `job_id`(`job_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of operator
-- ----------------------------
INSERT INTO `operator` VALUES (1, NULL, '123456', 'abcdef', 0, '2025-11-22 19:12:50', 1, '2025-11-22 19:12:50', 1, '???1', '???1', 1, '??1');
INSERT INTO `operator` VALUES (2, NULL, 'abcdef', '123456', 0, '2025-11-22 19:12:50', 2, '2025-11-22 19:12:50', 2, '???2', '???2', 2, '??2');

-- ----------------------------
-- Table structure for raw_image
-- ----------------------------
DROP TABLE IF EXISTS `raw_image`;
CREATE TABLE `raw_image`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `image_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `image_path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `image_size` bigint NULL DEFAULT NULL,
  `image_width` int NULL DEFAULT NULL,
  `image_height` int NULL DEFAULT NULL,
  `upload_source` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'camera',
  `device_id` smallint NULL DEFAULT NULL,
  `work_order_id` int NULL DEFAULT NULL,
  `upload_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `upload_user_id` int NULL DEFAULT NULL,
  `upload_user_name` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `status` tinyint NULL DEFAULT 0,
  `is_deleted` tinyint NULL DEFAULT 0,
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_upload_time`(`upload_time` ASC) USING BTREE,
  INDEX `idx_work_order`(`work_order_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 65 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of raw_image
-- ----------------------------
INSERT INTO `raw_image` VALUES (53, '12.jpg', 'uploads\\images\\20251127_125642_877.jpg', 16951, NULL, NULL, 'camera', NULL, NULL, '2025-11-27 12:56:43', NULL, NULL, 2, 1, NULL);
INSERT INTO `raw_image` VALUES (55, '谭浩航.png', 'uploads\\images\\20251208_101932_170.png', 318106, NULL, NULL, 'camera', NULL, NULL, '2025-12-08 10:19:32', NULL, NULL, 2, 1, NULL);
INSERT INTO `raw_image` VALUES (56, '3.jpg', 'uploads\\images\\20251208_102022_044.jpg', 2626620, NULL, NULL, 'camera', NULL, NULL, '2025-12-08 10:20:22', NULL, NULL, 2, 1, NULL);
INSERT INTO `raw_image` VALUES (58, '62B124124B181CC706CD81164F288FBF.jpg', 'uploads\\images\\20251208_104711_816.jpg', 2582811, NULL, NULL, 'camera', NULL, NULL, '2025-12-08 10:47:12', NULL, NULL, 0, 1, NULL);
INSERT INTO `raw_image` VALUES (59, '2.jpg', 'uploads\\images\\20251208_105714_029.jpg', 2201549, NULL, NULL, 'camera', NULL, NULL, '2025-12-08 10:57:14', NULL, NULL, 2, 1, NULL);
INSERT INTO `raw_image` VALUES (60, '谭浩航.png', 'uploads\\images\\20251208_105904_947.png', 318106, NULL, NULL, 'camera', NULL, NULL, '2025-12-08 10:59:05', NULL, NULL, 2, 0, NULL);
INSERT INTO `raw_image` VALUES (61, '谭浩航.png', 'uploads\\images\\20251208_111049_354.png', 318106, NULL, NULL, 'camera', NULL, NULL, '2025-12-08 11:10:49', NULL, NULL, 0, 1, NULL);
INSERT INTO `raw_image` VALUES (62, 'D1320.jpeg', 'uploads\\images\\20251208_111117_822.jpeg', 431398, NULL, NULL, 'camera', NULL, NULL, '2025-12-08 11:11:18', NULL, NULL, 2, 0, NULL);
INSERT INTO `raw_image` VALUES (63, '17.jpg', 'uploads\\images\\20251208_112534_346.jpg', 21065, NULL, NULL, 'camera', NULL, NULL, '2025-12-08 11:25:34', NULL, NULL, 0, 1, NULL);
INSERT INTO `raw_image` VALUES (64, '17.jpg', 'uploads\\images\\20251208_115215_490.jpg', 21065, NULL, NULL, 'camera', NULL, NULL, '2025-12-08 11:52:15', NULL, NULL, 0, 1, NULL);

-- ----------------------------
-- Table structure for sys_log
-- ----------------------------
DROP TABLE IF EXISTS `sys_log`;
CREATE TABLE `sys_log`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `time` datetime NULL DEFAULT NULL,
  `operation` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `operator` smallint NULL DEFAULT NULL,
  `operator_type` decimal(1, 0) NOT NULL,
  `target` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 56 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_log
-- ----------------------------
INSERT INTO `sys_log` VALUES (1, '2025-11-22 19:12:50', '??1', 1, 1, NULL);
INSERT INTO `sys_log` VALUES (2, '2025-11-22 19:12:50', '??2', 2, 2, NULL);
INSERT INTO `sys_log` VALUES (3, '2025-11-22 19:27:22', '登录', 1, 0, NULL);
INSERT INTO `sys_log` VALUES (4, '2025-11-22 19:30:26', '删除', 1, 0, '检测结果');
INSERT INTO `sys_log` VALUES (5, '2025-11-22 19:30:28', '删除', 1, 0, '检测结果');
INSERT INTO `sys_log` VALUES (6, '2025-11-24 18:00:04', '登录', 1, 0, NULL);
INSERT INTO `sys_log` VALUES (7, '2025-11-26 14:59:04', '登录', 1, 0, NULL);
INSERT INTO `sys_log` VALUES (8, '2025-11-26 15:43:49', '登录', 1, 0, NULL);
INSERT INTO `sys_log` VALUES (9, '2025-11-26 16:06:40', '登录', 1, 0, NULL);
INSERT INTO `sys_log` VALUES (10, '2025-11-26 16:28:33', '登录', 1, 0, NULL);
INSERT INTO `sys_log` VALUES (11, '2025-11-26 16:38:37', '登录', 1, 0, NULL);
INSERT INTO `sys_log` VALUES (12, '2025-11-26 17:31:56', '登录', 1, 0, NULL);
INSERT INTO `sys_log` VALUES (13, '2025-11-26 17:35:53', '登录', 1, 0, NULL);
INSERT INTO `sys_log` VALUES (14, '2025-11-26 17:42:46', '登录', 1, 0, NULL);
INSERT INTO `sys_log` VALUES (15, '2025-11-26 17:45:11', '登录', 1, 0, NULL);
INSERT INTO `sys_log` VALUES (16, '2025-11-26 18:13:13', '登录', 1, 0, NULL);
INSERT INTO `sys_log` VALUES (17, '2025-11-26 18:15:35', '登录', 1, 0, NULL);
INSERT INTO `sys_log` VALUES (18, '2025-11-26 18:23:32', '登录', 1, 0, NULL);
INSERT INTO `sys_log` VALUES (19, '2025-11-26 18:32:21', '登录', 1, 0, NULL);
INSERT INTO `sys_log` VALUES (20, '2025-11-26 18:49:46', '登录', 1, 0, NULL);
INSERT INTO `sys_log` VALUES (21, '2025-11-26 19:00:24', '登录', 1, 0, NULL);
INSERT INTO `sys_log` VALUES (22, '2025-11-26 19:04:53', '开始', 1, 1, '系统状态');
INSERT INTO `sys_log` VALUES (23, '2025-11-26 19:05:14', '登录', 1, 0, NULL);
INSERT INTO `sys_log` VALUES (24, '2025-11-26 19:11:22', '登录', 1, 0, NULL);
INSERT INTO `sys_log` VALUES (25, '2025-11-26 19:19:21', '登录', 1, 0, NULL);
INSERT INTO `sys_log` VALUES (26, '2025-11-26 20:20:11', '登录', 1, 0, NULL);
INSERT INTO `sys_log` VALUES (27, '2025-11-27 10:20:15', '登录', 1, 0, NULL);
INSERT INTO `sys_log` VALUES (28, '2025-11-27 10:42:34', '登录', 1, 0, NULL);
INSERT INTO `sys_log` VALUES (29, '2025-11-27 12:24:14', '登录', 1, 0, NULL);
INSERT INTO `sys_log` VALUES (30, '2025-12-01 12:14:30', '登录', 1, 0, NULL);
INSERT INTO `sys_log` VALUES (31, '2025-12-03 11:37:16', '登录', 1, 0, NULL);
INSERT INTO `sys_log` VALUES (32, '2025-12-03 16:53:39', '登录', 1, 0, NULL);
INSERT INTO `sys_log` VALUES (33, '2025-12-04 15:58:26', '登录', 1, 0, NULL);
INSERT INTO `sys_log` VALUES (34, '2025-12-08 10:14:09', '登录', 1, 0, NULL);
INSERT INTO `sys_log` VALUES (35, '2025-12-08 10:16:11', '添加', 1, 0, 'api');
INSERT INTO `sys_log` VALUES (36, '2025-12-08 10:16:19', '删除', 1, 0, 'api');
INSERT INTO `sys_log` VALUES (37, '2025-12-08 10:16:28', '添加', 1, 0, 'api');
INSERT INTO `sys_log` VALUES (38, '2025-12-08 10:16:31', '删除', 1, 0, 'api');
INSERT INTO `sys_log` VALUES (39, '2025-12-08 10:47:01', '登录', 1, 0, NULL);
INSERT INTO `sys_log` VALUES (40, '2025-12-08 10:52:48', '登录', 1, 0, NULL);
INSERT INTO `sys_log` VALUES (41, '2025-12-08 10:53:00', '登录', 1, 0, NULL);
INSERT INTO `sys_log` VALUES (42, '2025-12-08 10:53:12', '添加', 1, 0, 'api');
INSERT INTO `sys_log` VALUES (43, '2025-12-08 10:53:34', '删除', 1, 0, 'api');
INSERT INTO `sys_log` VALUES (44, '2025-12-08 10:53:53', '登录', 1, 0, NULL);
INSERT INTO `sys_log` VALUES (45, '2025-12-08 10:56:01', '添加', 1, 0, 'api');
INSERT INTO `sys_log` VALUES (46, '2025-12-08 11:10:31', '登录', 1, 0, NULL);
INSERT INTO `sys_log` VALUES (47, '2025-12-08 11:22:44', '登录', 1, 0, NULL);
INSERT INTO `sys_log` VALUES (48, '2025-12-08 11:51:55', '登录', 1, 0, NULL);
INSERT INTO `sys_log` VALUES (49, '2025-12-08 12:08:41', '登录', 1, 0, NULL);
INSERT INTO `sys_log` VALUES (50, '2025-12-08 12:20:44', '登录', 1, 0, NULL);
INSERT INTO `sys_log` VALUES (51, '2025-12-08 12:21:16', '删除', 1, 0, 'api');
INSERT INTO `sys_log` VALUES (52, '2025-12-08 12:21:27', '登录', 1, 0, NULL);
INSERT INTO `sys_log` VALUES (53, '2025-12-08 12:21:32', '添加', 1, 0, 'api');
INSERT INTO `sys_log` VALUES (54, '2025-12-08 12:21:42', '添加', 1, 0, 'api');
INSERT INTO `sys_log` VALUES (55, '2025-12-08 12:21:52', '删除', 1, 0, 'api');

-- ----------------------------
-- Table structure for warnings
-- ----------------------------
DROP TABLE IF EXISTS `warnings`;
CREATE TABLE `warnings`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `create_time` datetime NULL DEFAULT NULL,
  `level` tinyint NULL DEFAULT NULL,
  `type` char(6) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `content` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of warnings
-- ----------------------------
INSERT INTO `warnings` VALUES (1, '2025-11-22 19:12:50', 1, '1', '????1');
INSERT INTO `warnings` VALUES (2, '2025-11-22 19:12:50', 2, '2', '????2');

-- ----------------------------
-- Table structure for work_order
-- ----------------------------
DROP TABLE IF EXISTS `work_order`;
CREATE TABLE `work_order`  (
  `id` smallint NOT NULL AUTO_INCREMENT,
  `is_deleted` decimal(1, 0) NULL DEFAULT 0,
  `create_time` datetime NULL DEFAULT NULL,
  `create_id` int NULL DEFAULT NULL,
  `current_num` int NULL DEFAULT NULL,
  `detect_sum` int NULL DEFAULT NULL,
  `is_over` decimal(1, 0) NULL DEFAULT 0,
  `finish_time` datetime NULL DEFAULT NULL,
  `update_time` datetime NULL DEFAULT NULL,
  `update_id` int NULL DEFAULT NULL,
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of work_order
-- ----------------------------
INSERT INTO `work_order` VALUES (1, 0, '2025-11-22 19:12:50', 1, 10, 10, 1, '2025-11-26 17:09:02', '2025-11-22 19:12:50', 1, '??1');
INSERT INTO `work_order` VALUES (2, 0, '2025-11-22 19:12:50', 2, 2, 20, 0, NULL, '2025-11-22 19:12:50', 2, '??2');

SET FOREIGN_KEY_CHECKS = 1;
