package com.wjm.yingxiao.mapper;

import com.wjm.yingxiao.entity.QueueData;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

public interface QueueDataMapper {

    List<String> selectAllLocations();
    List<String> selectAllCategories();
    List<String> selectAllRestaurantNames();
    Date selectLatestHourTime();

    List<QueueData> selectQueueDataByCondition(
            @Param("location") String location,
            @Param("category") String category,
            @Param("restaurantName") String restaurantName,
            @Param("statTime") Date statTime);

    Integer selectLastQueueNumber(@Param("restaurantName") String restaurantName, @Param("statTime") Date statTime);

    List<QueueData> selectRestaurantAllTimeData(
            @Param("restaurantName") String restaurantName,
            @Param("location") String location,
            @Param("category") String category);

    @Select("SELECT * FROM queue_data " +
            "WHERE restaurant_name = #{restaurantName} " +
            "AND (#{location} IS NULL OR location = #{location}) " +
            "AND (#{category} IS NULL OR category = #{category}) " +
            "AND stat_time >= DATE_SUB(NOW(), INTERVAL 30 DAY) " +
            "ORDER BY stat_time DESC")
    List<QueueData> selectRestaurantLastMonthData(
            @Param("restaurantName") String restaurantName,
            @Param("location") String location,
            @Param("category") String category);

    // 联动筛选接口
    List<String> selectCategoriesByLocation(@Param("location") String location);
    List<String> selectRestaurantsByLocation(@Param("location") String location);
    List<String> selectLocationsByCondition(@Param("category") String category, @Param("restaurantName") String restaurantName);
    List<String> selectRestaurantsByCategory(@Param("category") String category);
    List<String> selectCategoriesByRestaurant(@Param("restaurantName") String restaurantName);

    // 通用条件查询餐馆（用于组合）
    List<String> selectRestaurantsByCondition(
            @Param("location") String location,
            @Param("category") String category,
            @Param("restaurantName") String restaurantName);

    // 对比功能：时间范围查询

    List<QueueData> selectQueueDataByTimeRange(
            @Param("location") String location,
            @Param("category") String category,
            @Param("restaurantName") String restaurantName,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);
}