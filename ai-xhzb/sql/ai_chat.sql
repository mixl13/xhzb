-- ----------------------------
-- MySQL 版 Spring AI 会话记忆 / 会话隔离 建表脚本
-- 说明：原有 Redis 实现（chat:history:{userId} / chat:memory:{conversationId}）保持不变，
--       本脚本仅为新增的 MySQL 实现方式提供存储结构，不改动既有表
-- ----------------------------

-- ----------------------------
-- 1、AI会话表 ai_chat_conversation（对应会话隔离：用户 -> 会话ID 集合）
-- ----------------------------
CREATE TABLE IF NOT EXISTS `ai_chat_conversation` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `conversation_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '会话ID（业务唯一标识，对应原Redis的chatId）',
  `user_id` bigint NOT NULL COMMENT '所属用户ID（会话隔离维度）',
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '会话标题（默认取首条用户提问）',
  `message_count` int DEFAULT '0' COMMENT '会话消息条数',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '0' COMMENT '状态（0正常 1已删除）',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '备注',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_conversation_id` (`conversation_id`) USING BTREE,
  KEY `idx_user_id` (`user_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='AI会话表';

-- ----------------------------
-- 2、AI会话消息表 ai_chat_message（对应会话记忆：会话ID -> 消息列表）
-- ----------------------------
CREATE TABLE IF NOT EXISTS `ai_chat_message` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `conversation_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '所属会话ID',
  `seq` int NOT NULL DEFAULT '0' COMMENT '消息序号（同一会话内递增，保证上下文按时间正序）',
  `message_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '消息类型（user/assistant/system/tool）',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT '消息内容',
  `metadata` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT '消息元数据（JSON字符串）',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '备注',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_conversation_seq` (`conversation_id`,`seq`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='AI会话消息表';
