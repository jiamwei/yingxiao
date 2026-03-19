package com.wjm.yingxiao.controller;

import com.wjm.yingxiao.entity.QueueDataVO;
import com.wjm.yingxiao.entity.QueueDataCompareVO;
import com.wjm.yingxiao.service.QueueDataService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import javax.annotation.Resource;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class QueueDataController {

    @Resource
    private QueueDataService queueDataService;

    @GetMapping("/api/filterOptions")
    public Map<String, List<String>> getFilterOptions() {
        return queueDataService.getFilterOptions();
    }

    @GetMapping("/api/queueData")
    public List<QueueDataVO> getQueueData(
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String restaurantName,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date statTime) {
        return queueDataService.getQueueDataWithDiff(location, category, restaurantName, statTime);
    }

    // 原有联动接口保持不变
    @GetMapping("/api/categoriesByLocation")
    public List<String> getCategoriesByLocation(@RequestParam String location) {
        return queueDataService.getCategoriesByLocation(location);
    }

    @GetMapping("/api/restaurantsByLocation")
    public List<String> getRestaurantsByLocation(@RequestParam String location) {
        return queueDataService.getRestaurantsByLocation(location);
    }

    @GetMapping("/api/locationsByCondition")
    public List<String> getLocationsByCondition(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String restaurantName) {
        return queueDataService.getLocationsByCondition(category, restaurantName);
    }

    @GetMapping("/api/restaurantsByCategory")
    public List<String> getRestaurantsByCategory(@RequestParam String category) {
        return queueDataService.getRestaurantsByCategory(category);
    }

    @GetMapping("/api/categoriesByRestaurant")
    public List<String> getCategoriesByRestaurant(@RequestParam String restaurantName) {
        return queueDataService.getCategoriesByRestaurant(restaurantName);
    }

    @GetMapping("/api/filterByCondition")
    public Map<String, List<String>> getFilterByCondition(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String restaurantName) {
        Map<String, List<String>> result = new HashMap<>();
        result.put("locations", queueDataService.getLocationsByCondition(category, restaurantName));
        result.put("restaurants", queueDataService.getRestaurantsByCategory(category));
        result.put("categories", queueDataService.getCategoriesByRestaurant(restaurantName));
        return result;
    }

    // ==================== 新增组合接口 ====================
    @GetMapping("/api/categoriesByCondition")
    public List<String> getCategoriesByCondition(
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String restaurantName) {
        return queueDataService.getCategoriesByCondition(location, restaurantName);
    }

    @GetMapping("/api/restaurantsByCondition")
    public List<String> getRestaurantsByCondition(
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String category) {
        return queueDataService.getRestaurantsByCondition(location, category);
    }

    @GetMapping("/api/queueDataCompare")
    public List<QueueDataCompareVO> compareQueueData(
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String restaurantName,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date statTime1,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date statTime2) {
        return queueDataService.compareQueueData(location, category, restaurantName, statTime1, statTime2);
    }
}