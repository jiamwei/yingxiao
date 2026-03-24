// 页面加载完成后执行
document.addEventListener('DOMContentLoaded', function() {
    console.log('页面加载完成，开始初始化...');

    // 全局变量，存储全量选项用于恢复
    window.allFilterOptions = null;

    // 主页面和对比面板的防循环标志
    window.isUpdating = false;
    window.isCompareUpdating = false;

    // 加载下拉框选项
    loadFilterOptions();

    // 绑定查询按钮事件
    document.getElementById('searchBtn').addEventListener('click', queryData);
    document.getElementById('resetBtn').addEventListener('click', resetForm);

    // 为主页面下拉框绑定 change 事件
    document.getElementById('location').addEventListener('change', onMainFilterChange);
    document.getElementById('category').addEventListener('change', onMainFilterChange);
    document.getElementById('restaurantName').addEventListener('change', onMainFilterChange);

    // 初始加载所有数据
    queryData();
});

/**
 * 加载筛选下拉框选项
 */
function loadFilterOptions() {
    fetch('/api/filterOptions')
        .then(response => {
            if (!response.ok) throw new Error('获取选项失败');
            return response.json();
        })
        .then(data => {
            window.allFilterOptions = data;
            // 填充主页面下拉框
            fillSelect('location', data.locations);
            fillSelect('category', data.categories);
            fillSelect('restaurantName', data.restaurantNames);
        })
        .catch(error => {
            console.error('加载选项失败:', error);
            showError('加载筛选选项失败，请刷新页面重试！');
        });
}

/**
 * 填充下拉框（保留第一个“全部”选项）
 */
function fillSelect(selectId, options) {
    const select = document.getElementById(selectId);
    if (!select) return;
    while (select.options.length > 1) {
        select.remove(1);
    }
    (options || []).forEach(opt => {
        if (opt) {
            const option = document.createElement('option');
            option.value = opt;
            option.textContent = opt;
            select.appendChild(option);
        }
    });
}

/**
 * 更新下拉框选项，同时检查当前选中值是否在新列表中，若不在则置空
 */
function updateSelectOptions(selectId, newOptions, keepValue = true) {
    const select = document.getElementById(selectId);
    if (!select) return;
    const currentVal = select.value;

    while (select.options.length > 1) {
        select.remove(1);
    }
    (newOptions || []).forEach(opt => {
        if (opt) {
            const option = document.createElement('option');
            option.value = opt;
            option.textContent = opt;
            select.appendChild(option);
        }
    });

    if (keepValue && currentVal && newOptions.includes(currentVal)) {
        select.value = currentVal;
    } else {
        select.value = '';
    }
}

// ==================== 主页面联动逻辑 ====================
async function onMainFilterChange() {
    if (window.isUpdating) return;
    window.isUpdating = true;
    try {
        await updateMainDropdowns();
    } finally {
        window.isUpdating = false;
    }
}

async function updateMainDropdowns() {
    const location = document.getElementById('location').value;
    const category = document.getElementById('category').value;
    const restaurant = document.getElementById('restaurantName').value;

    const [locations, categories, restaurants] = await Promise.all([
        fetchLocations(location, category, restaurant),
        fetchCategories(location, category, restaurant),
        fetchRestaurants(location, category, restaurant)
    ]);

    updateSelectOptions('location', locations, true);
    updateSelectOptions('category', categories, true);
    updateSelectOptions('restaurantName', restaurants, true);
}

// 获取地点选项
async function fetchLocations(location, category, restaurant) {
    if (!category && !restaurant) {
        return window.allFilterOptions?.locations || [];
    }
    const params = new URLSearchParams();
    if (category) params.append('category', category);
    if (restaurant) params.append('restaurantName', restaurant);
    const res = await fetch(`/api/locationsByCondition?${params}`);
    if (!res.ok) return [];
    return res.json();
}

// 获取品类选项
async function fetchCategories(location, category, restaurant) {
    if (!location && !restaurant) {
        return window.allFilterOptions?.categories || [];
    }
    const params = new URLSearchParams();
    if (location) params.append('location', location);
    if (restaurant) params.append('restaurantName', restaurant);
    const res = await fetch(`/api/categoriesByCondition?${params}`);
    if (!res.ok) return [];
    return res.json();
}

// 获取餐馆选项
async function fetchRestaurants(location, category, restaurant) {
    if (!location && !category) {
        return window.allFilterOptions?.restaurantNames || [];
    }
    const params = new URLSearchParams();
    if (location) params.append('location', location);
    if (category) params.append('category', category);
    const res = await fetch(`/api/restaurantsByCondition?${params}`);
    if (!res.ok) return [];
    return res.json();
}

// ==================== 主页面查询/重置 ====================
function queryData() {
    const location = document.getElementById('location').value;
    const category = document.getElementById('category').value;
    const restaurant = document.getElementById('restaurantName').value;
    const statTimeInput = document.getElementById('statTime').value;

    const params = {};
    if (location) params.location = location;
    if (category) params.category = category;
    if (restaurant) params.restaurantName = restaurant;

    if (statTimeInput) {
        const [date, hour] = statTimeInput.split('T');
        params.statTime = `${date} ${hour}:00`;
    }

    const queryString = new URLSearchParams(params).toString();
    const url = `/api/queueData${queryString ? '?' + queryString : ''}`;

    showLoading();

    fetch(url)
        .then(res => res.ok ? res.json() : Promise.reject('请求失败'))
        .then(data => {
            renderTable(data);
            document.querySelector('#dataCount span').textContent = data.length;
        })
        .catch(err => {
            console.error(err);
            showError('查询失败，请重试');
        });
}

function resetForm() {
    document.getElementById('location').value = '';
    document.getElementById('category').value = '';
    document.getElementById('restaurantName').value = '';
    document.getElementById('statTime').value = '';

    if (window.allFilterOptions) {
        updateSelectOptions('location', window.allFilterOptions.locations, false);
        updateSelectOptions('category', window.allFilterOptions.categories, false);
        updateSelectOptions('restaurantName', window.allFilterOptions.restaurantNames, false);
    }
    queryData();
}

function renderTable(data) {
    const tbody = document.getElementById('dataBody');
    tbody.innerHTML = '';

    if (!data || data.length === 0) {
        tbody.innerHTML = `<tr class="empty-row"><td colspan="7"><div class="empty-state"><i class="bi bi-inbox"></i><p>暂无匹配数据</p></div></td></tr>`;
        return;
    }

    data.forEach((item, index) => {
        const tr = document.createElement('tr');
        let statTimeStr = '-';
        if (item.statTime) {
            try {
                const parts = item.statTime.split('T');
                statTimeStr = parts.length === 2 ? parts[0] + ' ' + parts[1].split('.')[0] : item.statTime;
            } catch { statTimeStr = item.statTime; }
        }

        // 排队人数样式类
        const queueNum = item.queueNumber ?? 0;
        const queueClass = queueNum === 0 ? 'zero' : 'non-zero';

        let diffHtml = '<span class="number-zero">0 -</span>';
        if (item.numberDiff > 0) diffHtml = `<span class="number-increase">+${item.numberDiff} ↑</span>`;
        else if (item.numberDiff < 0) diffHtml = `<span class="number-decrease">${item.numberDiff} ↓</span>`;

        tr.innerHTML = `
            <td>${index + 1}</td>
            <td>${item.location || '-'}</td>
            <td>${item.category || '-'}</td>
            <td>${item.restaurantName || '-'}</td>
            <td><span class="queue-number ${queueClass}">${queueNum}</span></td>
            <td class="stat-time">${statTimeStr}</td>
            <td>${diffHtml}</td>
        `;
        tbody.appendChild(tr);
    });
}

function showLoading() {
    document.getElementById('dataBody').innerHTML = `
        <tr class="empty-row"><td colspan="7"><div class="empty-state"><i class="bi bi-hourglass-split"></i><p>正在加载数据...</p></div></td></tr>
    `;
}

function showError(msg) {
    document.getElementById('dataBody').innerHTML = `
        <tr class="empty-row"><td colspan="7"><div class="empty-state"><i class="bi bi-exclamation-triangle"></i><p>${msg}</p></div></td></tr>
    `;
}

// ==================== 对比页面逻辑 ====================
const compareModal = document.getElementById('compareModal');
const compareBtn = document.getElementById('compareBtn');
const closeModal = document.querySelector('#compareModal .close');
const time1Date = document.getElementById('time1Date');
const time1Hour = document.getElementById('time1Hour');
const time2Input = document.getElementById('time2');
const compareSearchBtn = document.getElementById('compareSearchBtn');
const compareResetBtn = document.getElementById('compareResetBtn');
const compareLocation = document.getElementById('compareLocation');
const compareCategory = document.getElementById('compareCategory');
const compareRestaurant = document.getElementById('compareRestaurant');

// 填充小时下拉框（00-23）
function populateHourSelect() {
    const hourSelect = document.getElementById('time1Hour');
    hourSelect.innerHTML = '<option value="">小时</option>';
    for (let i = 0; i < 24; i++) {
        const hour = i.toString().padStart(2, '0');
        const option = document.createElement('option');
        option.value = hour;
        option.textContent = hour + '点';
        hourSelect.appendChild(option);
    }
}

// 打开模态框
compareBtn.addEventListener('click', () => {
    // 填充对比面板的下拉框
    if (window.allFilterOptions) {
        fillSelect('compareLocation', window.allFilterOptions.locations);
        fillSelect('compareCategory', window.allFilterOptions.categories);
        fillSelect('compareRestaurant', window.allFilterOptions.restaurantNames);
    } else {
        fetch('/api/filterOptions')
            .then(res => res.json())
            .then(data => {
                window.allFilterOptions = data;
                fillSelect('compareLocation', data.locations);
                fillSelect('compareCategory', data.categories);
                fillSelect('compareRestaurant', data.restaurantNames);
            });
    }
    // 填充小时下拉框
    populateHourSelect();

    // 设置小时下拉框默认选中当前小时
    const now = new Date();
    const currentHour = now.getHours().toString().padStart(2, '0');
    time1Hour.value = currentHour;

    // 清空之前的选择和时间
    compareLocation.value = '';
    compareCategory.value = '';
    compareRestaurant.value = '';
    time1Date.value = '';
    time2Input.value = '';
    time2Input.disabled = true;
    document.getElementById('compareBody').innerHTML = `
        <tr class="empty-row"><td colspan="7"><div class="empty-state"><i class="bi bi-inbox"></i><p>请选择时间点并点击查询</p></div></td></tr>
    `;
    compareModal.style.display = 'flex';
});

// 关闭模态框
closeModal.addEventListener('click', () => compareModal.style.display = 'none');
window.addEventListener('click', (e) => { if (e.target === compareModal) compareModal.style.display = 'none'; });

// 绑定对比面板下拉框 change 事件
compareLocation.addEventListener('change', onCompareFilterChange);
compareCategory.addEventListener('change', onCompareFilterChange);
compareRestaurant.addEventListener('change', onCompareFilterChange);

async function onCompareFilterChange() {
    if (window.isCompareUpdating) return;
    window.isCompareUpdating = true;
    try {
        await updateCompareDropdowns();
    } finally {
        window.isCompareUpdating = false;
    }
}

async function updateCompareDropdowns() {
    const location = compareLocation.value;
    const category = compareCategory.value;
    const restaurant = compareRestaurant.value;

    const [locations, categories, restaurants] = await Promise.all([
        fetchLocations(location, category, restaurant),
        fetchCategories(location, category, restaurant),
        fetchRestaurants(location, category, restaurant)
    ]);

    updateSelectOptions('compareLocation', locations, true);
    updateSelectOptions('compareCategory', categories, true);
    updateSelectOptions('compareRestaurant', restaurants, true);
}

// 时间点1的日期或小时变化时，更新 time2 的可用状态和默认日期
function onTime1Change() {
    const dateVal = time1Date.value;
    const hourVal = time1Hour.value;

    if (dateVal && hourVal) {
        time2Input.disabled = false;
        time2Input.value = dateVal;
    } else {
        time2Input.disabled = true;
        time2Input.value = '';
    }
}

time1Date.addEventListener('change', onTime1Change);
time1Hour.addEventListener('change', onTime1Change);

// 查询对比数据
compareSearchBtn.addEventListener('click', () => {
    const location = compareLocation.value;
    const category = compareCategory.value;
    const restaurant = compareRestaurant.value;
    const date1 = time1Date.value;
    const hour1 = time1Hour.value;
    const date2 = time2Input.value;

    if (!date1 || !hour1 || !date2) {
        alert('请完整选择两个时间点（日期和小时）');
        return;
    }

    const time1Full = `${date1} ${hour1}:00:00`;
    const time2Full = `${date2} ${hour1}:00:00`;

    const params = new URLSearchParams();
    if (location) params.append('location', location);
    if (category) params.append('category', category);
    if (restaurant) params.append('restaurantName', restaurant);
    params.append('statTime1', time1Full);
    params.append('statTime2', time2Full);

    const tbody = document.getElementById('compareBody');
    tbody.innerHTML = `<tr class="empty-row"><td colspan="7"><div class="empty-state"><i class="bi bi-hourglass-split"></i><p>正在加载对比数据...</p></div></td></tr>`;

    fetch(`/api/queueDataCompare?${params}`)
        .then(res => res.ok ? res.json() : Promise.reject())
        .then(data => renderCompareTable(data))
        .catch(() => {
            tbody.innerHTML = `<tr class="empty-row"><td colspan="7"><div class="empty-state"><i class="bi bi-exclamation-triangle"></i><p>查询失败，请重试</p></div></td></tr>`;
        });
});

// 对比重置按钮
compareResetBtn.addEventListener('click', () => {
    compareLocation.value = '';
    compareCategory.value = '';
    compareRestaurant.value = '';
    time1Date.value = '';
    time1Hour.value = '';
    time2Input.value = '';
    time2Input.disabled = true;
    if (window.allFilterOptions) {
        updateSelectOptions('compareLocation', window.allFilterOptions.locations, false);
        updateSelectOptions('compareCategory', window.allFilterOptions.categories, false);
        updateSelectOptions('compareRestaurant', window.allFilterOptions.restaurantNames, false);
    }
    document.getElementById('compareBody').innerHTML = `
        <tr class="empty-row"><td colspan="7"><div class="empty-state"><i class="bi bi-inbox"></i><p>请选择时间点并点击查询</p></div></td></tr>
    `;
});

// 渲染对比表格
function renderCompareTable(data) {
    const tbody = document.getElementById('compareBody');
    tbody.innerHTML = '';

    if (!data || data.length === 0) {
        tbody.innerHTML = `<tr class="empty-row"><td colspan="7"><div class="empty-state"><i class="bi bi-inbox"></i><p>暂无对比数据</p></div></td></tr>`;
        return;
    }

    data.forEach((item, index) => {
        const diff = (item.number2 || 0) - (item.number1 || 0);
        let diffHtml = '<span class="number-zero">0</span>';
        if (diff > 0) diffHtml = `<span class="number-increase">+${diff} ↑</span>`;
        else if (diff < 0) diffHtml = `<span class="number-decrease">${diff} ↓</span>`;

        // 人数样式
        const num1Class = (item.number1 || 0) === 0 ? 'zero' : 'non-zero';
        const num2Class = (item.number2 || 0) === 0 ? 'zero' : 'non-zero';

        const row = `
            <tr>
                <td>${index + 1}</td>
                <td>${item.location || '-'}</td>
                <td>${item.category || '-'}</td>
                <td>${item.restaurantName || '-'}</td>
                <td><span class="queue-number ${num1Class}">${item.number1 ?? 0}</span></td>
                <td><span class="queue-number ${num2Class}">${item.number2 ?? 0}</span></td>
                <td>${diffHtml}</td>
            </tr>
        `;
        tbody.insertAdjacentHTML('beforeend', row);
    });
}