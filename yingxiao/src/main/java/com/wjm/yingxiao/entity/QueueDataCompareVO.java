package com.wjm.yingxiao.entity;

/**
 * @Description TODO
 * @Author wjmwx
 * @Date 10:43 2026/3/19
 **/

import lombok.Data;

@Data
public class QueueDataCompareVO {
    private String location;
    private String category;
    private String restaurantName;
    private Integer number1; // 时间点1人数，若无数据则为0
    private Integer number2; // 时间点2人数，若无数据则为0
}
