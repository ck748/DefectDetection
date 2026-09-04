/*
Navicat MySQL Data Transfer

Source Server         : mysql
Source Server Version : 80041
Source Host           : localhost:3306
Source Database       : defect_detection

Target Server Type    : MYSQL
Target Server Version : 80041
File Encoding         : 65001

Date: 2026-09-04 13:08:11
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for annotation_data
-- ----------------------------
DROP TABLE IF EXISTS `annotation_data`;
CREATE TABLE `annotation_data` (
  `id` int NOT NULL AUTO_INCREMENT,
  `raw_image_id` int NOT NULL,
  `task_id` int DEFAULT NULL,
  `category` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `category_id` tinyint DEFAULT NULL,
  `x` decimal(10,4) NOT NULL,
  `y` decimal(10,4) NOT NULL,
  `width` decimal(10,4) NOT NULL,
  `height` decimal(10,4) NOT NULL,
  `confidence` decimal(6,3) DEFAULT '1.000',
  `is_difficult` tinyint DEFAULT '0',
  `annotator_id` int DEFAULT NULL,
  `annotator_name` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
  `annotation_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `is_verified` tinyint DEFAULT '0',
  `verifier_id` int DEFAULT NULL,
  `verify_time` datetime DEFAULT NULL,
  `is_deleted` tinyint DEFAULT '0',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_raw_image` (`raw_image_id`) USING BTREE,
  KEY `idx_task` (`task_id`) USING BTREE,
  KEY `idx_category` (`category_id`) USING BTREE,
  CONSTRAINT `annotation_data_ibfk_1` FOREIGN KEY (`raw_image_id`) REFERENCES `raw_image` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE=InnoDB AUTO_INCREMENT=83 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Records of annotation_data
-- ----------------------------
INSERT INTO `annotation_data` VALUES ('30', '53', null, '合格', '1', '0.0000', '0.0000', '0.0000', '0.0000', '1.000', '0', '1', '标注员', '2025-11-27 04:56:49', '0', null, null, '1', null);
INSERT INTO `annotation_data` VALUES ('32', '55', null, '裂痕', '2', '0.0000', '0.0000', '0.0000', '0.0000', '1.000', '0', '1', '标注员', '2025-12-08 02:19:37', '0', null, null, '1', null);
INSERT INTO `annotation_data` VALUES ('33', '56', null, '合格', '1', '0.0000', '0.0000', '0.0000', '0.0000', '1.000', '0', '1', '标注员', '2025-12-08 02:20:27', '0', null, null, '1', null);
INSERT INTO `annotation_data` VALUES ('35', '58', null, '裂痕', '2', '0.0000', '0.0000', '0.0000', '0.0000', '1.000', '0', '1', '标注员', '2025-12-08 02:47:17', '0', null, null, '1', null);
INSERT INTO `annotation_data` VALUES ('36', '59', null, '裂痕', '2', '224.3333', '212.6667', '21.0000', '29.0000', '1.000', '0', '1', '标注员', '2025-12-08 02:57:19', '0', null, null, '1', null);
INSERT INTO `annotation_data` VALUES ('37', '55', null, '裂痕', '2', '290.3333', '308.8229', '47.0000', '26.0000', '1.000', '0', '1', '标注员', '2025-12-08 02:59:11', '0', null, null, '0', null);
INSERT INTO `annotation_data` VALUES ('38', '60', null, '裂痕', '2', '331.3333', '513.8229', '157.0000', '41.0000', '1.000', '0', '1', '标注员', '2025-12-08 02:59:49', '0', null, null, '1', null);
INSERT INTO `annotation_data` VALUES ('39', '61', null, '裂痕', '2', '0.0000', '0.0000', '0.0000', '0.0000', '1.000', '0', '1', '标注员', '2025-12-08 03:10:55', '0', null, null, '1', null);
INSERT INTO `annotation_data` VALUES ('40', '63', null, '裂痕', '2', '215.3333', '207.6667', '115.0000', '127.0000', '1.000', '0', '1', '标注员', '2025-12-08 03:25:38', '0', null, null, '1', null);
INSERT INTO `annotation_data` VALUES ('41', '63', null, '划痕', '3', '422.3333', '57.6667', '111.0000', '93.0000', '1.000', '0', '1', '标注员', '2025-12-08 03:25:40', '0', null, null, '1', null);
INSERT INTO `annotation_data` VALUES ('42', '63', null, '裂痕', '2', '226.3333', '263.6667', '115.0000', '101.0000', '1.000', '0', '1', '标注员', '2025-12-08 03:26:12', '0', null, null, '1', null);
INSERT INTO `annotation_data` VALUES ('43', '63', null, '划痕', '3', '428.3333', '75.6667', '57.0000', '42.0000', '1.000', '0', '1', '标注员', '2025-12-08 03:26:13', '0', null, null, '1', null);
INSERT INTO `annotation_data` VALUES ('44', '64', null, '裂痕', '2', '217.3333', '327.6667', '83.0000', '137.0000', '1.000', '0', '1', '标注员', '2025-12-08 03:52:19', '0', null, null, '1', null);
INSERT INTO `annotation_data` VALUES ('45', '64', null, '划痕', '3', '234.3333', '163.6667', '87.0000', '80.0000', '1.000', '0', '1', '标注员', '2025-12-08 03:52:20', '0', null, null, '1', null);
INSERT INTO `annotation_data` VALUES ('46', '62', null, '裂痕', '2', '315.3333', '736.6042', '64.0000', '56.0000', '1.000', '0', '1', '标注员', '2025-12-08 04:12:15', '0', null, null, '1', null);
INSERT INTO `annotation_data` VALUES ('47', '65', null, '划痕', '3', '68.3333', '6.6667', '218.0000', '526.0000', '1.000', '0', '1', '标注员', '2025-12-08 05:17:01', '0', null, null, '1', null);
INSERT INTO `annotation_data` VALUES ('48', '89', null, '裂痕', '2', '100.0000', '169.6667', '46.0000', '128.0000', '1.000', '0', '1', '标注员', '2025-12-13 08:28:24', '0', null, null, '0', null);
INSERT INTO `annotation_data` VALUES ('49', '89', null, '裂痕', '2', '196.0000', '165.6667', '55.0000', '102.0000', '1.000', '0', '1', '标注员', '2025-12-13 08:28:25', '0', null, null, '0', null);
INSERT INTO `annotation_data` VALUES ('50', '89', null, '裂痕', '2', '211.0000', '91.6667', '76.0000', '64.0000', '1.000', '0', '1', '标注员', '2025-12-13 08:28:25', '0', null, null, '0', null);
INSERT INTO `annotation_data` VALUES ('51', '89', null, '裂痕', '2', '320.0000', '109.6667', '77.0000', '204.0000', '1.000', '0', '1', '标注员', '2025-12-13 08:28:25', '0', null, null, '0', null);
INSERT INTO `annotation_data` VALUES ('52', '89', null, '裂痕', '2', '135.0000', '433.6667', '28.0000', '43.0000', '1.000', '0', '1', '标注员', '2025-12-13 08:28:26', '0', null, null, '0', null);
INSERT INTO `annotation_data` VALUES ('53', '89', null, '裂痕', '2', '83.0000', '449.6667', '94.0000', '67.0000', '1.000', '0', '1', '标注员', '2025-12-13 08:28:26', '0', null, null, '0', null);
INSERT INTO `annotation_data` VALUES ('54', '89', null, '裂痕', '2', '218.0000', '465.6667', '92.0000', '45.0000', '1.000', '0', '1', '标注员', '2025-12-13 08:28:27', '0', null, null, '0', null);
INSERT INTO `annotation_data` VALUES ('55', '89', null, '裂痕', '2', '255.0000', '371.6667', '96.0000', '46.0000', '1.000', '0', '1', '标注员', '2025-12-13 08:28:27', '0', null, null, '0', null);
INSERT INTO `annotation_data` VALUES ('56', '89', null, '裂痕', '2', '364.0000', '365.6667', '73.0000', '71.0000', '1.000', '0', '1', '标注员', '2025-12-13 08:28:27', '0', null, null, '0', null);
INSERT INTO `annotation_data` VALUES ('57', '89', null, '裂痕', '2', '451.0000', '348.6667', '50.0000', '44.0000', '1.000', '0', '1', '标注员', '2025-12-13 08:28:28', '0', null, null, '0', null);
INSERT INTO `annotation_data` VALUES ('58', '89', null, '裂痕', '2', '507.0000', '158.6667', '20.0000', '37.0000', '1.000', '0', '1', '标注员', '2025-12-13 08:28:28', '0', null, null, '0', null);
INSERT INTO `annotation_data` VALUES ('59', '89', null, '裂痕', '2', '461.0000', '37.6667', '9.0000', '53.0000', '1.000', '0', '1', '标注员', '2025-12-13 08:28:29', '0', null, null, '0', null);
INSERT INTO `annotation_data` VALUES ('60', '89', null, '划痕', '3', '483.0000', '457.6667', '26.0000', '52.0000', '1.000', '0', '1', '标注员', '2025-12-13 08:28:30', '0', null, null, '0', null);
INSERT INTO `annotation_data` VALUES ('61', '89', null, '划痕', '3', '423.0000', '495.6667', '16.0000', '28.0000', '1.000', '0', '1', '标注员', '2025-12-13 08:28:30', '0', null, null, '0', null);
INSERT INTO `annotation_data` VALUES ('62', '89', null, '划痕', '3', '99.0000', '347.6667', '76.0000', '37.0000', '1.000', '0', '1', '标注员', '2025-12-13 08:28:31', '0', null, null, '0', null);
INSERT INTO `annotation_data` VALUES ('63', '89', null, '划痕', '3', '102.0000', '55.6667', '49.0000', '46.0000', '1.000', '0', '1', '标注员', '2025-12-13 08:28:32', '0', null, null, '0', null);
INSERT INTO `annotation_data` VALUES ('64', '89', null, '划痕', '3', '63.0000', '107.6667', '14.0000', '60.0000', '1.000', '0', '1', '标注员', '2025-12-13 08:28:33', '0', null, null, '0', null);
INSERT INTO `annotation_data` VALUES ('65', '89', null, '合格', '1', '200.0000', '294.6667', '51.0000', '43.0000', '1.000', '0', '1', '标注员', '2025-12-13 08:28:34', '0', null, null, '0', null);
INSERT INTO `annotation_data` VALUES ('66', '89', null, '合格', '1', '428.0000', '215.6667', '38.0000', '49.0000', '1.000', '0', '1', '标注员', '2025-12-13 08:28:35', '0', null, null, '0', null);
INSERT INTO `annotation_data` VALUES ('67', '89', null, '合格', '1', '433.0000', '101.6667', '28.0000', '56.0000', '1.000', '0', '1', '标注员', '2025-12-13 08:28:36', '0', null, null, '0', null);
INSERT INTO `annotation_data` VALUES ('68', '89', null, '合格', '1', '379.0000', '27.6667', '36.0000', '57.0000', '1.000', '0', '1', '标注员', '2025-12-13 08:28:37', '0', null, null, '0', null);
INSERT INTO `annotation_data` VALUES ('69', '89', null, '合格', '1', '250.0000', '19.6667', '83.0000', '46.0000', '1.000', '0', '1', '标注员', '2025-12-13 08:28:38', '0', null, null, '0', null);
INSERT INTO `annotation_data` VALUES ('70', '89', null, '合格', '1', '165.0000', '11.6667', '56.0000', '32.0000', '1.000', '0', '1', '标注员', '2025-12-13 08:28:39', '0', null, null, '0', null);
INSERT INTO `annotation_data` VALUES ('71', '70', null, '合格', '1', '7.0000', '5.6667', '176.0000', '542.0000', '1.000', '0', '1', '标注员', '2025-12-15 03:33:49', '0', null, null, '1', null);
INSERT INTO `annotation_data` VALUES ('72', '70', null, '裂痕', '2', '197.0000', '3.6667', '157.0000', '524.0000', '1.000', '0', '1', '标注员', '2025-12-15 03:33:53', '0', null, null, '1', null);
INSERT INTO `annotation_data` VALUES ('73', '70', null, '合格', '1', '370.0000', '107.6667', '170.0000', '426.0000', '1.000', '0', '1', '标注员', '2025-12-15 03:33:57', '0', null, null, '1', null);
INSERT INTO `annotation_data` VALUES ('74', '84', null, '合格', '1', '43.0000', '238.0000', '65.0000', '134.0000', '1.000', '0', '1', '标注员', '2025-12-15 03:41:07', '0', null, null, '0', null);
INSERT INTO `annotation_data` VALUES ('75', '68', null, '划痕', '3', '169.3333', '6.6667', '201.0000', '543.0000', '1.000', '0', '1', '标注员', '2025-12-20 06:31:15', '0', null, null, '0', null);
INSERT INTO `annotation_data` VALUES ('76', '72', null, '裂痕', '2', '177.3333', '0.6667', '233.0000', '554.0000', '1.000', '0', '1', '标注员', '2025-12-20 06:31:30', '0', null, null, '0', null);
INSERT INTO `annotation_data` VALUES ('77', '69', null, '合格', '1', '143.0000', '63.6667', '231.0000', '466.0000', '1.000', '0', '1', '标注员', '2025-12-20 08:18:14', '0', null, null, '0', null);
INSERT INTO `annotation_data` VALUES ('78', '69', null, '划痕', '3', '427.0000', '3.6667', '71.0000', '546.0000', '1.000', '0', '1', '标注员', '2025-12-20 08:18:19', '0', null, null, '0', null);
INSERT INTO `annotation_data` VALUES ('79', '74', null, '划痕', '3', '23.0000', '105.6667', '94.0000', '437.0000', '1.000', '0', '1', '标注员', '2025-12-20 08:18:28', '0', null, null, '0', null);
INSERT INTO `annotation_data` VALUES ('80', '74', null, '划痕', '3', '339.0000', '3.6667', '106.0000', '540.0000', '1.000', '0', '1', '标注员', '2025-12-20 08:18:31', '0', null, null, '0', null);
INSERT INTO `annotation_data` VALUES ('81', '86', null, '划痕', '3', '143.3333', '91.6667', '333.0000', '356.0000', '1.000', '0', '1', '标注员', '2026-08-28 03:50:10', '0', null, null, '0', null);
INSERT INTO `annotation_data` VALUES ('82', '87', null, '划痕', '3', '185.0000', '140.0000', '265.0000', '285.0000', '1.000', '0', '1', '标注员', '2026-08-28 05:14:09', '0', null, null, '0', null);

-- ----------------------------
-- Table structure for api
-- ----------------------------
DROP TABLE IF EXISTS `api`;
CREATE TABLE `api` (
  `id` int NOT NULL AUTO_INCREMENT,
  `is_deleted` decimal(1,0) DEFAULT '0',
  `create_time` datetime DEFAULT NULL,
  `create_id` int DEFAULT NULL,
  `create_name` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  `update_id` int DEFAULT NULL,
  `validity_period` int DEFAULT '-1',
  `validity_times` int DEFAULT '-1',
  `permission_level` tinyint DEFAULT '0',
  `api_key` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `status` decimal(1,0) DEFAULT '1',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
  `counts` int DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `api_key` (`api_key`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=45 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Records of api
-- ----------------------------
INSERT INTO `api` VALUES ('1', '1', '2025-11-22 19:12:50', '1', 'admin3', '2025-11-22 19:12:50', '1', '-1', '-1', '0', 'APIKEY1', '1', null, '0');
INSERT INTO `api` VALUES ('2', '1', '2025-11-22 19:12:50', '2', 'admin2', '2025-11-22 19:12:50', '2', '-1', '-1', '0', 'APIKEY2', '1', null, '0');
INSERT INTO `api` VALUES ('7', '1', null, null, 'admin1', null, null, '-1', '-1', '1', 'ma-6jW2ipsalB', '1', '', '0');
INSERT INTO `api` VALUES ('11', '1', null, null, 'admin1', null, null, '-1', '-1', '2', 'ma-q7vGI1VQ2L', '1', '', '0');
INSERT INTO `api` VALUES ('12', '1', null, null, 'admin1', null, null, '-1', '-1', '1', 'ma-hyV7eyNvN0', '1', '', '0');
INSERT INTO `api` VALUES ('13', '1', null, null, 'admin1', null, null, '-1', '-1', '1', 'ma-kVmN8xfQ0O', '1', '', '0');
INSERT INTO `api` VALUES ('14', '1', null, null, 'admin1', null, null, '-1', '-1', '2', 'ma-YZc6q1UZhx', '1', '', '0');
INSERT INTO `api` VALUES ('15', '1', null, null, 'admin1', null, null, '-1', '-1', '1', 'ma-iB23VvOuBR', '1', '', '0');
INSERT INTO `api` VALUES ('16', '1', null, null, 'admin1', null, null, '-1', '-1', '1', 'ma-ogZ63TzIwd', '1', '', '0');
INSERT INTO `api` VALUES ('17', '1', null, null, 'admin1', null, null, '-1', '-1', '1', 'ma-4Mqtn1YA44', '1', '', '0');
INSERT INTO `api` VALUES ('18', '1', null, null, 'admin1', null, null, '-1', '-1', '1', 'ma-0P8tIsWgQE', '1', '', '0');
INSERT INTO `api` VALUES ('19', '1', null, null, 'admin1', null, null, '-1', '-1', '3', 'ma-w8t2sKNZcU', '1', '', '0');
INSERT INTO `api` VALUES ('20', '1', null, null, 'admin1', null, null, '-1', '-1', '1', 'ma-OehsuD243L', '1', '', '0');
INSERT INTO `api` VALUES ('21', '1', null, null, 'admin1', null, null, '-1', '-1', '1', 'ma-7HGnDvjMx1', '1', '', '0');
INSERT INTO `api` VALUES ('22', '1', null, null, 'admin1', null, null, '-1', '-1', '1', 'ma-GDwxodRzLp', '1', '', '0');
INSERT INTO `api` VALUES ('23', '1', null, null, 'admin1', null, null, '-1', '-1', '1', 'ma-xSchXE2HLy', '1', '', '0');
INSERT INTO `api` VALUES ('24', '1', null, null, 'admin1', null, null, '-1', '-1', '1', 'ma-5fwhyYco8C', '1', '', '0');
INSERT INTO `api` VALUES ('25', '0', null, null, 'admin1', null, null, '-1', '-1', '1', 'ma-OHcvtsG7Ja', '1', '', '0');
INSERT INTO `api` VALUES ('26', '0', null, null, 'admin1', null, null, '-1', '-1', '2', 'ma-ltzaIBG7xo', '1', '', '0');
INSERT INTO `api` VALUES ('27', '0', null, null, 'admin1', null, null, '-1', '-1', '1', 'ma-2WDBdgmGzq', '1', '', '0');
INSERT INTO `api` VALUES ('28', '0', null, null, 'admin1', null, null, '-1', '-1', '1', 'ma-RFWoHA6pZL', '1', '', '0');
INSERT INTO `api` VALUES ('29', '0', null, null, 'admin1', null, null, '-1', '-1', '3', 'ma-uEDvkCVUlx', '1', '', '0');
INSERT INTO `api` VALUES ('30', '0', null, null, 'admin1', null, null, '-1', '4', '1', 'ma-ISl3ebmTa5', '1', '', '0');
INSERT INTO `api` VALUES ('31', '0', null, null, 'admin1', null, null, '-1', '-1', '1', 'ma-UDhU2flgSV', '1', '', '0');
INSERT INTO `api` VALUES ('32', '0', null, null, 'admin1', null, null, '-1', '-1', '1', 'ma-koDDEVoes4', '0', '', '0');
INSERT INTO `api` VALUES ('33', '0', null, null, 'admin1', null, null, '4', '-1', '1', 'ma-RVezHUOfVv', '1', '', '0');
INSERT INTO `api` VALUES ('34', '0', null, null, 'admin1', null, null, '-1', '-1', '1', 'ma-9U0OFrUZgD', '1', '', '0');
INSERT INTO `api` VALUES ('35', '0', null, null, 'admin1', null, null, '-1', '-1', '2', 'ma-LMtbrWafWu', '1', '', '0');
INSERT INTO `api` VALUES ('36', '0', null, null, 'admin1', null, null, '-1', '-1', '1', 'ma-RpZOmyovAt', '1', '', '0');
INSERT INTO `api` VALUES ('37', '0', null, null, 'admin1', null, null, '-1', '-1', '1', 'ma-5xKT4vLX0m', '1', '', '0');
INSERT INTO `api` VALUES ('38', '0', null, null, 'admin1', null, null, '-1', '-1', '1', 'ma-DRnkwnKuC8', '1', '', '0');
INSERT INTO `api` VALUES ('39', '0', null, null, 'admin1', null, null, '-1', '-1', '1', 'ma-zzdRFNhv8w', '1', '', '0');
INSERT INTO `api` VALUES ('40', '0', null, null, 'admin1', null, null, '-1', '-1', '1', 'ma-gnCvpQ2ivd', '1', '', '0');
INSERT INTO `api` VALUES ('41', '0', null, null, 'admin1', null, null, '-1', '-1', '1', 'ma-5pYHcN02IC', '1', '', '0');
INSERT INTO `api` VALUES ('43', '0', null, null, 'admin1', null, null, '30', '1000', '2', 'ma-EBTshsEPDJ', '1', '', '0');
INSERT INTO `api` VALUES ('44', '0', null, null, 'admin1', null, null, '30', '1000', '1', 'ma-1ij6yL2Slp', '1', '', '0');

-- ----------------------------
-- Table structure for defection
-- ----------------------------
DROP TABLE IF EXISTS `defection`;
CREATE TABLE `defection` (
  `id` int NOT NULL AUTO_INCREMENT,
  `l` decimal(10,4) DEFAULT NULL,
  `h` decimal(10,4) DEFAULT NULL,
  `x` decimal(10,4) DEFAULT NULL,
  `y` decimal(10,4) DEFAULT NULL,
  `score` decimal(6,3) DEFAULT NULL,
  `detect_id` int DEFAULT NULL,
  `category` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
  `category_id` tinyint DEFAULT NULL,
  `severity_level` int DEFAULT NULL,
  `repair_suggestion` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
  `data_source` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT 'detection',
  `annotation_id` int DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=95 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Records of defection
-- ----------------------------
INSERT INTO `defection` VALUES ('1', '1.2340', '5.6780', '0.1230', '0.4560', '0.789', '1', '????1', '1', null, null, 'detection', null);
INSERT INTO `defection` VALUES ('2', '3.4560', '7.8900', '0.7890', '0.1230', '0.789', '2', '????2', '2', null, null, 'detection', null);
INSERT INTO `defection` VALUES ('3', '190.9610', '170.6960', '96.0200', '85.6480', '0.902', '3', '点蚀', '3', '5', '严重程度: 5级 | 未知缺陷类型,建议人工检查 | 缺陷面积: 32596.2789', 'detection', null);
INSERT INTO `defection` VALUES ('4', '44.8600', '101.8220', '177.5270', '149.0880', '0.797', '3', '点蚀', '3', '5', '严重程度: 5级 | 未知缺陷类型,建议人工检查 | 缺陷面积: 4567.7349', 'detection', null);
INSERT INTO `defection` VALUES ('5', '66.8290', '27.8920', '105.5160', '185.9950', '0.565', '3', '点蚀', '3', '4', '严重程度: 4级 | 未知缺陷类型,建议人工检查 | 缺陷面积: 1863.9945', 'detection', null);
INSERT INTO `defection` VALUES ('6', '171.9290', '155.1520', '86.0350', '78.4230', '0.881', '4', '划痕', '4', '4', '严重程度: 4级 | 中度划痕,需要打磨后重新喷涂 | 缺陷面积: 26675.1282', 'detection', null);
INSERT INTO `defection` VALUES ('7', '297.0000', '287.0000', '148.5000', '143.5000', '0.500', '5', '划痕', '4', '3', '严重程度: 3级 | 中度划痕,需要打磨后重新喷涂 | 缺陷面积: 85239.0000', 'detection', null);
INSERT INTO `defection` VALUES ('8', '473.0000', '491.0720', '236.5000', '245.5360', '0.431', '6', '划痕', '4', '3', '严重程度: 3级 | 中度划痕,需要打磨后重新喷涂 | 缺陷面积: 232277.0560', 'detection', null);
INSERT INTO `defection` VALUES ('9', '662.1210', '327.9330', '2692.9380', '3868.0330', '0.812', '7', '划痕', '4', '4', '严重程度: 4级 | 中度划痕,需要打磨后重新喷涂 | 缺陷面积: 217131.3259', 'detection', null);
INSERT INTO `defection` VALUES ('10', '334.4300', '853.0690', '167.2150', '2906.8520', '0.443', '7', '斑块', '5', '4', '严重程度: 4级 | 未知缺陷类型,建议人工检查 | 缺陷面积: 285291.8657', 'detection', null);
INSERT INTO `defection` VALUES ('11', '171.9290', '155.1520', '86.0350', '78.4230', '0.881', '8', '划痕', '4', '4', '严重程度: 4级 | 中度划痕,需要打磨后重新喷涂 | 缺陷面积: 26675.1282', 'detection', null);
INSERT INTO `defection` VALUES ('12', '40.5320', '114.2670', '179.7330', '57.4730', '0.944', '37', '斑块', '5', '5', '严重程度: 5级 | 未知缺陷类型,建议人工检查 | 缺陷面积: 4631.4700', 'detection', null);
INSERT INTO `defection` VALUES ('13', '44.2940', '67.4840', '51.2540', '36.2960', '0.911', '37', '斑块', '5', '5', '严重程度: 5级 | 未知缺陷类型,建议人工检查 | 缺陷面积: 2989.1363', 'detection', null);
INSERT INTO `defection` VALUES ('14', '89.4100', '86.9500', '78.1170', '156.5240', '0.896', '37', '斑块', '5', '5', '严重程度: 5级 | 未知缺陷类型,建议人工检查 | 缺陷面积: 7774.1995', 'detection', null);
INSERT INTO `defection` VALUES ('15', '30.2150', '23.4650', '184.8750', '188.2670', '0.847', '37', '斑块', '5', '5', '严重程度: 5级 | 未知缺陷类型,建议人工检查 | 缺陷面积: 708.9950', 'detection', null);
INSERT INTO `defection` VALUES ('16', '16.9380', '39.8040', '8.4690', '19.9020', '0.570', '37', '斑块', '5', '4', '严重程度: 4级 | 未知缺陷类型,建议人工检查 | 缺陷面积: 674.2002', 'detection', null);
INSERT INTO `defection` VALUES ('17', '77.1890', '69.3140', '117.5020', '42.2080', '0.514', '37', '斑块', '5', '4', '严重程度: 4级 | 未知缺陷类型,建议人工检查 | 缺陷面积: 5350.2783', 'detection', null);
INSERT INTO `defection` VALUES ('18', '75.6560', '70.1060', '116.3540', '41.4020', '0.496', '37', '斑块', '5', '4', '严重程度: 4级 | 未知缺陷类型,建议人工检查 | 缺陷面积: 5303.9395', 'detection', null);
INSERT INTO `defection` VALUES ('19', '171.9290', '155.1520', '86.0350', '78.4230', '0.881', '39', '划痕', '4', '4', '严重程度: 4级 | 中度划痕,需要打磨后重新喷涂 | 缺陷面积: 26675.1282', 'detection', null);
INSERT INTO `defection` VALUES ('20', '286.5890', '586.8590', '403.4430', '293.5700', '0.887', '40', '点蚀', '3', '5', '严重程度: 5级 | 未知缺陷类型,建议人工检查 | 缺陷面积: 168187.3340', 'detection', null);
INSERT INTO `defection` VALUES ('21', '659.1500', '716.5460', '688.3220', '857.2420', '0.494', '41', '划痕', '4', '3', '严重程度: 3级 | 中度划痕,需要打磨后重新喷涂 | 缺陷面积: 472311.2959', 'detection', null);
INSERT INTO `defection` VALUES ('22', '659.1500', '716.5460', '688.3220', '857.2420', '0.494', '42', '划痕', '4', '3', '严重程度: 3级 | 中度划痕,需要打磨后重新喷涂 | 缺陷面积: 472311.2959', 'detection', null);
INSERT INTO `defection` VALUES ('23', '248.0000', '227.7370', '124.0000', '113.9260', '0.765', '43', '划痕', '4', '4', '严重程度: 4级 | 中度划痕,需要打磨后重新喷涂 | 缺陷面积: 56478.7760', 'detection', null);
INSERT INTO `defection` VALUES ('24', '123.2630', '269.0000', '171.6330', '134.5000', '0.516', '44', '点蚀', '3', '4', '严重程度: 4级 | 未知缺陷类型,建议人工检查 | 缺陷面积: 33157.7470', 'detection', null);
INSERT INTO `defection` VALUES ('25', '104.3350', '269.0000', '52.5130', '134.5000', '0.404', '44', '斑块', '5', '4', '严重程度: 4级 | 未知缺陷类型,建议人工检查 | 缺陷面积: 28066.1150', 'detection', null);
INSERT INTO `defection` VALUES ('26', '215.9820', '201.0370', '107.9910', '101.6710', '0.823', '45', '点蚀', '3', '5', '严重程度: 5级 | 未知缺陷类型,建议人工检查 | 缺陷面积: 43420.3733', 'detection', null);
INSERT INTO `defection` VALUES ('27', '165.9900', '150.2770', '82.9950', '76.4970', '0.631', '46', '划痕', '4', '4', '严重程度: 4级 | 中度划痕,需要打磨后重新喷涂 | 缺陷面积: 24944.4792', 'detection', null);
INSERT INTO `defection` VALUES ('28', '269.0000', '243.7080', '134.5000', '123.1210', '0.904', '47', '点蚀', '3', '5', '严重程度: 5级 | 未知缺陷类型,建议人工检查 | 缺陷面积: 65557.4520', 'detection', null);
INSERT INTO `defection` VALUES ('29', '230.0000', '205.7600', '115.0000', '104.3790', '0.605', '48', '点蚀', '3', '4', '严重程度: 4级 | 未知缺陷类型,建议人工检查 | 缺陷面积: 47324.8000', 'detection', null);
INSERT INTO `defection` VALUES ('30', '165.0200', '167.0000', '82.5100', '83.5000', '0.856', '49', '划痕', '4', '4', '严重程度: 4级 | 中度划痕,需要打磨后重新喷涂 | 缺陷面积: 27558.3400', 'detection', null);
INSERT INTO `defection` VALUES ('31', '138.9020', '127.8000', '69.4510', '63.9000', '0.913', '50', '划痕', '4', '4', '严重程度: 4级 | 中度划痕,需要打磨后重新喷涂 | 缺陷面积: 17751.6756', 'detection', null);
INSERT INTO `defection` VALUES ('32', '113.0000', '101.5050', '56.5000', '52.1800', '0.464', '51', '点蚀', '3', '4', '严重程度: 4级 | 未知缺陷类型,建议人工检查 | 缺陷面积: 11470.0650', 'detection', null);
INSERT INTO `defection` VALUES ('33', '125.8570', '136.0000', '65.1200', '68.0000', '0.784', '52', '划痕', '4', '4', '严重程度: 4级 | 中度划痕,需要打磨后重新喷涂 | 缺陷面积: 17116.5520', 'detection', null);
INSERT INTO `defection` VALUES ('34', '230.0000', '205.7600', '115.0000', '104.3790', '0.605', '55', '裂痕', '6', '4', '严重程度: 4级 | 未知缺陷类型,建议人工检查 | 缺陷面积: 47324.8000', 'detection', null);
INSERT INTO `defection` VALUES ('35', '67.1780', '107.7770', '76.5940', '136.9200', '0.944', '56', '斑块', '5', '5', '严重程度: 5级 | 未知缺陷类型,建议人工检查 | 缺陷面积: 7240.2433', 'detection', null);
INSERT INTO `defection` VALUES ('36', '59.4360', '54.6290', '163.0380', '152.8790', '0.902', '56', '斑块', '5', '5', '严重程度: 5级 | 未知缺陷类型,建议人工检查 | 缺陷面积: 3246.9292', 'detection', null);
INSERT INTO `defection` VALUES ('37', '28.7880', '46.3700', '73.1270', '31.5910', '0.686', '56', '斑块', '5', '5', '严重程度: 5级 | 未知缺陷类型,建议人工检查 | 缺陷面积: 1334.8996', 'detection', null);
INSERT INTO `defection` VALUES ('38', '60.7390', '75.8680', '106.1020', '162.0650', '0.922', '57', '斑块', '5', '5', '严重程度: 5级 | 未知缺陷类型,建议人工检查 | 缺陷面积: 4608.1465', 'detection', null);
INSERT INTO `defection` VALUES ('39', '61.7170', '137.9150', '95.7140', '68.9570', '0.909', '57', '斑块', '5', '5', '严重程度: 5级 | 未知缺陷类型,建议人工检查 | 缺陷面积: 8511.7001', 'detection', null);
INSERT INTO `defection` VALUES ('40', '50.8160', '46.6640', '174.5860', '23.3320', '0.614', '57', '斑块', '5', '4', '严重程度: 4级 | 未知缺陷类型,建议人工检查 | 缺陷面积: 2371.2778', 'detection', null);
INSERT INTO `defection` VALUES ('41', '50.7990', '46.5380', '174.6000', '23.2820', '0.465', '57', '斑块', '5', '4', '严重程度: 4级 | 未知缺陷类型,建议人工检查 | 缺陷面积: 2364.0839', 'detection', null);
INSERT INTO `defection` VALUES ('42', '74.4640', '77.9900', '58.6880', '93.9660', '0.920', '58', '斑块', '5', '5', '严重程度: 5级 | 未知缺陷类型,建议人工检查 | 缺陷面积: 5807.4474', 'detection', null);
INSERT INTO `defection` VALUES ('43', '39.2780', '48.7070', '148.6240', '133.2970', '0.857', '58', '斑块', '5', '5', '严重程度: 5级 | 未知缺陷类型,建议人工检查 | 缺陷面积: 1913.1135', 'detection', null);
INSERT INTO `defection` VALUES ('44', '82.0100', '73.8940', '118.2160', '161.3590', '0.699', '59', '氧化', '7', '5', '严重程度: 5级 | 未知缺陷类型,建议人工检查 | 缺陷面积: 6060.0469', 'detection', null);
INSERT INTO `defection` VALUES ('45', '78.4450', '64.7790', '107.4980', '93.9500', '0.674', '59', '氧化', '7', '5', '严重程度: 5级 | 未知缺陷类型,建议人工检查 | 缺陷面积: 5081.5887', 'detection', null);
INSERT INTO `defection` VALUES ('46', '66.7720', '87.0300', '47.0240', '44.8070', '0.940', '60', '斑块', '5', '5', '严重程度: 5级 | 未知缺陷类型,建议人工检查 | 缺陷面积: 5811.1672', 'detection', null);
INSERT INTO `defection` VALUES ('47', '57.9210', '52.6960', '155.9710', '94.5910', '0.911', '60', '斑块', '5', '5', '严重程度: 5级 | 未知缺陷类型,建议人工检查 | 缺陷面积: 3052.2050', 'detection', null);
INSERT INTO `defection` VALUES ('48', '77.2560', '39.5170', '39.4140', '180.1150', '0.816', '60', '斑块', '5', '5', '严重程度: 5级 | 未知缺陷类型,建议人工检查 | 缺陷面积: 3052.9254', 'detection', null);
INSERT INTO `defection` VALUES ('49', '58.9350', '36.2720', '46.3140', '121.7960', '0.812', '60', '斑块', '5', '5', '严重程度: 5级 | 未知缺陷类型,建议人工检查 | 缺陷面积: 2137.6903', 'detection', null);
INSERT INTO `defection` VALUES ('50', '167.4370', '69.5170', '103.4210', '165.2410', '0.648', '61', '开裂', '8', '4', '严重程度: 4级 | 未知缺陷类型,建议人工检查 | 缺陷面积: 11639.7179', 'detection', null);
INSERT INTO `defection` VALUES ('51', '198.6160', '73.0970', '100.3190', '36.5480', '0.430', '61', '开裂', '8', '4', '严重程度: 4级 | 未知缺陷类型,建议人工检查 | 缺陷面积: 14518.2338', 'detection', null);
INSERT INTO `defection` VALUES ('52', '169.0950', '157.4540', '99.0190', '121.2590', '0.493', '62', '氧化', '7', '4', '严重程度: 4级 | 未知缺陷类型,建议人工检查 | 缺陷面积: 26624.6841', 'detection', null);
INSERT INTO `defection` VALUES ('53', '165.4800', '73.8790', '82.7400', '50.2240', '0.742', '63', '开裂', '8', '5', '严重程度: 5级 | 未知缺陷类型,建议人工检查 | 缺陷面积: 12225.4969', 'detection', null);
INSERT INTO `defection` VALUES ('54', '191.2080', '59.5340', '103.4860', '67.7050', '0.616', '64', '开裂', '8', '4', '严重程度: 4级 | 未知缺陷类型,建议人工检查 | 缺陷面积: 11383.3771', 'detection', null);
INSERT INTO `defection` VALUES ('55', '171.4450', '77.5940', '113.0390', '140.5320', '0.476', '64', '开裂', '8', '4', '严重程度: 4级 | 未知缺陷类型,建议人工检查 | 缺陷面积: 13303.1033', 'detection', null);
INSERT INTO `defection` VALUES ('56', '63.7320', '121.3740', '166.0100', '139.3120', '0.949', '65', '斑块', '5', '5', '严重程度: 5级 | 未知缺陷类型,建议人工检查 | 缺陷面积: 7735.4078', 'detection', null);
INSERT INTO `defection` VALUES ('57', '51.6350', '69.7180', '99.4210', '130.2740', '0.891', '65', '斑块', '5', '5', '严重程度: 5级 | 未知缺陷类型,建议人工检查 | 缺陷面积: 3599.8889', 'detection', null);
INSERT INTO `defection` VALUES ('58', '59.2000', '70.4500', '94.8910', '37.3780', '0.735', '65', '斑块', '5', '5', '严重程度: 5级 | 未知缺陷类型,建议人工检查 | 缺陷面积: 4170.6400', 'detection', null);
INSERT INTO `defection` VALUES ('59', '57.9470', '41.3940', '144.1550', '57.0100', '0.459', '65', '斑块', '5', '4', '严重程度: 4级 | 未知缺陷类型,建议人工检查 | 缺陷面积: 2398.6581', 'detection', null);
INSERT INTO `defection` VALUES ('60', '42.3100', '43.9710', '166.8590', '22.0490', '0.787', '66', '斑块', '5', '5', '严重程度: 5级 | 未知缺陷类型,建议人工检查 | 缺陷面积: 1860.4130', 'detection', null);
INSERT INTO `defection` VALUES ('61', '77.0750', '198.7370', '38.7110', '99.3680', '0.771', '66', '斑块', '5', '5', '严重程度: 5级 | 未知缺陷类型,建议人工检查 | 缺陷面积: 15317.6543', 'detection', null);
INSERT INTO `defection` VALUES ('62', '86.0860', '115.3870', '156.9560', '142.3060', '0.751', '66', '斑块', '5', '5', '严重程度: 5级 | 未知缺陷类型,建议人工检查 | 缺陷面积: 9933.2053', 'detection', null);
INSERT INTO `defection` VALUES ('63', '817.4000', '1075.0770', '2179.1930', '938.3150', '0.802', '67', '划痕', '4', '4', '严重程度: 4级 | 中度划痕,需要打磨后重新喷涂 | 缺陷面积: 878767.9398', 'detection', null);
INSERT INTO `defection` VALUES ('64', '202.9330', '502.4920', '571.0970', '251.2460', '0.902', '68', '划痕', '4', '4', '严重程度: 4级 | 中度划痕,需要打磨后重新喷涂 | 缺陷面积: 101972.2090', 'detection', null);
INSERT INTO `defection` VALUES ('65', '416.8460', '839.1250', '696.6450', '985.6800', '0.849', '68', '划痕', '4', '4', '严重程度: 4级 | 中度划痕,需要打磨后重新喷涂 | 缺陷面积: 349785.8998', 'detection', null);
INSERT INTO `defection` VALUES ('66', '230.5230', '540.2670', '285.9810', '820.8510', '0.406', '68', '划痕', '4', '3', '严重程度: 3级 | 中度划痕,需要打磨后重新喷涂 | 缺陷面积: 124543.9696', 'detection', null);
INSERT INTO `defection` VALUES ('67', '415.2600', '842.6130', '550.5270', '423.2600', '0.547', '69', '划痕', '4', '3', '严重程度: 3级 | 中度划痕,需要打磨后重新喷涂 | 缺陷面积: 349903.4744', 'detection', null);
INSERT INTO `defection` VALUES ('68', '62.5940', '231.1610', '673.7310', '479.9350', '0.487', '72', '划痕', '4', '3', '严重程度: 3级 | 中度划痕,需要打磨后重新喷涂 | 缺陷面积: 14469.2916', 'detection', null);
INSERT INTO `defection` VALUES ('69', '399.9760', '812.0060', '859.4240', '413.1940', '0.548', '73', '划痕', '4', '3', '严重程度: 3级 | 中度划痕,需要打磨后重新喷涂 | 缺陷面积: 324782.9119', 'detection', null);
INSERT INTO `defection` VALUES ('70', '23.1670', '199.5000', '132.4120', '99.7560', '0.872', '74', '划痕', '4', '4', '严重程度: 4级 | 中度划痕,需要打磨后重新喷涂 | 缺陷面积: 4621.8165', 'detection', null);
INSERT INTO `defection` VALUES ('71', '35.5510', '199.9290', '182.2020', '99.9640', '0.759', '74', '夹杂', '9', '5', '严重程度: 5级 | 未知缺陷类型,建议人工检查 | 缺陷面积: 7107.6759', 'detection', null);
INSERT INTO `defection` VALUES ('72', '19.7620', '47.1330', '108.3560', '109.4360', '0.899', '75', '夹杂', '9', '5', '严重程度: 5级 | 未知缺陷类型,建议人工检查 | 缺陷面积: 931.4423', 'detection', null);
INSERT INTO `defection` VALUES ('73', '45.8720', '184.2320', '92.0200', '106.8990', '0.770', '75', '夹杂', '9', '5', '严重程度: 5级 | 未知缺陷类型,建议人工检查 | 缺陷面积: 8451.0903', 'detection', null);
INSERT INTO `defection` VALUES ('74', '17.7170', '50.2650', '8.8580', '157.8360', '0.701', '75', '夹杂', '9', '5', '严重程度: 5级 | 未知缺陷类型,建议人工检查 | 缺陷面积: 890.5450', 'detection', null);
INSERT INTO `defection` VALUES ('75', '25.3560', '45.4020', '70.3560', '161.2710', '0.948', '76', '夹杂', '9', '5', '严重程度: 5级 | 未知缺陷类型,建议人工检查 | 缺陷面积: 1151.2131', 'detection', null);
INSERT INTO `defection` VALUES ('76', '25.8670', '56.5530', '81.4110', '98.2210', '0.793', '76', '夹杂', '9', '5', '严重程度: 5级 | 未知缺陷类型,建议人工检查 | 缺陷面积: 1462.8565', 'detection', null);
INSERT INTO `defection` VALUES ('77', '19.8270', '198.9440', '121.0710', '99.4720', '0.793', '77', '划痕', '4', '4', '严重程度: 4级 | 中度划痕,需要打磨后重新喷涂 | 缺陷面积: 3944.4627', 'detection', null);
INSERT INTO `defection` VALUES ('78', '36.1780', '168.2340', '78.5010', '84.5600', '0.420', '77', '划痕', '4', '3', '严重程度: 3级 | 中度划痕,需要打磨后重新喷涂 | 缺陷面积: 6086.3697', 'detection', null);
INSERT INTO `defection` VALUES ('79', '62.0860', '161.3040', '168.9560', '119.0950', '0.699', '78', '划痕', '4', '4', '严重程度: 4级 | 中度划痕,需要打磨后重新喷涂 | 缺陷面积: 10014.7201', 'detection', null);
INSERT INTO `defection` VALUES ('80', '62.2010', '162.8240', '168.8990', '116.6920', '0.477', '78', '划痕', '4', '3', '严重程度: 3级 | 中度划痕,需要打磨后重新喷涂 | 缺陷面积: 10127.8156', 'detection', null);
INSERT INTO `defection` VALUES ('81', '31.1220', '199.7220', '141.3200', '99.8610', '0.926', '79', '划痕', '4', '4', '严重程度: 4级 | 中度划痕,需要打磨后重新喷涂 | 缺陷面积: 6215.7481', 'detection', null);
INSERT INTO `defection` VALUES ('82', '19.3250', '152.7590', '20.6670', '123.6200', '0.579', '79', '划痕', '4', '3', '严重程度: 3级 | 中度划痕,需要打磨后重新喷涂 | 缺陷面积: 2952.0677', 'detection', null);
INSERT INTO `defection` VALUES ('83', '74.1180', '171.8860', '104.4120', '114.0280', '0.938', '80', '斑块', '10', '5', '严重程度: 5级 | 未知缺陷类型,建议人工检查 | 缺陷面积: 12739.8465', 'detection', null);
INSERT INTO `defection` VALUES ('84', '32.4710', '59.5390', '183.7640', '123.2790', '0.848', '80', '斑块', '10', '5', '严重程度: 5级 | 未知缺陷类型,建议人工检查 | 缺陷面积: 1933.2909', 'detection', null);
INSERT INTO `defection` VALUES ('85', '138.9440', '93.4060', '98.3230', '66.6110', '0.612', '81', '开裂', '11', '4', '严重程度: 4级 | 未知缺陷类型,建议人工检查 | 缺陷面积: 12978.2033', 'detection', null);
INSERT INTO `defection` VALUES ('86', '142.5060', '55.5880', '71.4320', '171.9770', '0.482', '81', '开裂', '11', '4', '严重程度: 4级 | 未知缺陷类型,建议人工检查 | 缺陷面积: 7921.6235', 'detection', null);
INSERT INTO `defection` VALUES ('87', '145.6140', '76.6440', '123.2840', '161.5200', '0.681', '82', '开裂', '11', '5', '严重程度: 5级 | 未知缺陷类型,建议人工检查 | 缺陷面积: 11160.4394', 'detection', null);
INSERT INTO `defection` VALUES ('88', '139.9140', '72.1600', '70.7850', '150.0680', '0.883', '83', '开裂', '11', '5', '严重程度: 5级 | 未知缺陷类型,建议人工检查 | 缺陷面积: 10096.1942', 'detection', null);
INSERT INTO `defection` VALUES ('89', '49.8520', '120.6500', '100.7700', '60.3940', '0.877', '84', '划痕', '4', '4', '严重程度: 4级 | 中度划痕,需要打磨后重新喷涂 | 缺陷面积: 6014.6438', 'detection', null);
INSERT INTO `defection` VALUES ('90', '16.7420', '199.1190', '147.5650', '99.5600', '0.653', '84', '划痕', '4', '4', '严重程度: 4级 | 中度划痕,需要打磨后重新喷涂 | 缺陷面积: 3333.6503', 'detection', null);
INSERT INTO `defection` VALUES ('91', '33.6220', '118.8730', '124.7350', '140.5630', '0.537', '86', '裂痕', '6', '4', '严重程度: 4级 | 未知缺陷类型,建议人工检查 | 缺陷面积: 3996.7480', 'detection', null);
INSERT INTO `defection` VALUES ('92', '32.5900', '118.6080', '125.1970', '140.6950', '0.316', '86', '裂痕', '6', '3', '严重程度: 3级 | 未知缺陷类型,建议人工检查 | 缺陷面积: 3865.4347', 'detection', null);
INSERT INTO `defection` VALUES ('93', '31.5830', '27.5970', '112.3620', '13.8170', '0.300', '86', '裂痕', '6', '3', '严重程度: 3级 | 未知缺陷类型,建议人工检查 | 缺陷面积: 871.5961', 'detection', null);
INSERT INTO `defection` VALUES ('94', '34.0840', '65.7080', '115.2070', '32.8540', '0.296', '86', '裂痕', '6', '3', '严重程度: 3级 | 未知缺陷类型,建议人工检查 | 缺陷面积: 2239.5915', 'detection', null);

-- ----------------------------
-- Table structure for defection_category
-- ----------------------------
DROP TABLE IF EXISTS `defection_category`;
CREATE TABLE `defection_category` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
  `count` int DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Records of defection_category
-- ----------------------------
INSERT INTO `defection_category` VALUES ('4', '划痕', '0', '2025-11-26 16:42:57');
INSERT INTO `defection_category` VALUES ('6', '裂痕', '0', '2025-12-03 11:41:12');
INSERT INTO `defection_category` VALUES ('9', '夹杂', '0', '2025-12-21 10:49:05');
INSERT INTO `defection_category` VALUES ('10', '斑块', '0', '2025-12-21 10:50:59');
INSERT INTO `defection_category` VALUES ('11', '开裂', '0', '2025-12-21 10:51:09');

-- ----------------------------
-- Table structure for detect_log
-- ----------------------------
DROP TABLE IF EXISTS `detect_log`;
CREATE TABLE `detect_log` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
  `defections_sum` tinyint DEFAULT '0',
  `time` datetime DEFAULT NULL,
  `work_order_id` int DEFAULT NULL,
  `storage_path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
  `data_source` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT 'detection',
  `raw_image_id` int DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `name` (`name`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=87 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Records of detect_log
-- ----------------------------
INSERT INTO `detect_log` VALUES ('56', '0ab078f3_130d_4b87_a1cf_d1fd6756e87e_20251208131625.jpeg', '3', '2025-12-08 13:16:25', '5', './detectPicture\\5\\0ab078f3_130d_4b87_a1cf_d1fd6756e87e_20251208131625.jpeg', 'detection', null);
INSERT INTO `detect_log` VALUES ('57', '6a190cad_e9ed_4995_989f_e5a6013919fe_20251208131855.jpeg', '4', '2025-12-08 13:18:55', '5', './detectPicture\\5\\6a190cad_e9ed_4995_989f_e5a6013919fe_20251208131855.jpeg', 'detection', null);
INSERT INTO `detect_log` VALUES ('58', 'f4e1a0f7_5f82_4973_97ec_1bb7626a34c9_20251208131900.jpeg', '2', '2025-12-08 13:19:01', '5', './detectPicture\\5\\f4e1a0f7_5f82_4973_97ec_1bb7626a34c9_20251208131900.jpeg', 'detection', null);
INSERT INTO `detect_log` VALUES ('59', 'bd68663a_eda9_4974_afd3_5fd4fc9666cc_20251208131904.jpeg', '2', '2025-12-08 13:19:04', '5', './detectPicture\\5\\bd68663a_eda9_4974_afd3_5fd4fc9666cc_20251208131904.jpeg', 'detection', null);
INSERT INTO `detect_log` VALUES ('60', '0fb2c613_3894_4edb_903f_aeb8243a100e_20251208131908.jpeg', '4', '2025-12-08 13:19:09', '5', './detectPicture\\5\\0fb2c613_3894_4edb_903f_aeb8243a100e_20251208131908.jpeg', 'detection', null);
INSERT INTO `detect_log` VALUES ('61', '9894c712_509a_49ea_81a2_098a8c67edcb_20251208131916.jpeg', '2', '2025-12-08 13:19:17', '5', './detectPicture\\5\\9894c712_509a_49ea_81a2_098a8c67edcb_20251208131916.jpeg', 'detection', null);
INSERT INTO `detect_log` VALUES ('62', '50b19be7_86fe_4315_b150_f6fcff289894_20251208131920.jpeg', '1', '2025-12-08 13:19:20', '5', './detectPicture\\5\\50b19be7_86fe_4315_b150_f6fcff289894_20251208131920.jpeg', 'detection', null);
INSERT INTO `detect_log` VALUES ('63', '830d3a9c_e911_4b22_b76b_4628f4bdd767_20251208131922.jpeg', '1', '2025-12-08 13:19:23', '5', './detectPicture\\5\\830d3a9c_e911_4b22_b76b_4628f4bdd767_20251208131922.jpeg', 'detection', null);
INSERT INTO `detect_log` VALUES ('64', '7fb34ca1_6c96_42bf_ac38_af1855431c62_20251208131926.jpeg', '2', '2025-12-08 13:19:26', '5', './detectPicture\\5\\7fb34ca1_6c96_42bf_ac38_af1855431c62_20251208131926.jpeg', 'detection', null);
INSERT INTO `detect_log` VALUES ('65', 'e164da7b_cc31_4040_866d_30b0ecf31707_20251208131930.jpeg', '4', '2025-12-08 13:19:30', '5', './detectPicture\\5\\e164da7b_cc31_4040_866d_30b0ecf31707_20251208131930.jpeg', 'detection', null);
INSERT INTO `detect_log` VALUES ('66', '15e61cb7_afea_49b3_8955_ae01caf280c2_20251208131933.jpeg', '3', '2025-12-08 13:19:33', '5', './detectPicture\\5\\15e61cb7_afea_49b3_8955_ae01caf280c2_20251208131933.jpeg', 'detection', null);
INSERT INTO `detect_log` VALUES ('67', '897fa5a1_9c53_4320_93eb_d4c32419d4d9_20251220172541.jpeg', '1', '2025-12-20 17:25:41', '2', './detectPicture\\2\\897fa5a1_9c53_4320_93eb_d4c32419d4d9_20251220172541.jpeg', 'detection', null);
INSERT INTO `detect_log` VALUES ('68', 'bf2681d5_9a37_43b3_92e0_07223016d378_20251221103422.jpeg', '3', '2025-12-21 10:34:23', '2', './detectPicture\\2\\bf2681d5_9a37_43b3_92e0_07223016d378_20251221103422.jpeg', 'detection', null);
INSERT INTO `detect_log` VALUES ('69', 'd1662002_0657_4618_b9c8_29704da3b911_20251221103453.jpeg', '1', '2025-12-21 10:34:54', '2', './detectPicture\\2\\d1662002_0657_4618_b9c8_29704da3b911_20251221103453.jpeg', 'detection', null);
INSERT INTO `detect_log` VALUES ('70', '48b09490_ca63_46dc_bcf0_e5b1631c04c2_20251221103514.jpeg', '0', '2025-12-21 10:35:14', '2', './detectPicture\\2\\48b09490_ca63_46dc_bcf0_e5b1631c04c2_20251221103514.jpeg', 'detection', null);
INSERT INTO `detect_log` VALUES ('72', 'b1d52385_af55_48a0_b791_c7c18ded3f4b_20251221103620.jpeg', '1', '2025-12-21 10:36:20', '2', './detectPicture\\2\\b1d52385_af55_48a0_b791_c7c18ded3f4b_20251221103620.jpeg', 'detection', null);
INSERT INTO `detect_log` VALUES ('73', '98d5a83a_c533_4197_85e6_9ada25be5840_20251221103638.jpeg', '1', '2025-12-21 10:36:39', '2', './detectPicture\\2\\98d5a83a_c533_4197_85e6_9ada25be5840_20251221103638.jpeg', 'detection', null);
INSERT INTO `detect_log` VALUES ('74', '55e3a3b2_4b21_4076_aac5_b78daa87face_20251221104904.jpeg', '2', '2025-12-21 10:49:05', '2', './detectPicture\\2\\55e3a3b2_4b21_4076_aac5_b78daa87face_20251221104904.jpeg', 'detection', null);
INSERT INTO `detect_log` VALUES ('75', '6dfe03cd_0aaf_45de_bcb7_d7b2a8f0e93f_20251221104944.jpeg', '3', '2025-12-21 10:49:44', '2', './detectPicture\\2\\6dfe03cd_0aaf_45de_bcb7_d7b2a8f0e93f_20251221104944.jpeg', 'detection', null);
INSERT INTO `detect_log` VALUES ('76', '40a5fccf_576a_4df4_b339_29699c9a72f7_20251221104953.jpeg', '2', '2025-12-21 10:49:53', '2', './detectPicture\\2\\40a5fccf_576a_4df4_b339_29699c9a72f7_20251221104953.jpeg', 'detection', null);
INSERT INTO `detect_log` VALUES ('77', 'd26e56c9_792c_443b_af66_d376062d1062_20251221105002.jpeg', '2', '2025-12-21 10:50:03', '2', './detectPicture\\2\\d26e56c9_792c_443b_af66_d376062d1062_20251221105002.jpeg', 'detection', null);
INSERT INTO `detect_log` VALUES ('78', '612f0b89_d2c4_4f15_891e_39ff461cfe81_20251221105019.jpeg', '2', '2025-12-21 10:50:20', '2', './detectPicture\\2\\612f0b89_d2c4_4f15_891e_39ff461cfe81_20251221105019.jpeg', 'detection', null);
INSERT INTO `detect_log` VALUES ('79', '5ef005b6_2aa5_4359_bd37_59d3b418a0b4_20251221105040.jpeg', '2', '2025-12-21 10:50:41', '2', './detectPicture\\2\\5ef005b6_2aa5_4359_bd37_59d3b418a0b4_20251221105040.jpeg', 'detection', null);
INSERT INTO `detect_log` VALUES ('80', 'd5c06976_60b3_429f_90d5_dcec0b157d4a_20251221105059.jpeg', '2', '2025-12-21 10:50:59', '2', './detectPicture\\2\\d5c06976_60b3_429f_90d5_dcec0b157d4a_20251221105059.jpeg', 'detection', null);
INSERT INTO `detect_log` VALUES ('81', 'c41df54d_ee1d_4098_9200_969bd80629f9_20251221105108.jpeg', '2', '2025-12-21 10:51:09', '2', './detectPicture\\2\\c41df54d_ee1d_4098_9200_969bd80629f9_20251221105108.jpeg', 'detection', null);
INSERT INTO `detect_log` VALUES ('82', 'c198bcc3_a9dc_4fd3_b386_18b47714bbb9_20251221105126.jpeg', '1', '2025-12-21 10:51:26', '2', './detectPicture\\2\\c198bcc3_a9dc_4fd3_b386_18b47714bbb9_20251221105126.jpeg', 'detection', null);
INSERT INTO `detect_log` VALUES ('83', '2f633010_613c_48e0_b1af_fc41ccff8537_20251221105144.jpeg', '1', '2025-12-21 10:51:45', '2', './detectPicture\\2\\2f633010_613c_48e0_b1af_fc41ccff8537_20251221105144.jpeg', 'detection', null);
INSERT INTO `detect_log` VALUES ('84', 'e8a3f797_03c6_4b72_b894_10ac30f3a284_20251221105215.jpeg', '2', '2025-12-21 10:52:16', '2', './detectPicture\\2\\e8a3f797_03c6_4b72_b894_10ac30f3a284_20251221105215.jpeg', 'detection', null);
INSERT INTO `detect_log` VALUES ('85', '8c1b2452_d6fc_406f_951f_ce3972c5c605_20260903093905.jpeg', '0', '2026-09-03 09:39:05', '2', './detectPicture\\2\\8c1b2452_d6fc_406f_951f_ce3972c5c605_20260903093905.jpeg', 'detection', null);
INSERT INTO `detect_log` VALUES ('86', '35c53e8c_9fca_4711_82c3_3dd130eb4ed5_20260903185819.jpeg', '4', '2026-09-03 18:58:19', '2', './detectPicture\\2\\35c53e8c_9fca_4711_82c3_3dd130eb4ed5_20260903185819.jpeg', 'detection', null);

-- ----------------------------
-- Table structure for device
-- ----------------------------
DROP TABLE IF EXISTS `device`;
CREATE TABLE `device` (
  `id` smallint NOT NULL AUTO_INCREMENT,
  `type` tinyint NOT NULL,
  `mac` char(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
  `ip` char(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
  `is_deleted` decimal(1,0) DEFAULT '0',
  `create_time` datetime DEFAULT NULL,
  `create_id` int DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  `update_id` int DEFAULT NULL,
  `is_Connect` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Records of device
-- ----------------------------
INSERT INTO `device` VALUES ('1', '1', '00:11:22:33:44:55', '127.0.0.1', 'IP???', '????????', '0', '2025-11-22 19:12:50', '1', '2025-11-22 19:12:50', '1', '0');
INSERT INTO `device` VALUES ('2', '2', 'AA:BB:CC:DD:EE:FF', '192.168.1.2', '??2', '??2', '0', '2025-11-22 19:12:50', '2', '2025-11-22 19:12:50', '2', '0');

-- ----------------------------
-- Table structure for manager
-- ----------------------------
DROP TABLE IF EXISTS `manager`;
CREATE TABLE `manager` (
  `id` smallint NOT NULL AUTO_INCREMENT,
  `account` varchar(11) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `password` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `phone_number` char(11) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
  `name` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
  `is_Online` tinyint(1) DEFAULT '0',
  `warnings_open` tinyint(1) DEFAULT '0',
  `warnings_level` tinyint DEFAULT '0',
  `phone_Way` tinyint(1) DEFAULT '0',
  `email_way` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `account` (`account`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Records of manager
-- ----------------------------
INSERT INTO `manager` VALUES ('1', 'admin1', 'e10adc3949ba59abbe56e057f20f883e', '12345678901', 'admin1', 'admin1@example.com', '0', '1', '0', '0', '1');
INSERT INTO `manager` VALUES ('2', 'admin2', 'e10adc3949ba59abbe56e057f20f883e', '98765432109', 'admin2', 'admin2@example.com', '0', '0', '0', '0', '0');
INSERT INTO `manager` VALUES ('3', 'HUMING', 'e10adc3949ba59abbe56e057f20f883e', '13800138000', 'HM', 'huming@example.com', '1', '0', '0', '0', '0');
INSERT INTO `manager` VALUES ('4', 'CHENKUN', 'e10adc3949ba59abbe56e057f20f883e', '13800138001', 'CK', 'chenkun@example.com', '0', '0', '0', '0', '0');
INSERT INTO `manager` VALUES ('5', 'YEMIN', 'e10adc3949ba59abbe56e057f20f883e', '13800138002', 'YM', 'yemin@example.com', '0', '0', '0', '0', '0');
INSERT INTO `manager` VALUES ('6', 'LIHONGBIN', 'e10adc3949ba59abbe56e057f20f883e', '13800138003', 'LHB', 'lihongbin@example.com', '0', '0', '0', '0', '0');
INSERT INTO `manager` VALUES ('7', 'HEJIA', 'e10adc3949ba59abbe56e057f20f883e', '13800138004', 'HJ', 'hejia@example.com', '0', '0', '0', '0', '0');
INSERT INTO `manager` VALUES ('8', 'QINLONG', 'e10adc3949ba59abbe56e057f20f883e', '13800138005', 'QL', 'qinlong@example.com', '0', '0', '0', '0', '0');

-- ----------------------------
-- Table structure for model
-- ----------------------------
DROP TABLE IF EXISTS `model`;
CREATE TABLE `model` (
  `id` smallint NOT NULL AUTO_INCREMENT,
  `name` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
  `param_storage_path` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `is_deleted` decimal(1,0) DEFAULT '0',
  `create_time` datetime DEFAULT NULL,
  `create_id` int DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  `update_id` int DEFAULT NULL,
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Records of model
-- ----------------------------
INSERT INTO `model` VALUES ('1', '??1', '/path/to/param1', '0', '2025-11-22 19:12:50', '1', '2025-11-22 19:12:50', '1', '??1');
INSERT INTO `model` VALUES ('2', '????', '/path/to/param2', '0', '2025-11-22 19:12:50', '2', '2025-11-22 19:12:50', '2', '??2');

-- ----------------------------
-- Table structure for operator
-- ----------------------------
DROP TABLE IF EXISTS `operator`;
CREATE TABLE `operator` (
  `id` smallint NOT NULL AUTO_INCREMENT,
  `job_id` int DEFAULT NULL,
  `login_pwd` char(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
  `op_pwd` char(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
  `is_deleted` decimal(1,0) DEFAULT '0',
  `create_time` datetime DEFAULT NULL,
  `create_id` int DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  `update_id` int DEFAULT NULL,
  `create_name` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
  `name` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
  `work_order_id` smallint DEFAULT NULL,
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `job_id` (`job_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Records of operator
-- ----------------------------
INSERT INTO `operator` VALUES ('3', '1001', 'M0vpbHBbiX4=', 'M0vpbHBbiX4=', '0', '2025-12-08 13:26:01', null, null, null, 'admin1', '工程师1', null, '工程师1');
INSERT INTO `operator` VALUES ('5', '1002', 'jd9t0YbucoA=', 'YT21WLSTvkA=', '0', '2026-08-31 15:14:57', null, null, null, 'admin1', '操作员1', null, '');
INSERT INTO `operator` VALUES ('6', '1003', 'jd9t0YbucoA=', 'jd9t0YbucoA=', '0', '2026-08-31 15:18:21', null, null, null, 'admin1', '运维工程师', null, null);

-- ----------------------------
-- Table structure for raw_image
-- ----------------------------
DROP TABLE IF EXISTS `raw_image`;
CREATE TABLE `raw_image` (
  `id` int NOT NULL AUTO_INCREMENT,
  `image_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `image_path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `image_size` bigint DEFAULT NULL,
  `image_width` int DEFAULT NULL,
  `image_height` int DEFAULT NULL,
  `upload_source` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT 'camera',
  `device_id` smallint DEFAULT NULL,
  `work_order_id` int DEFAULT NULL,
  `upload_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `upload_user_id` int DEFAULT NULL,
  `upload_user_name` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
  `status` tinyint DEFAULT '0',
  `is_deleted` tinyint DEFAULT '0',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_status` (`status`) USING BTREE,
  KEY `idx_upload_time` (`upload_time`) USING BTREE,
  KEY `idx_work_order` (`work_order_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=94 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Records of raw_image
-- ----------------------------
INSERT INTO `raw_image` VALUES ('53', '12.jpg', 'uploads\\images\\20251127_125642_877.jpg', '16951', null, null, 'camera', null, null, '2025-11-27 12:56:43', null, null, '2', '1', null);
INSERT INTO `raw_image` VALUES ('55', '谭浩航.png', 'uploads\\images\\20251208_101932_170.png', '318106', null, null, 'camera', null, null, '2025-12-08 10:19:32', null, null, '2', '1', null);
INSERT INTO `raw_image` VALUES ('56', '3.jpg', 'uploads\\images\\20251208_102022_044.jpg', '2626620', null, null, 'camera', null, null, '2025-12-08 10:20:22', null, null, '2', '1', null);
INSERT INTO `raw_image` VALUES ('58', '62B124124B181CC706CD81164F288FBF.jpg', 'uploads\\images\\20251208_104711_816.jpg', '2582811', null, null, 'camera', null, null, '2025-12-08 10:47:12', null, null, '0', '1', null);
INSERT INTO `raw_image` VALUES ('59', '2.jpg', 'uploads\\images\\20251208_105714_029.jpg', '2201549', null, null, 'camera', null, null, '2025-12-08 10:57:14', null, null, '2', '1', null);
INSERT INTO `raw_image` VALUES ('60', '谭浩航.png', 'uploads\\images\\20251208_105904_947.png', '318106', null, null, 'camera', null, null, '2025-12-08 10:59:05', null, null, '2', '1', null);
INSERT INTO `raw_image` VALUES ('61', '谭浩航.png', 'uploads\\images\\20251208_111049_354.png', '318106', null, null, 'camera', null, null, '2025-12-08 11:10:49', null, null, '0', '1', null);
INSERT INTO `raw_image` VALUES ('62', 'D1320.jpeg', 'uploads\\images\\20251208_111117_822.jpeg', '431398', null, null, 'camera', null, null, '2025-12-08 11:11:18', null, null, '2', '1', null);
INSERT INTO `raw_image` VALUES ('63', '17.jpg', 'uploads\\images\\20251208_112534_346.jpg', '21065', null, null, 'camera', null, null, '2025-12-08 11:25:34', null, null, '0', '1', null);
INSERT INTO `raw_image` VALUES ('64', '17.jpg', 'uploads\\images\\20251208_115215_490.jpg', '21065', null, null, 'camera', null, null, '2025-12-08 11:52:15', null, null, '0', '1', null);
INSERT INTO `raw_image` VALUES ('65', '9.jpg', 'uploads\\images\\20251208_131645_003.jpg', '17622', null, null, 'camera', null, null, '2025-12-08 13:16:45', null, null, '2', '1', null);
INSERT INTO `raw_image` VALUES ('66', '12.jpg', 'uploads\\images\\20251208_131645_300.jpg', '16951', null, null, 'camera', null, null, '2025-12-08 13:16:45', null, null, '0', '0', null);
INSERT INTO `raw_image` VALUES ('67', '11.jpg', 'uploads\\images\\20251208_131645_312.jpg', '19755', null, null, 'camera', null, null, '2025-12-08 13:16:45', null, null, '0', '0', null);
INSERT INTO `raw_image` VALUES ('68', '16.jpg', 'uploads\\images\\20251208_131645_320.jpg', '22653', null, null, 'camera', null, null, '2025-12-08 13:16:45', null, null, '2', '0', null);
INSERT INTO `raw_image` VALUES ('69', '10.jpg', 'uploads\\images\\20251208_131645_318.jpg', '8118', null, null, 'camera', null, null, '2025-12-08 13:16:45', null, null, '2', '0', null);
INSERT INTO `raw_image` VALUES ('70', '17.jpg', 'uploads\\images\\20251208_131645_320.jpg', '21065', null, null, 'camera', null, null, '2025-12-08 13:16:45', null, null, '2', '1', null);
INSERT INTO `raw_image` VALUES ('71', '13.jpg', 'uploads\\images\\20251208_131645_350.jpg', '7341', null, null, 'camera', null, null, '2025-12-08 13:16:45', null, null, '0', '0', null);
INSERT INTO `raw_image` VALUES ('72', '15.jpg', 'uploads\\images\\20251208_131645_634.jpg', '20844', null, null, 'camera', null, null, '2025-12-08 13:16:46', null, null, '2', '0', null);
INSERT INTO `raw_image` VALUES ('73', '14.jpg', 'uploads\\images\\20251208_131645_664.jpg', '16722', null, null, 'camera', null, null, '2025-12-08 13:16:46', null, null, '0', '0', null);
INSERT INTO `raw_image` VALUES ('74', '154.jpg', 'uploads\\images\\20251208_132010_180.jpg', '10416', null, null, 'camera', null, null, '2025-12-08 13:20:10', null, null, '2', '0', null);
INSERT INTO `raw_image` VALUES ('75', '158.jpg', 'uploads\\images\\20251208_132010_489.jpg', '15926', null, null, 'camera', null, null, '2025-12-08 13:20:11', null, null, '0', '0', null);
INSERT INTO `raw_image` VALUES ('76', '159.jpg', 'uploads\\images\\20251208_132010_489.jpg', '15848', null, null, 'camera', null, null, '2025-12-08 13:20:11', null, null, '0', '0', null);
INSERT INTO `raw_image` VALUES ('77', '155.jpg', 'uploads\\images\\20251208_132010_495.jpg', '21690', null, null, 'camera', null, null, '2025-12-08 13:20:11', null, null, '0', '0', null);
INSERT INTO `raw_image` VALUES ('78', '156.jpg', 'uploads\\images\\20251208_132010_495.jpg', '9990', null, null, 'camera', null, null, '2025-12-08 13:20:11', null, null, '0', '0', null);
INSERT INTO `raw_image` VALUES ('79', '160.jpg', 'uploads\\images\\20251208_132010_525.jpg', '13440', null, null, 'camera', null, null, '2025-12-08 13:20:11', null, null, '0', '0', null);
INSERT INTO `raw_image` VALUES ('80', '162.jpg', 'uploads\\images\\20251208_132010_830.jpg', '23716', null, null, 'camera', null, null, '2025-12-08 13:20:11', null, null, '0', '0', null);
INSERT INTO `raw_image` VALUES ('81', '163.jpg', 'uploads\\images\\20251208_132010_846.jpg', '16423', null, null, 'camera', null, null, '2025-12-08 13:20:11', null, null, '0', '1', null);
INSERT INTO `raw_image` VALUES ('82', '161.jpg', 'uploads\\images\\20251208_132010_841.jpg', '12864', null, null, 'camera', null, null, '2025-12-08 13:20:11', null, null, '0', '0', null);
INSERT INTO `raw_image` VALUES ('83', '164.jpg', 'uploads\\images\\20251208_132010_846.jpg', '11108', null, null, 'camera', null, null, '2025-12-08 13:20:11', null, null, '0', '1', null);
INSERT INTO `raw_image` VALUES ('84', '165.jpg', 'uploads\\images\\20251208_132010_852.jpg', '9144', null, null, 'camera', null, null, '2025-12-08 13:20:11', null, null, '2', '0', null);
INSERT INTO `raw_image` VALUES ('85', '167.jpg', 'uploads\\images\\20251208_132010_870.jpg', '11200', null, null, 'camera', null, null, '2025-12-08 13:20:11', null, null, '0', '0', null);
INSERT INTO `raw_image` VALUES ('86', '169.jpg', 'uploads\\images\\20251208_132011_174.jpg', '19009', null, null, 'camera', null, null, '2025-12-08 13:20:11', null, null, '2', '0', null);
INSERT INTO `raw_image` VALUES ('87', '168.jpg', 'uploads\\images\\20251208_132011_203.jpg', '20066', null, null, 'camera', null, null, '2025-12-08 13:20:11', null, null, '2', '0', null);
INSERT INTO `raw_image` VALUES ('88', '166.jpg', 'uploads\\images\\20251208_132011_203.jpg', '18299', null, null, 'camera', null, null, '2025-12-08 13:20:11', null, null, '0', '0', null);
INSERT INTO `raw_image` VALUES ('89', '170.jpg', 'uploads\\images\\20251208_132011_208.jpg', '21250', null, null, 'camera', null, null, '2025-12-08 13:20:11', null, null, '2', '0', null);
INSERT INTO `raw_image` VALUES ('90', '284e8564-b542-44f8-a638-cecb84675865.png', 'uploads\\images\\20251223_091347_597.png', '98809', null, null, 'camera', null, null, '2025-12-23 09:13:48', null, null, '0', '1', null);
INSERT INTO `raw_image` VALUES ('91', '智疗云_.png', 'uploads\\images\\20260830_170522_671.png', '6935', null, null, 'camera', null, null, '2026-08-30 17:05:23', null, null, '0', '1', null);
INSERT INTO `raw_image` VALUES ('92', '智疗云_.png', 'uploads\\images\\20260830_170710_396.png', '6935', null, null, 'camera', null, null, '2026-08-30 17:07:10', null, null, '0', '1', null);
INSERT INTO `raw_image` VALUES ('93', '智疗云_.png', 'uploads\\images\\20260830_200159_091.png', '6935', null, null, 'camera', null, null, '2026-08-30 20:01:59', null, null, '0', '1', null);

-- ----------------------------
-- Table structure for sys_log
-- ----------------------------
DROP TABLE IF EXISTS `sys_log`;
CREATE TABLE `sys_log` (
  `id` int NOT NULL AUTO_INCREMENT,
  `time` datetime DEFAULT NULL,
  `operation` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
  `operator` smallint DEFAULT NULL,
  `operator_type` decimal(1,0) NOT NULL,
  `target` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
  `resource_type` varchar(64) DEFAULT '常规业务' COMMENT '资源类型',
  `client_ip` varchar(64) DEFAULT '127.0.0.1' COMMENT '终端IP',
  `status` varchar(32) DEFAULT '成功' COMMENT '操作状态',
  `details` varchar(500) DEFAULT '' COMMENT '操作详情',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=247 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Records of sys_log
-- ----------------------------
INSERT INTO `sys_log` VALUES ('1', '2025-11-22 19:12:50', '??1', '1', '1', null, '常规业务', '127.0.0.1', '成功', '');
INSERT INTO `sys_log` VALUES ('2', '2025-11-22 19:12:50', '??2', '2', '2', null, '常规业务', '127.0.0.1', '成功', '');
INSERT INTO `sys_log` VALUES ('3', '2025-11-22 19:27:22', '登录', '1', '0', null, '常规业务', '127.0.0.1', '成功', '');
INSERT INTO `sys_log` VALUES ('4', '2025-11-22 19:30:26', '删除', '1', '0', '检测结果', '常规业务', '127.0.0.1', '成功', '');
INSERT INTO `sys_log` VALUES ('5', '2025-11-22 19:30:28', '删除', '1', '0', '检测结果', '常规业务', '127.0.0.1', '成功', '');
INSERT INTO `sys_log` VALUES ('6', '2025-11-24 18:00:04', '登录', '1', '0', null, '常规业务', '127.0.0.1', '成功', '');
INSERT INTO `sys_log` VALUES ('7', '2025-11-26 14:59:04', '登录', '1', '0', null, '常规业务', '127.0.0.1', '成功', '');
INSERT INTO `sys_log` VALUES ('8', '2025-11-26 15:43:49', '登录', '1', '0', null, '常规业务', '127.0.0.1', '成功', '');
INSERT INTO `sys_log` VALUES ('9', '2025-11-26 16:06:40', '登录', '1', '0', null, '常规业务', '127.0.0.1', '成功', '');
INSERT INTO `sys_log` VALUES ('10', '2025-11-26 16:28:33', '登录', '1', '0', null, '常规业务', '127.0.0.1', '成功', '');
INSERT INTO `sys_log` VALUES ('11', '2025-11-26 16:38:37', '登录', '1', '0', null, '常规业务', '127.0.0.1', '成功', '');
INSERT INTO `sys_log` VALUES ('12', '2025-11-26 17:31:56', '登录', '1', '0', null, '常规业务', '127.0.0.1', '成功', '');
INSERT INTO `sys_log` VALUES ('13', '2025-11-26 17:35:53', '登录', '1', '0', null, '常规业务', '127.0.0.1', '成功', '');
INSERT INTO `sys_log` VALUES ('14', '2025-11-26 17:42:46', '登录', '1', '0', null, '常规业务', '127.0.0.1', '成功', '');
INSERT INTO `sys_log` VALUES ('15', '2025-11-26 17:45:11', '登录', '1', '0', null, '常规业务', '127.0.0.1', '成功', '');
INSERT INTO `sys_log` VALUES ('16', '2025-11-26 18:13:13', '登录', '1', '0', null, '常规业务', '127.0.0.1', '成功', '');
INSERT INTO `sys_log` VALUES ('17', '2025-11-26 18:15:35', '登录', '1', '0', null, '常规业务', '127.0.0.1', '成功', '');
INSERT INTO `sys_log` VALUES ('18', '2025-11-26 18:23:32', '登录', '1', '0', null, '常规业务', '127.0.0.1', '成功', '');
INSERT INTO `sys_log` VALUES ('19', '2025-11-26 18:32:21', '登录', '1', '0', null, '常规业务', '127.0.0.1', '成功', '');
INSERT INTO `sys_log` VALUES ('20', '2025-11-26 18:49:46', '登录', '1', '0', null, '常规业务', '127.0.0.1', '成功', '');
INSERT INTO `sys_log` VALUES ('21', '2025-11-26 19:00:24', '登录', '1', '0', null, '常规业务', '127.0.0.1', '成功', '');
INSERT INTO `sys_log` VALUES ('22', '2025-11-26 19:04:53', '开始', '1', '1', '系统状态', '常规业务', '127.0.0.1', '成功', '');
INSERT INTO `sys_log` VALUES ('23', '2025-11-26 19:05:14', '登录', '1', '0', null, '常规业务', '127.0.0.1', '成功', '');
INSERT INTO `sys_log` VALUES ('24', '2025-11-26 19:11:22', '登录', '1', '0', null, '常规业务', '127.0.0.1', '成功', '');
INSERT INTO `sys_log` VALUES ('25', '2025-11-26 19:19:21', '登录', '1', '0', null, '常规业务', '127.0.0.1', '成功', '');
INSERT INTO `sys_log` VALUES ('26', '2025-11-26 20:20:11', '登录', '1', '0', null, '常规业务', '127.0.0.1', '成功', '');
INSERT INTO `sys_log` VALUES ('27', '2025-11-27 10:20:15', '登录', '1', '0', null, '常规业务', '127.0.0.1', '成功', '');
INSERT INTO `sys_log` VALUES ('28', '2025-11-27 10:42:34', '登录', '1', '0', null, '常规业务', '127.0.0.1', '成功', '');
INSERT INTO `sys_log` VALUES ('29', '2025-11-27 12:24:14', '登录', '1', '0', null, '常规业务', '127.0.0.1', '成功', '');
INSERT INTO `sys_log` VALUES ('30', '2025-12-01 12:14:30', '登录', '1', '0', null, '常规业务', '127.0.0.1', '成功', '');
INSERT INTO `sys_log` VALUES ('31', '2025-12-03 11:37:16', '登录', '1', '0', null, '常规业务', '127.0.0.1', '成功', '');
INSERT INTO `sys_log` VALUES ('32', '2025-12-03 16:53:39', '登录', '1', '0', null, '常规业务', '127.0.0.1', '成功', '');
INSERT INTO `sys_log` VALUES ('33', '2025-12-04 15:58:26', '登录', '1', '0', null, '常规业务', '127.0.0.1', '成功', '');
INSERT INTO `sys_log` VALUES ('34', '2025-12-08 10:14:09', '登录', '1', '0', null, '常规业务', '127.0.0.1', '成功', '');
INSERT INTO `sys_log` VALUES ('35', '2025-12-08 10:16:11', '添加', '1', '0', 'api', '常规业务', '127.0.0.1', '成功', '');
INSERT INTO `sys_log` VALUES ('36', '2025-12-08 10:16:19', '删除', '1', '0', 'api', '常规业务', '127.0.0.1', '成功', '');
INSERT INTO `sys_log` VALUES ('37', '2025-12-08 10:16:28', '添加', '1', '0', 'api', '常规业务', '127.0.0.1', '成功', '');
INSERT INTO `sys_log` VALUES ('38', '2025-12-08 10:16:31', '删除', '1', '0', 'api', '常规业务', '127.0.0.1', '成功', '');
INSERT INTO `sys_log` VALUES ('39', '2025-12-08 10:47:01', '登录', '1', '0', null, '常规业务', '127.0.0.1', '成功', '');
INSERT INTO `sys_log` VALUES ('40', '2025-12-08 10:52:48', '登录', '1', '0', null, '常规业务', '127.0.0.1', '成功', '');
INSERT INTO `sys_log` VALUES ('41', '2025-12-08 10:53:00', '登录', '1', '0', null, '常规业务', '127.0.0.1', '成功', '');
INSERT INTO `sys_log` VALUES ('42', '2025-12-08 10:53:12', '添加', '1', '0', 'api', '常规业务', '127.0.0.1', '成功', '');
INSERT INTO `sys_log` VALUES ('43', '2025-12-08 10:53:34', '删除', '1', '0', 'api', '常规业务', '127.0.0.1', '成功', '');
INSERT INTO `sys_log` VALUES ('44', '2025-12-08 10:53:53', '登录', '1', '0', null, '常规业务', '127.0.0.1', '成功', '');
INSERT INTO `sys_log` VALUES ('45', '2025-12-08 10:56:01', '添加', '1', '0', 'api', '常规业务', '127.0.0.1', '成功', '');
INSERT INTO `sys_log` VALUES ('46', '2025-12-08 11:10:31', '登录', '1', '0', null, '常规业务', '127.0.0.1', '成功', '');
INSERT INTO `sys_log` VALUES ('47', '2025-12-08 11:22:44', '登录', '1', '0', null, '常规业务', '127.0.0.1', '成功', '');
INSERT INTO `sys_log` VALUES ('48', '2025-12-08 11:51:55', '登录', '1', '0', null, '常规业务', '127.0.0.1', '成功', '');
INSERT INTO `sys_log` VALUES ('49', '2025-12-08 12:08:41', '登录', '1', '0', null, '常规业务', '127.0.0.1', '成功', '');
INSERT INTO `sys_log` VALUES ('50', '2025-12-08 12:20:44', '登录', '1', '0', null, '常规业务', '127.0.0.1', '成功', '');
INSERT INTO `sys_log` VALUES ('51', '2025-12-08 12:21:16', '删除', '1', '0', 'api', '常规业务', '127.0.0.1', '成功', '');
INSERT INTO `sys_log` VALUES ('52', '2025-12-08 12:21:27', '登录', '1', '0', null, '常规业务', '127.0.0.1', '成功', '');
INSERT INTO `sys_log` VALUES ('53', '2025-12-08 12:21:32', '添加', '1', '0', 'api', '常规业务', '127.0.0.1', '成功', '');
INSERT INTO `sys_log` VALUES ('54', '2025-12-08 12:21:42', '添加', '1', '0', 'api', '常规业务', '127.0.0.1', '成功', '');
INSERT INTO `sys_log` VALUES ('55', '2025-12-08 12:21:52', '删除', '1', '0', 'api', '常规业务', '127.0.0.1', '成功', '');
INSERT INTO `sys_log` VALUES ('56', '2025-12-08 13:14:00', '登录', '1', '0', null, '常规业务', '127.0.0.1', '成功', '');
INSERT INTO `sys_log` VALUES ('57', '2025-12-08 13:14:00', '登录', '1', '0', null, '常规业务', '127.0.0.1', '成功', '');
INSERT INTO `sys_log` VALUES ('58', '2025-12-08 13:17:35', '删除', '1', '0', '检测结果', '常规业务', '127.0.0.1', '成功', '');
INSERT INTO `sys_log` VALUES ('59', '2025-12-08 13:17:39', '删除', '1', '0', '检测结果', '常规业务', '127.0.0.1', '成功', '');
INSERT INTO `sys_log` VALUES ('60', '2025-12-08 13:17:42', '删除', '1', '0', '检测结果', '常规业务', '127.0.0.1', '成功', '');
INSERT INTO `sys_log` VALUES ('61', '2025-12-08 13:17:46', '删除', '1', '0', '检测结果', '常规业务', '127.0.0.1', '成功', '');
INSERT INTO `sys_log` VALUES ('62', '2025-12-08 13:17:51', '删除', '1', '0', '检测结果', '常规业务', '127.0.0.1', '成功', '');
INSERT INTO `sys_log` VALUES ('63', '2025-12-08 13:18:01', '删除', '1', '0', '检测结果', '常规业务', '127.0.0.1', '成功', '');
INSERT INTO `sys_log` VALUES ('64', '2025-12-08 13:25:32', '添加', '1', '0', 'api', '常规业务', '127.0.0.1', '成功', '');
INSERT INTO `sys_log` VALUES ('65', '2025-12-08 13:26:01', '添加', '1', '0', '操作员', '常规业务', '127.0.0.1', '成功', '');
INSERT INTO `sys_log` VALUES ('66', '2025-12-08 13:26:05', '删除', '1', '0', 'api', '常规业务', '127.0.0.1', '成功', '');
INSERT INTO `sys_log` VALUES ('67', '2025-12-08 13:26:08', '删除', '1', '0', 'api', '常规业务', '127.0.0.1', '成功', '');
INSERT INTO `sys_log` VALUES ('68', '2025-12-08 13:29:34', '登录', '1', '0', null, '常规业务', '127.0.0.1', '成功', '');
INSERT INTO `sys_log` VALUES ('69', '2025-12-08 14:52:20', '登录', '1', '0', null, '常规业务', '127.0.0.1', '成功', '');
INSERT INTO `sys_log` VALUES ('70', '2025-12-08 14:59:54', '登录', '1', '0', null, '常规业务', '127.0.0.1', '成功', '');
INSERT INTO `sys_log` VALUES ('71', '2025-12-09 09:59:43', '登录', '1', '0', null, '常规业务', '127.0.0.1', '成功', '');
INSERT INTO `sys_log` VALUES ('72', '2025-12-09 21:19:48', '登录', '1', '0', null, '常规业务', '127.0.0.1', '成功', '');
INSERT INTO `sys_log` VALUES ('73', '2025-12-10 11:19:26', '登录', '1', '0', null, '常规业务', '127.0.0.1', '成功', '');
INSERT INTO `sys_log` VALUES ('74', '2025-12-11 09:52:19', '登录', '1', '0', null, '常规业务', '127.0.0.1', '成功', '');
INSERT INTO `sys_log` VALUES ('75', '2025-12-11 15:22:42', '登录', '1', '0', null, '常规业务', '127.0.0.1', '成功', '');
INSERT INTO `sys_log` VALUES ('76', '2025-12-11 17:46:54', '登录', '1', '0', null, '常规业务', '127.0.0.1', '成功', '');
INSERT INTO `sys_log` VALUES ('77', '2025-12-11 20:02:05', '登录', '1', '0', null, '常规业务', '127.0.0.1', '成功', '');
INSERT INTO `sys_log` VALUES ('78', '2025-12-11 20:02:13', '登录', '1', '0', null, '常规业务', '127.0.0.1', '成功', '');
INSERT INTO `sys_log` VALUES ('79', '2025-12-11 20:02:26', '登录', '1', '0', null, '常规业务', '127.0.0.1', '成功', '');
INSERT INTO `sys_log` VALUES ('80', '2025-12-11 20:02:32', '登录', '1', '0', null, '常规业务', '127.0.0.1', '成功', '');
INSERT INTO `sys_log` VALUES ('81', '2025-12-11 20:03:31', '登录', '1', '0', null, '常规业务', '127.0.0.1', '成功', '');
INSERT INTO `sys_log` VALUES ('82', '2025-12-11 20:34:18', '添加', '1', '0', 'api', '常规业务', '127.0.0.1', '成功', '');
INSERT INTO `sys_log` VALUES ('83', '2025-12-12 11:24:34', '登录', '1', '0', null, '常规业务', '127.0.0.1', '成功', '');
INSERT INTO `sys_log` VALUES ('84', '2025-12-12 12:43:09', '登录', '1', '0', null, '常规业务', '127.0.0.1', '成功', '');
INSERT INTO `sys_log` VALUES ('85', '2025-12-12 13:27:40', '登录', '1', '0', null, '常规业务', '127.0.0.1', '成功', '');
INSERT INTO `sys_log` VALUES ('86', '2025-12-12 13:27:49', '登录', '1', '0', null, '常规业务', '127.0.0.1', '成功', '');
INSERT INTO `sys_log` VALUES ('87', '2025-12-12 14:41:10', '登录', '1', '0', null, '常规业务', '127.0.0.1', '成功', '');
INSERT INTO `sys_log` VALUES ('88', '2025-12-12 16:31:26', '登录', '1', '0', null, '常规业务', '127.0.0.1', '成功', '');
INSERT INTO `sys_log` VALUES ('89', '2025-12-12 16:31:54', '登录', '1', '0', null, '常规业务', '127.0.0.1', '成功', '');
INSERT INTO `sys_log` VALUES ('90', '2025-12-12 17:39:11', '登录', '1', '0', null, '常规业务', '127.0.0.1', '成功', '');
INSERT INTO `sys_log` VALUES ('91', '2025-12-12 17:59:49', '登录', '1', '0', null, '常规业务', '127.0.0.1', '成功', '');
INSERT INTO `sys_log` VALUES ('92', '2025-12-13 08:57:22', '登录', '1', '0', null, '常规业务', '127.0.0.1', '成功', '');
INSERT INTO `sys_log` VALUES ('93', '2025-12-13 11:20:44', '登录', '1', '0', null, '常规业务', '127.0.0.1', '成功', '');
INSERT INTO `sys_log` VALUES ('94', '2025-12-13 13:40:44', '登录', '1', '0', null, '常规业务', '127.0.0.1', '成功', '');
INSERT INTO `sys_log` VALUES ('95', '2025-12-13 14:33:40', '登录', '1', '0', null, '常规业务', '127.0.0.1', '成功', '');
INSERT INTO `sys_log` VALUES ('96', '2025-12-13 14:33:45', '登录', '1', '0', null, '常规业务', '127.0.0.1', '成功', '');
INSERT INTO `sys_log` VALUES ('97', '2025-12-13 14:33:57', '登录', '1', '0', null, '常规业务', '127.0.0.1', '成功', '');
INSERT INTO `sys_log` VALUES ('98', '2025-12-13 14:45:11', '登录', '1', '0', null, '常规业务', '127.0.0.1', '成功', '');
INSERT INTO `sys_log` VALUES ('99', '2025-12-13 15:14:06', '登录', '1', '0', null, '常规业务', '127.0.0.1', '成功', '');
INSERT INTO `sys_log` VALUES ('100', '2025-12-13 15:55:26', '登录', '1', '0', null, '常规业务', '127.0.0.1', '成功', '');
INSERT INTO `sys_log` VALUES ('101', '2025-12-13 16:23:41', '登录', '1', '0', null, '常规业务', '127.0.0.1', '成功', '');
INSERT INTO `sys_log` VALUES ('102', '2025-12-13 16:54:27', '删除', '1', '0', 'api', '常规业务', '127.0.0.1', '成功', '');
INSERT INTO `sys_log` VALUES ('103', '2025-12-13 18:45:23', '登录', '1', '0', null, '常规业务', '127.0.0.1', '成功', '');
INSERT INTO `sys_log` VALUES ('104', '2025-12-13 18:49:03', '登录', '1', '0', null, '常规业务', '127.0.0.1', '成功', '');
INSERT INTO `sys_log` VALUES ('105', '2025-12-14 10:03:32', '登录', '1', '0', null, '常规业务', '127.0.0.1', '成功', '');
INSERT INTO `sys_log` VALUES ('106', '2025-12-14 13:42:55', '登录', '1', '0', null, '常规业务', '127.0.0.1', '成功', '');
INSERT INTO `sys_log` VALUES ('107', '2025-12-14 14:04:12', '登录', '1', '0', null, '常规业务', '127.0.0.1', '成功', '');
INSERT INTO `sys_log` VALUES ('108', '2025-12-14 14:12:44', '登录', '1', '0', null, '常规业务', '127.0.0.1', '成功', '');
INSERT INTO `sys_log` VALUES ('109', '2025-12-14 18:40:17', '登录', '1', '0', null, '常规业务', '127.0.0.1', '成功', '');
INSERT INTO `sys_log` VALUES ('110', '2025-12-14 19:37:26', '登录', '1', '0', null, '常规业务', '127.0.0.1', '成功', '');
INSERT INTO `sys_log` VALUES ('111', '2025-12-15 09:16:42', '登录', '1', '0', null, '常规业务', '127.0.0.1', '成功', '');
INSERT INTO `sys_log` VALUES ('112', '2025-12-15 17:53:40', '登录', '1', '0', null, '常规业务', '127.0.0.1', '成功', '');
INSERT INTO `sys_log` VALUES ('113', '2025-12-16 10:46:07', '登录', '1', '0', null, '常规业务', '127.0.0.1', '成功', '');
INSERT INTO `sys_log` VALUES ('114', '2025-12-16 21:32:47', '登录', '1', '0', null, '常规业务', '127.0.0.1', '成功', '');
INSERT INTO `sys_log` VALUES ('115', '2025-12-17 10:46:36', '登录', '1', '0', null, '常规业务', '127.0.0.1', '成功', '');
INSERT INTO `sys_log` VALUES ('116', '2025-12-18 10:25:39', '登录', '1', '0', null, '常规业务', '127.0.0.1', '成功', '');
INSERT INTO `sys_log` VALUES ('117', '2025-12-18 16:16:53', '登录', '1', '0', null, '常规业务', '127.0.0.1', '成功', '');
INSERT INTO `sys_log` VALUES ('118', '2025-12-19 15:08:42', '登录', '1', '0', null, '常规业务', '127.0.0.1', '成功', '');
INSERT INTO `sys_log` VALUES ('119', '2025-12-20 10:20:06', '登录', '1', '0', null, '常规业务', '127.0.0.1', '成功', '');
INSERT INTO `sys_log` VALUES ('120', '2025-12-20 10:20:22', '登录', '1', '0', null, '常规业务', '127.0.0.1', '成功', '');
INSERT INTO `sys_log` VALUES ('121', '2025-12-20 10:21:50', '登录', '1', '0', null, '常规业务', '127.0.0.1', '成功', '');
INSERT INTO `sys_log` VALUES ('122', '2025-12-20 17:10:09', '登录', '1', '0', null, '常规业务', '127.0.0.1', '成功', '');
INSERT INTO `sys_log` VALUES ('123', '2025-12-21 10:29:43', '登录', '1', '0', null, '常规业务', '127.0.0.1', '成功', '');
INSERT INTO `sys_log` VALUES ('124', '2025-12-21 15:25:55', '登录', '1', '0', null, '常规业务', '127.0.0.1', '成功', '');
INSERT INTO `sys_log` VALUES ('125', '2025-12-21 16:37:42', '登录', '1', '0', null, '常规业务', '127.0.0.1', '成功', '');
INSERT INTO `sys_log` VALUES ('126', '2025-12-21 20:18:28', '登录', '1', '0', null, '常规业务', '127.0.0.1', '成功', '');
INSERT INTO `sys_log` VALUES ('127', '2025-12-22 13:09:44', '登录', '1', '0', null, '常规业务', '127.0.0.1', '成功', '');
INSERT INTO `sys_log` VALUES ('128', '2025-12-22 13:45:18', '登录', '1', '0', null, '常规业务', '127.0.0.1', '成功', '');
INSERT INTO `sys_log` VALUES ('129', '2025-12-22 14:03:30', '登录', '1', '0', null, '常规业务', '127.0.0.1', '成功', '');
INSERT INTO `sys_log` VALUES ('130', '2025-12-22 17:32:29', '登录', '1', '0', null, '常规业务', '127.0.0.1', '成功', '');
INSERT INTO `sys_log` VALUES ('131', '2025-12-22 21:27:17', '登录', '1', '0', null, '常规业务', '127.0.0.1', '成功', '');
INSERT INTO `sys_log` VALUES ('132', '2025-12-23 08:55:33', '登录', '1', '0', null, '常规业务', '127.0.0.1', '成功', '');
INSERT INTO `sys_log` VALUES ('133', '2025-12-23 10:07:59', '登录', '1', '0', null, '常规业务', '127.0.0.1', '成功', '');
INSERT INTO `sys_log` VALUES ('134', '2025-12-23 10:49:39', '登录', '1', '0', null, '常规业务', '127.0.0.1', '成功', '');
INSERT INTO `sys_log` VALUES ('135', '2025-12-23 10:52:16', '登录', '1', '0', null, '常规业务', '127.0.0.1', '成功', '');
INSERT INTO `sys_log` VALUES ('136', '2025-12-23 11:02:18', '登录', '1', '0', null, '常规业务', '127.0.0.1', '成功', '');
INSERT INTO `sys_log` VALUES ('137', '2025-12-23 11:06:44', '登录', '1', '0', null, '常规业务', '127.0.0.1', '成功', '');
INSERT INTO `sys_log` VALUES ('138', '2025-12-26 09:48:00', '登录', '1', '0', null, '常规业务', '127.0.0.1', '成功', '');
INSERT INTO `sys_log` VALUES ('139', '2025-12-30 19:25:47', '登录', '1', '0', null, '常规业务', '127.0.0.1', '成功', '');
INSERT INTO `sys_log` VALUES ('140', '2026-01-04 13:18:30', '登录', '1', '0', null, '常规业务', '127.0.0.1', '成功', '');
INSERT INTO `sys_log` VALUES ('141', '2026-01-04 17:06:53', '登录', '1', '0', null, '常规业务', '127.0.0.1', '成功', '');
INSERT INTO `sys_log` VALUES ('142', '2026-01-04 17:18:44', '登录', '1', '0', null, '常规业务', '127.0.0.1', '成功', '');
INSERT INTO `sys_log` VALUES ('143', '2026-01-04 20:39:53', '登录', '1', '0', null, '常规业务', '127.0.0.1', '成功', '');
INSERT INTO `sys_log` VALUES ('144', '2026-01-05 16:52:51', '登录', '1', '0', null, '常规业务', '127.0.0.1', '成功', '');
INSERT INTO `sys_log` VALUES ('145', '2026-01-05 18:20:09', '登录', '1', '0', null, '常规业务', '127.0.0.1', '成功', '');
INSERT INTO `sys_log` VALUES ('146', '2026-01-06 09:42:51', '登录', '1', '0', null, '常规业务', '127.0.0.1', '成功', '');
INSERT INTO `sys_log` VALUES ('147', '2026-01-06 10:23:34', '登录', '1', '0', null, '常规业务', '127.0.0.1', '成功', '');
INSERT INTO `sys_log` VALUES ('148', '2026-01-06 22:02:03', '登录', '1', '0', null, '常规业务', '127.0.0.1', '成功', '');
INSERT INTO `sys_log` VALUES ('149', '2026-08-26 20:29:35', '登录', '1', '0', null, '常规业务', '127.0.0.1', '成功', '');
INSERT INTO `sys_log` VALUES ('150', '2026-08-28 09:29:56', '登录', '1', '0', null, '常规业务', '127.0.0.1', '成功', '');
INSERT INTO `sys_log` VALUES ('151', '2026-08-28 11:47:08', '登录', '1', '0', null, '常规业务', '127.0.0.1', '成功', '');
INSERT INTO `sys_log` VALUES ('152', '2026-08-28 11:57:42', '登录', '1', '0', null, '常规业务', '127.0.0.1', '成功', '');
INSERT INTO `sys_log` VALUES ('153', '2026-08-28 13:22:15', '登录', '1', '0', null, '常规业务', '127.0.0.1', '成功', '');
INSERT INTO `sys_log` VALUES ('154', '2026-08-28 18:33:46', '登录', '1', '0', null, '常规业务', '127.0.0.1', '成功', '');
INSERT INTO `sys_log` VALUES ('155', '2026-08-28 19:01:47', '登录', '1', '0', null, '常规业务', '127.0.0.1', '成功', '');
INSERT INTO `sys_log` VALUES ('156', '2026-08-29 08:41:37', '登录', '1', '0', null, '常规业务', '127.0.0.1', '成功', '');
INSERT INTO `sys_log` VALUES ('157', '2026-08-29 09:18:33', '登录', '1', '0', null, '常规业务', '127.0.0.1', '成功', '');
INSERT INTO `sys_log` VALUES ('158', '2026-08-29 10:09:41', '登录', '1', '0', null, '常规业务', '127.0.0.1', '成功', '');
INSERT INTO `sys_log` VALUES ('159', '2026-08-29 10:51:08', '登录', '1', '0', null, '常规业务', '127.0.0.1', '成功', '');
INSERT INTO `sys_log` VALUES ('160', '2026-08-29 11:01:31', '登录', '1', '0', null, '常规业务', '127.0.0.1', '成功', '');
INSERT INTO `sys_log` VALUES ('161', '2026-08-29 11:25:21', '登录', '1', '0', null, '常规业务', '127.0.0.1', '成功', '');
INSERT INTO `sys_log` VALUES ('162', '2026-08-29 16:34:52', '登录', '1', '0', null, '常规业务', '127.0.0.1', '成功', '');
INSERT INTO `sys_log` VALUES ('163', '2026-08-29 19:26:12', '登录', '1', '0', null, '常规业务', '127.0.0.1', '成功', '');
INSERT INTO `sys_log` VALUES ('164', '2026-08-29 20:41:14', '登录', '1', '0', null, '常规业务', '127.0.0.1', '成功', '');
INSERT INTO `sys_log` VALUES ('165', '2026-08-30 08:34:15', '登录', '1', '0', null, '常规业务', '127.0.0.1', '成功', '');
INSERT INTO `sys_log` VALUES ('166', '2026-08-30 09:08:31', '登录', '1', '0', null, '常规业务', '127.0.0.1', '成功', '');
INSERT INTO `sys_log` VALUES ('167', '2026-08-30 09:12:43', '登录', '1', '0', null, '常规业务', '127.0.0.1', '成功', '');
INSERT INTO `sys_log` VALUES ('168', '2026-08-30 15:05:03', '添加', '1', '0', 'api', '常规业务', '127.0.0.1', '成功', '');
INSERT INTO `sys_log` VALUES ('169', '2026-08-30 15:05:20', '添加', '1', '0', 'api', '常规业务', '127.0.0.1', '成功', '');
INSERT INTO `sys_log` VALUES ('170', '2026-08-30 15:30:10', '登录', '1', '0', null, '常规业务', '127.0.0.1', '成功', '');
INSERT INTO `sys_log` VALUES ('171', '2026-08-30 15:30:15', '更新', '1', '0', 'api', '常规业务', '127.0.0.1', '成功', '');
INSERT INTO `sys_log` VALUES ('172', '2026-08-30 15:30:20', '更新', '1', '0', 'api', '常规业务', '127.0.0.1', '成功', '');
INSERT INTO `sys_log` VALUES ('173', '2026-08-30 15:30:25', '更新', '1', '0', 'api', '常规业务', '127.0.0.1', '成功', '');
INSERT INTO `sys_log` VALUES ('174', '2026-08-30 15:52:49', '登录', '1', '0', null, '常规业务', '127.0.0.1', '成功', '');
INSERT INTO `sys_log` VALUES ('175', '2026-08-30 15:54:47', '登录', '3', '0', null, '常规业务', '127.0.0.1', '成功', '');
INSERT INTO `sys_log` VALUES ('176', '2026-08-30 16:16:28', '删除', '3', '0', 'api', '常规业务', '127.0.0.1', '成功', '');
INSERT INTO `sys_log` VALUES ('177', '2026-08-30 16:18:50', '登录', '1', '0', null, '常规业务', '127.0.0.1', '成功', '');
INSERT INTO `sys_log` VALUES ('192', '2026-08-30 17:07:10', '上传图片', '1', '0', '智疗云_.png', '数据标注/图像管理', '127.0.0.1', '成功', '图片上传');
INSERT INTO `sys_log` VALUES ('193', '2026-08-30 17:45:12', '删除图片', '1', '0', '83', '数据标注/图像管理', '127.0.0.1', '成功', '删除图片');
INSERT INTO `sys_log` VALUES ('194', '2026-08-30 17:56:40', '删除图片', '1', '0', '17.jpg', '数据标注/图像管理', '127.0.0.1', '成功', '删除图片: 17.jpg');
INSERT INTO `sys_log` VALUES ('195', '2026-08-30 17:57:19', '删除', '1', '0', '质检检测', '质检检测', '127.0.0.1', '成功', '删除成功');
INSERT INTO `sys_log` VALUES ('196', '2026-08-30 17:59:23', '删除', '1', '0', 'API密钥', 'API密钥', '127.0.0.1', '成功', '批量删除API密钥: ID列表=[20]');
INSERT INTO `sys_log` VALUES ('197', '2026-08-30 17:59:35', '删除', '1', '0', 'API密钥', 'API密钥', '127.0.0.1', '成功', '批量删除API密钥: ID列表=[21]');
INSERT INTO `sys_log` VALUES ('198', '2026-08-30 17:59:37', '删除', '1', '0', 'API密钥', 'API密钥', '127.0.0.1', '成功', '批量删除API密钥: ID列表=[22]');
INSERT INTO `sys_log` VALUES ('199', '2026-08-30 17:59:38', '删除', '1', '0', 'API密钥', 'API密钥', '127.0.0.1', '成功', '批量删除API密钥: ID列表=[23]');
INSERT INTO `sys_log` VALUES ('200', '2026-08-30 17:59:40', '删除', '1', '0', 'API密钥', 'API密钥', '127.0.0.1', '成功', '批量删除API密钥: ID列表=[24]');
INSERT INTO `sys_log` VALUES ('201', '2026-08-30 19:53:20', '更新', '1', '0', '操作人员', '操作人员', '192.168.137.1', '成功', '更新操作员: 工号=1001, 姓名=工程师1');
INSERT INTO `sys_log` VALUES ('202', '2026-08-30 20:00:43', '登录', '1', '0', '系统用户', '系统用户', '192.168.179.237', '成功', '用户登录: 账号[admin1] 验证成功');
INSERT INTO `sys_log` VALUES ('203', '2026-08-30 20:01:59', '上传图片', '1', '0', '智疗云_.png', '数据标注/图像管理', '192.168.137.1', '成功', '上传图片: 智疗云_.png');
INSERT INTO `sys_log` VALUES ('204', '2026-08-30 20:02:02', '删除图片', '1', '0', '智疗云_.png', '数据标注/图像管理', '192.168.137.1', '成功', '删除图片: 智疗云_.png');
INSERT INTO `sys_log` VALUES ('205', '2026-08-30 20:02:12', '删除图片', '1', '0', '9.jpg', '数据标注/图像管理', '192.168.137.1', '成功', '删除图片: 9.jpg');
INSERT INTO `sys_log` VALUES ('206', '2026-08-31 11:25:11', '登录', '1', '0', '系统用户', '系统用户', '192.168.137.1', '成功', '用户登录: 账号[admin1] 验证成功');
INSERT INTO `sys_log` VALUES ('207', '2026-08-31 11:37:49', '登录', '1', '0', '系统用户', '系统用户', '192.168.137.1', '成功', '用户登录: 账号[admin1] 验证成功');
INSERT INTO `sys_log` VALUES ('208', '2026-08-31 11:43:26', '登录', '1', '0', '系统用户', '系统用户', '192.168.137.1', '成功', '用户登录: 账号[admin1] 验证成功');
INSERT INTO `sys_log` VALUES ('209', '2026-08-31 11:46:42', '登录', '1', '0', '系统用户', '系统用户', '192.168.137.1', '成功', '用户登录: 账号[admin1] 验证成功');
INSERT INTO `sys_log` VALUES ('210', '2026-08-31 11:48:23', '登录', '1', '0', '系统用户', '系统用户', '192.168.137.1', '成功', '用户登录: 账号[admin1] 验证成功');
INSERT INTO `sys_log` VALUES ('211', '2026-08-31 13:53:40', '更新', '1', '0', 'API密钥', 'API密钥', '192.168.137.1', '成功', '更新API密钥: ID=35, 状态=启用, 权限等级=2');
INSERT INTO `sys_log` VALUES ('212', '2026-08-31 13:53:46', '更新', '1', '0', 'API密钥', 'API密钥', '192.168.137.1', '成功', '更新API密钥: ID=26, 状态=启用, 权限等级=2');
INSERT INTO `sys_log` VALUES ('213', '2026-08-31 13:53:51', '更新', '1', '0', 'API密钥', 'API密钥', '192.168.137.1', '成功', '更新API密钥: ID=29, 状态=启用, 权限等级=3');
INSERT INTO `sys_log` VALUES ('214', '2026-08-31 13:53:56', '更新', '1', '0', 'API密钥', 'API密钥', '192.168.137.1', '成功', '更新API密钥: ID=30, 状态=启用, 权限等级=1');
INSERT INTO `sys_log` VALUES ('215', '2026-08-31 13:54:05', '更新', '1', '0', 'API密钥', 'API密钥', '192.168.137.1', '成功', '更新API密钥: ID=32, 状态=禁用, 权限等级=1');
INSERT INTO `sys_log` VALUES ('216', '2026-08-31 13:54:10', '更新', '1', '0', 'API密钥', 'API密钥', '192.168.137.1', '成功', '更新API密钥: ID=33, 状态=启用, 权限等级=1');
INSERT INTO `sys_log` VALUES ('217', '2026-08-31 14:40:31', '登录', '1', '0', '系统用户', '系统用户', '192.168.137.1', '成功', '用户登录: 账号[admin1] 验证成功');
INSERT INTO `sys_log` VALUES ('218', '2026-08-31 14:59:51', '添加', '-1', '0', '操作人员', '操作人员', '192.168.137.1', '失败', '添加操作员: 姓名=-, 工号=-');
INSERT INTO `sys_log` VALUES ('219', '2026-08-31 15:00:13', '登录', '1', '0', '系统用户', '系统用户', '192.168.137.1', '成功', '用户登录: 账号[admin1] 验证成功');
INSERT INTO `sys_log` VALUES ('220', '2026-08-31 15:00:55', '添加', '1', '0', 'API密钥', 'API密钥', '192.168.137.1', '失败', '批量生成密钥: 数量=null, 有效期=-1天, 权限等级=3');
INSERT INTO `sys_log` VALUES ('221', '2026-08-31 15:01:10', '添加', '1', '0', 'API密钥', 'API密钥', '192.168.137.1', '失败', '批量生成密钥: 数量=null, 有效期=-1天, 权限等级=3');
INSERT INTO `sys_log` VALUES ('222', '2026-08-31 15:04:03', '添加', '-1', '0', '操作人员', '操作人员', '192.168.137.1', '失败', '添加操作员: 姓名=test_op, 工号=9999');
INSERT INTO `sys_log` VALUES ('223', '2026-08-31 15:04:12', '添加', '-1', '0', '操作人员', '操作人员', '192.168.137.1', '失败', '添加操作员: 姓名=test_op, 工号=9999');
INSERT INTO `sys_log` VALUES ('224', '2026-08-31 15:04:21', '添加', '-1', '0', '操作人员', '操作人员', '192.168.137.1', '失败', '添加操作员: 姓名=test_op, 工号=9999');
INSERT INTO `sys_log` VALUES ('225', '2026-08-31 15:05:03', '添加', '-1', '0', '操作人员', '操作人员', '192.168.137.1', '失败', '添加操作员: 姓名=test_op, 工号=9999');
INSERT INTO `sys_log` VALUES ('226', '2026-08-31 15:05:03', '添加', '-1', '0', '操作人员', '操作人员', '192.168.137.1', '失败', '添加操作员: 姓名=test_op, 工号=9999');
INSERT INTO `sys_log` VALUES ('227', '2026-08-31 15:05:25', '添加', '-1', '0', '操作人员', '操作人员', '192.168.137.1', '失败', '添加操作员: 姓名=test_op, 工号=9999');
INSERT INTO `sys_log` VALUES ('228', '2026-08-31 15:05:35', '添加', '-1', '0', '操作人员', '操作人员', '192.168.137.1', '失败', '添加操作员: 姓名=test_op, 工号=9999');
INSERT INTO `sys_log` VALUES ('229', '2026-08-31 15:06:26', '添加', '-1', '0', '操作人员', '操作人员', '192.168.137.1', '失败', '添加操作员: 姓名=操作员测试, 工号=1099');
INSERT INTO `sys_log` VALUES ('230', '2026-08-31 15:06:46', '登录', '1', '0', '系统用户', '系统用户', '192.168.137.1', '成功', '用户登录: 账号[admin1] 验证成功');
INSERT INTO `sys_log` VALUES ('231', '2026-08-31 15:06:46', '添加', '1', '0', '操作人员', '操作人员', '192.168.137.1', '成功', '添加操作员: 姓名=操作员测试, 工号=1099');
INSERT INTO `sys_log` VALUES ('232', '2026-08-31 15:14:57', '登录', '1', '0', '系统用户', '系统用户', '192.168.137.1', '成功', '用户登录: 账号[admin1] 验证成功');
INSERT INTO `sys_log` VALUES ('233', '2026-08-31 15:14:57', '添加', '1', '0', '操作人员', '操作人员', '192.168.137.1', '成功', '添加操作员: 姓名=操作员1, 工号=1002');
INSERT INTO `sys_log` VALUES ('234', '2026-08-31 15:15:19', '登录', '1', '0', '系统用户', '系统用户', '192.168.137.1', '成功', '用户登录: 账号[admin1] 验证成功');
INSERT INTO `sys_log` VALUES ('235', '2026-08-31 15:18:21', '添加', '1', '0', '操作人员', '操作人员', '192.168.137.1', '成功', '添加操作员: 姓名=运维工程师, 工号=1003');
INSERT INTO `sys_log` VALUES ('236', '2026-08-31 15:21:56', '添加', '-1', '0', 'API密钥', 'API密钥', '192.168.137.1', '成功', '批量生成密钥: 数量=1, 有效期=-1天, 权限等级=3');
INSERT INTO `sys_log` VALUES ('237', '2026-08-31 15:28:52', '添加', '1', '0', 'API密钥', 'API密钥', '192.168.137.1', '成功', '批量生成密钥: 数量=1, 有效期=30天, 权限等级=2');
INSERT INTO `sys_log` VALUES ('238', '2026-08-31 15:29:01', '添加', '1', '0', 'API密钥', 'API密钥', '192.168.137.1', '成功', '批量生成密钥: 数量=1, 有效期=30天, 权限等级=1');
INSERT INTO `sys_log` VALUES ('239', '2026-08-31 15:34:42', '登录', '1', '0', '系统用户', '系统用户', '192.168.137.1', '成功', '用户登录: 账号[admin1] 验证成功');
INSERT INTO `sys_log` VALUES ('240', '2026-08-31 18:02:19', '登录', '1', '0', '系统用户', '系统用户', '192.168.137.1', '成功', '用户登录: 账号[admin1] 验证成功');
INSERT INTO `sys_log` VALUES ('241', '2026-09-02 19:24:17', '登录', '1', '0', null, '常规业务', '127.0.0.1', '成功', '');
INSERT INTO `sys_log` VALUES ('242', '2026-09-02 20:22:57', '登录', '1', '0', '系统用户', '系统用户', '192.168.137.1', '成功', '用户登录: 账号[admin1] 验证成功');
INSERT INTO `sys_log` VALUES ('243', '2026-09-03 08:22:19', '登录', '1', '0', null, '常规业务', '127.0.0.1', '成功', '');
INSERT INTO `sys_log` VALUES ('244', '2026-09-03 08:31:23', '登录', '1', '0', '系统用户', '系统用户', '192.168.137.1', '成功', '用户登录: 账号[admin1] 验证成功');
INSERT INTO `sys_log` VALUES ('245', '2026-09-03 11:16:01', '登录', '1', '0', '系统用户', '系统用户', '192.168.137.1', '成功', '用户登录: 账号[admin1] 验证成功');
INSERT INTO `sys_log` VALUES ('246', '2026-09-03 18:35:53', '登录', '1', '0', '系统用户', '系统用户', '192.168.137.1', '成功', '用户登录: 账号[admin1] 验证成功');

-- ----------------------------
-- Table structure for warnings
-- ----------------------------
DROP TABLE IF EXISTS `warnings`;
CREATE TABLE `warnings` (
  `id` int NOT NULL AUTO_INCREMENT,
  `axle_code` varchar(64) DEFAULT NULL COMMENT '半轴编号',
  `create_time` datetime DEFAULT NULL,
  `level` tinyint DEFAULT NULL,
  `type` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  `category` varchar(64) DEFAULT NULL COMMENT '质量类别',
  `content` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Records of warnings
-- ----------------------------
INSERT INTO `warnings` VALUES ('1', '2403511-P301', '2026-09-12 14:22:08', '1', '划痕', '外观表面缺陷', '');
INSERT INTO `warnings` VALUES ('2', 'CN744139', '2026-09-10 07:11:25', '3', '裂纹', '内部探伤缺陷', '');
INSERT INTO `warnings` VALUES ('3', '248-005-012', '2026-09-04 12:44:38', '2', '裂纹', '表面微裂纹', '');
INSERT INTO `warnings` VALUES ('4', 'BJ892301-B', '2026-08-29 18:20:15', '2', '划痕', '轴颈表面损伤', '');
INSERT INTO `warnings` VALUES ('5', '2401882-P108', '2026-08-23 23:55:40', '3', '裂纹', '结构完整性', '');
INSERT INTO `warnings` VALUES ('6', 'CN812044', '2026-08-20 10:30:15', '1', '划痕', '机械加工划伤', '');
INSERT INTO `warnings` VALUES ('7', '310-942-005', '2026-08-18 05:40:12', '3', '裂纹', '金相疲劳', '');
INSERT INTO `warnings` VALUES ('8', '2409733-P812', '2026-08-12 11:22:36', '2', '裂纹', '无损探伤', '');
INSERT INTO `warnings` VALUES ('9', 'TJ-449102', '2026-08-06 15:45:18', '1', '划痕', '装配划损', '');
INSERT INTO `warnings` VALUES ('10', '248-019-088', '2026-08-01 16:45:22', '3', '裂纹', '应力开裂', '');
INSERT INTO `warnings` VALUES ('11', 'CN650391', '2026-07-28 14:18:30', '2', '划痕', '配合面划痕', '');
INSERT INTO `warnings` VALUES ('12', '2404619-P504', '2026-07-20 09:30:15', '2', '裂纹', '内部探伤缺陷', '');
INSERT INTO `warnings` VALUES ('13', 'HB-772910', '2026-07-15 11:20:45', '1', '划痕', '搬运磕划', '');
INSERT INTO `warnings` VALUES ('14', '248-031-104', '2026-07-08 16:05:20', '3', '裂纹', '无损探伤', '');
INSERT INTO `warnings` VALUES ('15', 'CN903421', '2026-06-30 10:15:40', '2', '划痕', '机械加工划伤', '');

-- ----------------------------
-- Table structure for work_order
-- ----------------------------
DROP TABLE IF EXISTS `work_order`;
CREATE TABLE `work_order` (
  `id` smallint NOT NULL AUTO_INCREMENT,
  `is_deleted` decimal(1,0) DEFAULT '0',
  `create_time` datetime DEFAULT NULL,
  `create_id` int DEFAULT NULL,
  `current_num` int DEFAULT NULL,
  `detect_sum` int DEFAULT NULL,
  `is_over` decimal(1,0) DEFAULT '0',
  `finish_time` datetime DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  `update_id` int DEFAULT NULL,
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Records of work_order
-- ----------------------------
INSERT INTO `work_order` VALUES ('1', '0', '2025-11-22 19:12:50', '1', '10', '10', '1', '2025-11-26 17:09:02', '2025-11-22 19:12:50', '1', '??1');
INSERT INTO `work_order` VALUES ('2', '0', '2025-11-22 19:12:50', '2', '2', '20', '0', null, '2025-11-22 19:12:50', '2', '??2');
