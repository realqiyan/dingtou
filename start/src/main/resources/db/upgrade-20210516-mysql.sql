ALTER TABLE `dingtou`.`stock`
ADD COLUMN `name` varchar(45) DEFAULT NULL COMMENT '显示名',
ADD COLUMN `category` varchar(45) DEFAULT NULL COMMENT '分类 例如：大盘、小盘、价值、行业、香港、债券、货币等',
ADD COLUMN `sub_category` varchar(45) DEFAULT NULL COMMENT '子分类 例如：300指数、500指数、养老、医药、传媒等';
