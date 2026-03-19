package com.wjm.yingxiao.entity;

/**
 * @Description TODO
 * @Author wjmwx
 * @Date 14:39 2026/3/17
 **/

import lombok.Data;
import java.util.Date;

@Data
public class QueueDataVO extends QueueData {
    /**
     * 与上一时间点的人数差
     */
    private Integer numberDiff;
}
