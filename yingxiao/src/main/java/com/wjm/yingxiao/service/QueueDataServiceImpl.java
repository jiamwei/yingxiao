package com.wjm.yingxiao.service;

import com.wjm.yingxiao.entity.QueueData;
import com.wjm.yingxiao.entity.QueueDataVO;
import com.wjm.yingxiao.entity.QueueDataCompareVO;
import com.wjm.yingxiao.mapper.QueueDataMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@Service
public class QueueDataServiceImpl implements QueueDataService {

    @Resource
    private QueueDataMapper queueDataMapper;

    @Override
    public Map<String, List<String>> getFilterOptions() {
        Map<String, List<String>> options = new HashMap<>();
        options.put("locations", queueDataMapper.selectAllLocations());
        options.put("categories", queueDataMapper.selectAllCategories());
        options.put("restaurantNames", queueDataMapper.selectAllRestaurantNames());
        return options;
    }

    @Override
    public List<QueueDataVO> getQueueDataWithDiff(String location, String category,
                                                  String restaurantName, Date statTime) {
        // 如果选择了具体时间点，使用新逻辑（前后30分钟 + 上一小时对比）
        if (statTime != null) {
            return getQueueDataWithDiffBySelectedTime(location, category, restaurantName, statTime);
        }

        // 原有逻辑：未选择时间点，查询最新整点数据
        List<QueueData> queueDataList;
        if (restaurantName != null && !restaurantName.isEmpty()) {
            queueDataList = queueDataMapper.selectRestaurantLastMonthData(restaurantName, location, category);
        } else {
            if (statTime == null) {
                statTime = queueDataMapper.selectLatestHourTime();
            }
            queueDataList = queueDataMapper.selectQueueDataByCondition(location, category, null, statTime);
        }

        List<QueueDataVO> voList = new ArrayList<>();
        for (QueueData data : queueDataList) {
            QueueDataVO vo = new QueueDataVO();
            BeanUtils.copyProperties(data, vo);
            Integer lastNumber = queueDataMapper.selectLastQueueNumber(data.getRestaurantName(), data.getStatTime());
            vo.setNumberDiff(lastNumber != null ? data.getQueueNumber() - lastNumber : 0);
            voList.add(vo);
        }

        if (restaurantName == null || restaurantName.isEmpty()) {
            voList.sort(Comparator.comparing(QueueDataVO::getLocation));
        }
        return voList;
    }

    /**
     * 新逻辑：根据选中的整点时间，查询前后30分钟数据，并与上一整点前后30分钟数据对比
     */
    private List<QueueDataVO> getQueueDataWithDiffBySelectedTime(String location, String category,
                                                                 String restaurantName, Date statTime) {
        ZoneId zone = ZoneId.systemDefault();
        LocalDateTime targetTime = statTime.toInstant().atZone(zone).toLocalDateTime();

        // 当前整点前后30分钟范围
        LocalDateTime currentStart = targetTime.minusMinutes(30);
        LocalDateTime currentEnd   = targetTime.plusMinutes(30);

        // 上一整点时间（减1小时）
        LocalDateTime prevTargetTime = targetTime.minusHours(1);
        LocalDateTime prevStart = prevTargetTime.minusMinutes(30);
        LocalDateTime prevEnd   = prevTargetTime.plusMinutes(30);

        // 查询两个时间范围内的所有数据
        List<QueueData> currentList = queueDataMapper.selectQueueDataByTimeRange(
                location, category, restaurantName, currentStart, currentEnd);
        List<QueueData> prevList = queueDataMapper.selectQueueDataByTimeRange(
                location, category, restaurantName, prevStart, prevEnd);

        // 分别取每组最接近目标时间的数据
        Map<String, QueueData> currentMap = getClosestToTarget(currentList, statTime);
        Map<String, QueueData> prevMap = getClosestToTarget(prevList,
                Date.from(prevTargetTime.atZone(zone).toInstant()));

        List<QueueDataVO> result = new ArrayList<>();
        for (Map.Entry<String, QueueData> entry : currentMap.entrySet()) {
            QueueData current = entry.getValue();
            QueueDataVO vo = new QueueDataVO();
            BeanUtils.copyProperties(current, vo);

            QueueData prev = prevMap.get(entry.getKey());
            vo.setNumberDiff(prev != null ? current.getQueueNumber() - prev.getQueueNumber() : 0);
            result.add(vo);
        }

        // 按地点、品类、餐馆名称排序，保持与原逻辑一致
        result.sort(Comparator.comparing(QueueDataVO::getLocation)
                .thenComparing(QueueDataVO::getCategory)
                .thenComparing(QueueDataVO::getRestaurantName));
        return result;
    }

    @Override
    public List<String> getCategoriesByLocation(String location) {
        return queueDataMapper.selectCategoriesByLocation(location);
    }

    @Override
    public List<String> getRestaurantsByLocation(String location) {
        return queueDataMapper.selectRestaurantsByLocation(location);
    }

    @Override
    public List<String> getLocationsByCondition(String category, String restaurantName) {
        return queueDataMapper.selectLocationsByCondition(category, restaurantName);
    }

    @Override
    public List<String> getRestaurantsByCategory(String category) {
        return queueDataMapper.selectRestaurantsByCategory(category);
    }

    @Override
    public List<String> getCategoriesByRestaurant(String restaurantName) {
        return queueDataMapper.selectCategoriesByRestaurant(restaurantName);
    }

    // ==================== 新增组合查询实现 ====================
    @Override
    public List<String> getCategoriesByCondition(String location, String restaurantName) {
        if ((location == null || location.isEmpty()) && (restaurantName == null || restaurantName.isEmpty())) {
            return queueDataMapper.selectAllCategories();
        }
        if (location != null && !location.isEmpty() && (restaurantName == null || restaurantName.isEmpty())) {
            return queueDataMapper.selectCategoriesByLocation(location);
        }
        if (restaurantName != null && !restaurantName.isEmpty() && (location == null || location.isEmpty())) {
            return queueDataMapper.selectCategoriesByRestaurant(restaurantName);
        }
        List<String> byLocation = queueDataMapper.selectCategoriesByLocation(location);
        List<String> byRestaurant = queueDataMapper.selectCategoriesByRestaurant(restaurantName);
        byLocation.retainAll(byRestaurant);
        return byLocation;
    }

    @Override
    public List<String> getRestaurantsByCondition(String location, String category) {
        if ((location == null || location.isEmpty()) && (category == null || category.isEmpty())) {
            return queueDataMapper.selectAllRestaurantNames();
        }
        if (location != null && !location.isEmpty() && (category == null || category.isEmpty())) {
            return queueDataMapper.selectRestaurantsByLocation(location);
        }
        if (category != null && !category.isEmpty() && (location == null || location.isEmpty())) {
            return queueDataMapper.selectRestaurantsByCategory(category);
        }
        return queueDataMapper.selectRestaurantsByCondition(location, category, null);
    }

    @Override
    public List<QueueDataCompareVO> compareQueueData(String location, String category,
                                                     String restaurantName, Date statTime1, Date statTime2) {
        // 使用系统默认时区进行时间转换
        ZoneId zone = ZoneId.systemDefault();

        LocalDateTime time1 = statTime1.toInstant().atZone(zone).toLocalDateTime();
        LocalDateTime time2 = statTime2.toInstant().atZone(zone).toLocalDateTime();

        // 计算半小时范围（包含边界）
        LocalDateTime start1 = time1.minusMinutes(30);
        LocalDateTime end1 = time1.plusMinutes(30);
        LocalDateTime start2 = time2.minusMinutes(30);
        LocalDateTime end2 = time2.plusMinutes(30);


        // 查询两个时间范围内的所有数据
        List<QueueData> list1 = queueDataMapper.selectQueueDataByTimeRange(location, category, restaurantName, start1, end1);
        List<QueueData> list2 = queueDataMapper.selectQueueDataByTimeRange(location, category, restaurantName, start2, end2);

        // 按餐馆分组，每个时间点只取最接近目标整点的一条数据
        Map<String, QueueData> map1 = getClosestToTarget(list1, statTime1);
        Map<String, QueueData> map2 = getClosestToTarget(list2, statTime2);

        // 合并结果
        Map<String, QueueDataCompareVO> resultMap = new HashMap<>();

        for (Map.Entry<String, QueueData> entry : map1.entrySet()) {
            QueueData data = entry.getValue();
            QueueDataCompareVO vo = new QueueDataCompareVO();
            vo.setLocation(data.getLocation());
            vo.setCategory(data.getCategory());
            vo.setRestaurantName(data.getRestaurantName());
            vo.setNumber1(data.getQueueNumber());
            vo.setNumber2(0);
            resultMap.put(entry.getKey(), vo);
        }

        for (Map.Entry<String, QueueData> entry : map2.entrySet()) {
            QueueData data = entry.getValue();
            String key = entry.getKey();
            if (resultMap.containsKey(key)) {
                resultMap.get(key).setNumber2(data.getQueueNumber());
            } else {
                QueueDataCompareVO vo = new QueueDataCompareVO();
                vo.setLocation(data.getLocation());
                vo.setCategory(data.getCategory());
                vo.setRestaurantName(data.getRestaurantName());
                vo.setNumber1(0);
                vo.setNumber2(data.getQueueNumber());
                resultMap.put(key, vo);
            }
        }

        return new ArrayList<>(resultMap.values());
    }

    private Map<String, QueueData> getClosestToTarget(List<QueueData> list, Date target) {
        Map<String, QueueData> map = new HashMap<>();
        if (list == null || list.isEmpty()) {
            return map;
        }
        for (QueueData data : list) {
            String key = data.getRestaurantName() + "|" + data.getLocation() + "|" + data.getCategory();
            if (!map.containsKey(key)) {
                map.put(key, data);
            } else {
                QueueData existing = map.get(key);
                long existingDiff = Math.abs(existing.getStatTime().getTime() - target.getTime());
                long currentDiff = Math.abs(data.getStatTime().getTime() - target.getTime());
                if (currentDiff < existingDiff) {
                    map.put(key, data);
                }
            }
        }
        return map;
    }
}