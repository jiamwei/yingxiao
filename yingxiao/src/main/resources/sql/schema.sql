-- 创建数据库
CREATE DATABASE IF NOT EXISTS restaurant_db DEFAULT CHARACTER SET utf8mb4;
USE restaurant_db;

-- 排队数据表
CREATE TABLE IF NOT EXISTS queue_data (
                                          id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
                                          location VARCHAR(50) NOT NULL COMMENT '地点',
    category VARCHAR(50) NOT NULL COMMENT '品类',
    restaurant_name VARCHAR(100) NOT NULL COMMENT '餐馆名称',
    queue_number INT NOT NULL COMMENT '排队人数',
    stat_time DATETIME NOT NULL COMMENT '统计时间点',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='餐馆排队人数统计表';

-- 插入测试数据
INSERT INTO queue_data (location, category, restaurant_name, queue_number, stat_time) VALUES
                                                                                          ('北京朝阳区', '火锅', '海底捞', 86, '2026-03-17 12:00:00'),
                                                                                          ('北京朝阳区', '火锅', '海底捞', 78, '2026-03-17 11:00:00'),
                                                                                          ('上海浦东新区', '烧烤', '木屋烧烤', 45, '2026-03-17 12:00:00'),
                                                                                          ('上海浦东新区', '烧烤', '木屋烧烤', 38, '2026-03-17 11:00:00'),
                                                                                          ('广州天河区', '粤菜', '点都德', 62, '2026-03-17 12:00:00');