package com.wjm.yingxiao.service;

import com.wjm.yingxiao.entity.QueueDataVO;
import com.wjm.yingxiao.entity.QueueDataCompareVO;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface QueueDataService {

    Map<String, List<String>> getFilterOptions();

    List<QueueDataVO> getQueueDataWithDiff(String location, String category, String restaurantName, Date statTime);

    List<String> getCategoriesByLocation(String location);
    List<String> getRestaurantsByLocation(String location);
    List<String> getLocationsByCondition(String category, String restaurantName);
    List<String> getRestaurantsByCategory(String category);
    List<String> getCategoriesByRestaurant(String restaurantName);

    // ==================== 新增组合查询方法 ====================
    List<String> getCategoriesByCondition(String location, String restaurantName);
    List<String> getRestaurantsByCondition(String location, String category);

    List<QueueDataCompareVO> compareQueueData(String location, String category, String restaurantName,
                                              Date statTime1, Date statTime2);
}