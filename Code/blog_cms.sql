/*
SQLyog Ultimate v12.09 (64 bit)
MySQL - 5.7.10-log : Database - blog_cms
*********************************************************************
*/

/*!40101 SET NAMES utf8 */;

/*!40101 SET SQL_MODE=''*/;

/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
CREATE DATABASE /*!32312 IF NOT EXISTS*/`blog_cms` /*!40100 DEFAULT CHARACTER SET utf8 */;

USE `blog_cms`;

/*Table structure for table `bl_blog` */

DROP TABLE IF EXISTS `bl_blog`;

CREATE TABLE `bl_blog` (
  `blog_id` varchar(32) NOT NULL COMMENT '帖子id',
  `blog_title` varchar(63) DEFAULT NULL COMMENT '标题',
  `blog_image` varchar(255) DEFAULT NULL COMMENT '封面',
  `blog_content` mediumtext COMMENT '帖子内容',
  `blog_content_img` varchar(60) DEFAULT NULL COMMENT '文章内容图片仓库',
  `blog_type` int(11) DEFAULT NULL COMMENT '博客分类',
  `blog_remark` varchar(255) DEFAULT NULL COMMENT '简介',
  `gmt_create` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `gmt_modified` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`blog_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC COMMENT='博客表';

/*Table structure for table `bl_link` */

DROP TABLE IF EXISTS `bl_link`;

CREATE TABLE `bl_link` (
  `id` int(255) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `nickname` varchar(50) DEFAULT NULL COMMENT '昵称',
  `desc_info` varchar(50) DEFAULT NULL COMMENT '简介',
  `img` text COMMENT '头像地址',
  `url` text COMMENT '主页地址',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8;

/*Table structure for table `bl_type` */

DROP TABLE IF EXISTS `bl_type`;

CREATE TABLE `bl_type` (
  `type_id` int(11) NOT NULL AUTO_INCREMENT COMMENT '分类id',
  `type_name` varchar(31) DEFAULT NULL COMMENT '分类名称',
  `type_blog_count` int(11) DEFAULT '0' COMMENT '帖子数',
  `type_img` varchar(255) DEFAULT NULL COMMENT '该分类的图片',
  PRIMARY KEY (`type_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=21 DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC COMMENT='帖子类型表';

/*Table structure for table `con_role_user` */

DROP TABLE IF EXISTS `con_role_user`;

CREATE TABLE `con_role_user` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '关联表的主键id',
  `role_id` int(11) DEFAULT NULL COMMENT '角色id',
  `user_id` int(11) DEFAULT NULL COMMENT '用户id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8;

/*Table structure for table `jh_role` */

DROP TABLE IF EXISTS `jh_role`;

CREATE TABLE `jh_role` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '角色主键',
  `role_name` varchar(50) NOT NULL COMMENT '角色名',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8;

/*Table structure for table `jh_user` */

DROP TABLE IF EXISTS `jh_user`;

CREATE TABLE `jh_user` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `username` varchar(50) DEFAULT NULL COMMENT '用户名',
  `password` varchar(50) DEFAULT NULL COMMENT '密码',
  `nickname` varchar(255) DEFAULT NULL COMMENT '昵称',
  `avatar` text NOT NULL COMMENT '头像（地址）',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `intro` text COMMENT '个人简介',
  PRIMARY KEY (`id`),
  UNIQUE KEY `UNIQUE_NAME` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8;

/*Table structure for table `message` */

DROP TABLE IF EXISTS `message`;

CREATE TABLE `message` (
  `id` int(255) NOT NULL AUTO_INCREMENT COMMENT '留言-主键',
  `nickname` varchar(50) DEFAULT NULL COMMENT '昵称',
  `url` text COMMENT '邮箱/网址',
  `content` text COMMENT '内容',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;
