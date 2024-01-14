ALTER TABLE `dingtou`.`stock_order`
ADD COLUMN `snapshot` JSON DEFAULT NULL COMMENT '交易快照';
