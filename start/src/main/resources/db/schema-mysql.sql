-- MySQL Workbench Forward Engineering

SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION';

-- -----------------------------------------------------
-- Schema dingtou
-- -----------------------------------------------------

-- -----------------------------------------------------
-- Schema dingtou
-- -----------------------------------------------------
CREATE SCHEMA IF NOT EXISTS `dingtou` DEFAULT CHARACTER SET utf8mb4 ;
USE `dingtou` ;

-- -----------------------------------------------------
-- Table `dingtou`.`stock`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `dingtou`.`stock` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `code` VARCHAR(45) NOT NULL COMMENT '股票基金编码',
  `type` VARCHAR(45) NOT NULL COMMENT '股票/基金',
  `market` VARCHAR(45) NOT NULL COMMENT '市场：沪、深、港、美、基',
  `owner` VARCHAR(45) NOT NULL DEFAULT 'sys' COMMENT '归属人',
  `trade_cfg` JSON NOT NULL COMMENT '交易配置：例如交易费用等',
  `total_fee` DECIMAL(10,4) NOT NULL COMMENT '总投入金额',
  `amount` DECIMAL(10,4) NOT NULL COMMENT '持有份额',
  `last_trade_time` DATETIME NULL,
  `trade_status` VARCHAR(45) NOT NULL COMMENT '当前状态：结算中，结算完毕',
  `status` INT NOT NULL DEFAULT 1 COMMENT '状态 0失效 1有效',
  PRIMARY KEY (`id`))
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `dingtou`.`stock_order`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `dingtou`.`stock_order` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `stock_id` BIGINT NOT NULL,
  `code` VARCHAR(45) NOT NULL,
  `create_time` DATETIME NOT NULL,
  `type` VARCHAR(45) NOT NULL COMMENT 'buy:买 sell:卖 bc:补偿',
  `out_id` VARCHAR(45) NOT NULL,
  `trade_time` DATE NOT NULL COMMENT '交易日期',
  `trade_fee` DECIMAL(10,4) NOT NULL COMMENT '交易金额',
  `trade_amount` DECIMAL(10,4) NOT NULL COMMENT '交易数量',
  `trade_service_fee` DECIMAL(10,4) NOT NULL COMMENT '交易服务费',
  `trade_status` VARCHAR(45) NOT NULL COMMENT '0:进行中 1:结算完成',
  PRIMARY KEY (`id`),
  CONSTRAINT `stock_id`
    FOREIGN KEY (`stock_id`)
    REFERENCES `dingtou`.`stock` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
