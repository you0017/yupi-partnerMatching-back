/*
 Navicat Premium Data Transfer

 Source Server         : root登录本机的mysql
 Source Server Type    : MySQL
 Source Server Version : 80033 (8.0.33)
 Source Host           : localhost:3306
 Source Schema         : user_center

 Target Server Type    : MySQL
 Target Server Version : 80033 (8.0.33)
 File Encoding         : 65001

 Date: 10/02/2025 11:42:54
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'id',
  `userName` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '用户昵称',
  `userAccount` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '账号',
  `avatarUrl` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '用户头像',
  `gender` tinyint NULL DEFAULT NULL COMMENT '性别',
  `userPassword` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '密码',
  `phone` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '电话',
  `email` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '邮箱',
  `userStatus` int NOT NULL DEFAULT 0 COMMENT '状态 0 - 正常',
  `createTime` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updateTime` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `isDelete` tinyint NOT NULL DEFAULT 0 COMMENT '是否删除',
  `userRole` int NOT NULL DEFAULT 0 COMMENT '用户角色 0 - 普通用户 1 - 管理员',
  `planetCode` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '星球编号',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 46 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user
-- ----------------------------
INSERT INTO `user` VALUES (38, '1611543951', '1611543951', 'https://sh-hengyang.oss-cn-wuhan-lr.aliyuncs.com/user-center/1.GIF', 1, 'ba879cb43df1f324cd49091767bb2fe9', NULL, NULL, 0, '2024-12-09 20:46:52', '2024-12-12 22:30:19', 0, 0, NULL);
INSERT INTO `user` VALUES (39, 'shihao', 'shihao', 'https://sh-hengyang.oss-cn-wuhan-lr.aliyuncs.com/user-center/1.GIF', 0, '58047e4b53cbd7a36174a5d93fb735be', '15673893813', '203858723363@qq.com', 0, '2024-12-11 20:24:28', '2024-12-15 15:09:35', 0, 1, '001');
INSERT INTO `user` VALUES (40, 'shihaoshihao', 'shihaoshihao', 'https://sh-hengyang.oss-cn-wuhan-lr.aliyuncs.com/user-center/1.GIF', 1, '58047e4b53cbd7a36174a5d93fb735be', NULL, NULL, 0, '2024-12-11 20:25:07', '2024-12-12 23:09:10', 0, 0, NULL);
INSERT INTO `user` VALUES (41, NULL, '2014534514', NULL, NULL, 'afc3ddfe9a58573b03af95c30c23d082', NULL, NULL, 0, '2024-12-13 22:51:10', '2024-12-13 22:51:10', 0, 0, NULL);
INSERT INTO `user` VALUES (42, NULL, '1234242', NULL, NULL, 'e4862be1211f0782ca0bcb16c32c4d57', NULL, NULL, 0, '2024-12-13 22:51:37', '2024-12-13 22:51:37', 0, 0, NULL);
INSERT INTO `user` VALUES (43, NULL, '34243214', NULL, NULL, '9c7ce6784735ce91c379abf4f6af52d5', NULL, NULL, 0, '2024-12-13 22:52:41', '2024-12-13 22:52:41', 0, 0, '2014534514');
INSERT INTO `user` VALUES (44, NULL, 'ceshi1', NULL, NULL, '75afa329cfb327af23252d410494714b', NULL, NULL, 0, '2024-12-13 22:57:06', '2024-12-13 22:57:06', 0, 0, 'shihao');
INSERT INTO `user` VALUES (45, NULL, NULL, NULL, NULL, 'sdf', NULL, NULL, 0, '2024-12-14 20:20:56', '2024-12-14 20:20:56', 0, 0, NULL);

SET FOREIGN_KEY_CHECKS = 1;
