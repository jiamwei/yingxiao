package com.wjm.yingxiao.entity;

/**
 * @Description TODO
 * @Author wjmwx
 * @Date 14:39 2026/3/17
 **/

import lombok.Data;
import java.util.Date;

@Data
public class QueueData {
    /**
     * 主键ID
     */
    private Long id;
    /**
     * 地点
     */
    private String location;
    /**
     * 品类
     */
    private String category;
    /**
     * 餐馆名称
     */
    private String restaurantName;
    /**
     * 排队人数
     */
    private Integer queueNumber;
    /**
     * 统计时间点
     */
    private Date statTime;
    /**
     * 创建时间
     */
    private Date createTime;
    /**
     * 更新时间
     */
    private Date updateTime;
}
