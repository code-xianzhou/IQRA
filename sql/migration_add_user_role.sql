-- Migration: Add role and first_login columns to user table
-- Run this if you already have the iqra_db database

USE iqra_db;

-- Add role column if not exists
ALTER TABLE `user` ADD COLUMN `role` VARCHAR(20) DEFAULT 'USER' COMMENT '角色: ADMIN-管理员, USER-普通用户' AFTER `phone`;

-- Add first_login column if not exists
ALTER TABLE `user` ADD COLUMN `first_login` TINYINT DEFAULT 1 COMMENT '是否首次登录: 0-否, 1-是' AFTER `role`;

-- Update existing admin user
UPDATE `user` SET `role` = 'ADMIN', `first_login` = 0 WHERE `username` = 'admin';

-- Update existing regular users
UPDATE `user` SET `role` = 'USER', `first_login` = 0 WHERE `username` != 'admin' AND (`role` IS NULL OR `role` = '');
