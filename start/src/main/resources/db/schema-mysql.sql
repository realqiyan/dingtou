CREATE DATABASE  IF NOT EXISTS `dingtou` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;
USE `dingtou`;
-- MySQL dump 10.13  Distrib 8.0.20, for macos10.15 (x86_64)
--
-- Host: 127.0.0.1    Database: dingtou
-- ------------------------------------------------------
-- Server version	8.0.20

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `stock`
--

DROP TABLE IF EXISTS `stock`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `stock` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `code` varchar(45) NOT NULL COMMENT '股票基金编码',
  `type` varchar(45) NOT NULL COMMENT '股票/基金',
  `market` varchar(45) NOT NULL COMMENT '市场：沪、深、港、美、基',
  `owner` varchar(45) NOT NULL DEFAULT 'sys' COMMENT '归属人',
  `trade_cfg` json NOT NULL COMMENT '交易配置：例如交易费用等',
  `total_fee` decimal(10,4) NOT NULL COMMENT '总投入金额',
  `amount` decimal(10,4) NOT NULL COMMENT '持有份额',
  `last_trade_time` datetime DEFAULT NULL,
  `trade_status` varchar(45) NOT NULL COMMENT '当前状态：结算中，结算完毕',
  `status` int NOT NULL DEFAULT '1' COMMENT '状态 0失效 1有效',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1362067770606362626 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `stock_order`
--

DROP TABLE IF EXISTS `stock_order`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `stock_order` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `stock_id` bigint NOT NULL,
  `code` varchar(45) NOT NULL,
  `create_time` datetime NOT NULL,
  `type` varchar(45) NOT NULL COMMENT 'buy:买 sell:卖 bc:补偿',
  `out_id` varchar(45) NOT NULL,
  `trade_time` date DEFAULT NULL COMMENT '交易日期',
  `trade_fee` decimal(10,4) NOT NULL COMMENT '交易金额',
  `trade_amount` decimal(10,4) NOT NULL COMMENT '交易数量',
  `trade_service_fee` decimal(10,4) NOT NULL COMMENT '交易服务费',
  `trade_status` varchar(45) NOT NULL COMMENT '0:进行中 1:结算完成',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_stock_order_out_id` (`out_id`),
  KEY `stock_id` (`stock_id`),
  CONSTRAINT `stock_id` FOREIGN KEY (`stock_id`) REFERENCES `stock` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1362104835805044738 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed