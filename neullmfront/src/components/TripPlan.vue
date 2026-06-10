<template>
  <div class="trip-plan-container">
    <div class="trip-header">
      <div class="trip-summary">
        <i class="fas fa-map-marked-alt"></i>
        <div class="trip-info">
          <h3>{{ planData.destination }} · {{ planData.days }}天行程</h3>
          <p>预算参考：¥{{ planData.totalBudget }} · 多日安排一览（出游场景由地图 POI 生成）</p>
        </div>
      </div>
    </div>

    <!-- 地图展示 -->
    <div class="map-section">
      <div id="trip-map" ref="mapContainer"></div>
      <div class="map-legend">
        <span class="legend-item"><span class="dot scenic"></span>景点</span>
        <span class="legend-item"><span class="dot restaurant"></span>餐饮</span>
        <span class="legend-item"><span class="dot hotel"></span>住宿</span>
      </div>
    </div>

    <!-- 每日行程列表 -->
    <div class="daily-plans">
      <div
        v-for="day in planData.dailyPlans"
        :key="day.day"
        :class="['day-card', { 'active': activeDay === day.day }]"
        @click="selectDay(day)"
      >
        <div class="day-badge">Day {{ day.day }}</div>
        <div class="day-content">
          <div class="day-theme">{{ day.theme }}</div>
          <div class="spots-preview">
            <span v-for="(spot, idx) in day.spots.slice(0, 3)" :key="idx" class="spot-tag">
              <i :class="spotIcon(spot.type)"></i>
              {{ spot.name }}
            </span>
          </div>
          <div class="day-budget">
            <i class="fas fa-coins"></i> 预算约 ¥{{ day.estimatedCost }}
          </div>
        </div>
        <div class="day-detail" v-if="activeDay === day.day">
          <div class="spots-list">
            <div v-for="(spot, idx) in day.spots" :key="idx" class="spot-item">
              <div class="spot-time-slot">
                <span class="time-badge">{{ spot.timeSlot }}</span>
              </div>
              <div class="spot-info">
                <div class="spot-name">
                  <i :class="['fas', spotIcon(spot.type), spotIconColor(spot.type)]"></i>
                  {{ spot.name }}
                </div>
                <div class="spot-address" v-if="spot.address">
                  <i class="fas fa-map-marker-alt"></i> {{ spot.address }}
                </div>
                <div class="spot-meta">
                  <span v-if="spot.duration" class="meta-item">
                    <i class="fas fa-clock"></i> {{ spot.duration }}分钟
                  </span>
                  <span v-if="spot.tel" class="meta-item">
                    <i class="fas fa-phone"></i> {{ spot.tel }}
                  </span>
                </div>
              </div>
            </div>
          </div>
          <div class="transport-tip" v-if="day.transportTip">
            <i class="fas fa-info-circle"></i> {{ day.transportTip }}
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import AMapLoader from '@amap/amap-jsapi-loader'

const props = defineProps({
  planData: {
    type: Object,
    required: true
  }
})

const mapContainer = ref(null)
const map = ref(null)
const activeDay = ref(1)
const markers = ref([])

const spotIcon = (type) => {
  return {
    scenic: 'fas fa-camera',
    restaurant: 'fas fa-utensils',
    hotel: 'fas fa-bed'
  }[type] || 'fas fa-map-pin'
}

const spotIconColor = (type) => {
  return {
    scenic: 'color-scenic',
    restaurant: 'color-restaurant',
    hotel: 'color-hotel'
  }[type] || ''
}

const selectDay = (day) => {
  activeDay.value = activeDay.value === day.day ? null : day.day
}

const initMap = async () => {
  if (!mapContainer.value || !props.planData.center) return

  try {
    window._AMapSecurityConfig = {
      securityJsCode: import.meta.env.VITE_AMAP_SECURITY_CODE || ''
    }

    const AMap = await AMapLoader.load({
      key: import.meta.env.VITE_AMAP_KEY || '',
      version: '2.0',
      plugins: ['AMap.Marker']
    })

    const [lng, lat] = props.planData.center.split(',').map(Number)

    map.value = new AMap.Map(mapContainer.value, {
      zoom: 12,
      center: [lng, lat],
      viewMode: '2D'
    })

    // 绘制所有标记
    drawMarkers(AMap)
  } catch (err) {
    console.error('地图加载失败:', err)
  }
}

const drawMarkers = (AMap) => {
  if (!map.value) return

  // 清除旧标记
  markers.value.forEach(m => m.setMap(null))
  markers.value = []

  const typeColors = {
    scenic: '#e74c3c',
    restaurant: '#f39c12',
    hotel: '#3498db'
  }

  const typeLabels = {
    scenic: '景',
    restaurant: '餐',
    hotel: '住'
  }

  let dayCounter = 1
  props.planData.dailyPlans.forEach(day => {
    day.spots.forEach((spot, idx) => {
      if (!spot.lng || !spot.lat) return

      const color = typeColors[spot.type] || '#95a5a6'
      const label = typeLabels[spot.type] || '点'

      // 创建自定义标记
      const markerContent = `
        <div style="
          width: 32px; height: 32px;
          background: ${color};
          border-radius: 50%;
          display: flex; align-items: center; justify-content: center;
          color: white; font-weight: bold; font-size: 12px;
          border: 2px solid white;
          box-shadow: 0 2px 6px rgba(0,0,0,0.3);
        ">${dayCounter}</div>
      `

      const marker = new AMap.Marker({
        position: [spot.lng, spot.lat],
        content: markerContent,
        offset: new AMap.Pixel(-16, -32),
        extData: { day: day.day, spot, idx }
      })

      // 悬浮提示
      marker.setTitle(`${spot.timeSlot} · ${spot.name}`)
      marker.on('click', () => {
        activeDay.value = day.day
      })

      marker.setMap(map.value)
      markers.value.push(marker)
      dayCounter++
    })
  })
}

onMounted(() => {
  initMap()
})

watch(() => props.planData, () => {
  if (map.value) {
    initMap()
  }
}, { deep: true })

watch(activeDay, () => {
  if (!map.value || !props.planData.dailyPlans) return

  const day = props.planData.dailyPlans.find(d => d.day === activeDay.value)
  if (!day || !day.spots.length) return

  // 找到该天的第一个有坐标的景点作为中心
  const firstSpot = day.spots.find(s => s.lng && s.lat)
  if (firstSpot) {
    map.value.setCenter([firstSpot.lng, firstSpot.lat])
    map.value.setZoom(13)
  }
})
</script>

<style scoped>
.trip-plan-container {
  background: white;
  border-radius: 16px;
  overflow: hidden;
  margin-top: 15px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
}

.trip-header {
  background: linear-gradient(135deg, #667eea, #764ba2);
  padding: 20px;
  color: white;
}

.trip-summary {
  display: flex;
  align-items: center;
  gap: 15px;
}

.trip-summary i {
  font-size: 2.5rem;
  opacity: 0.9;
}

.trip-info h3 {
  margin: 0 0 5px 0;
  font-size: 1.3rem;
}

.trip-info p {
  margin: 0;
  opacity: 0.85;
  font-size: 0.9rem;
}

.map-section {
  position: relative;
  height: 350px;
}

#trip-map {
  width: 100%;
  height: 100%;
}

.map-legend {
  position: absolute;
  bottom: 10px;
  left: 10px;
  background: rgba(255,255,255,0.95);
  padding: 8px 12px;
  border-radius: 8px;
  display: flex;
  gap: 12px;
  font-size: 0.8rem;
  box-shadow: 0 2px 6px rgba(0,0,0,0.15);
  z-index: 10;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 5px;
}

.dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
}

.dot.scenic { background: #e74c3c; }
.dot.restaurant { background: #f39c12; }
.dot.hotel { background: #3498db; }

.daily-plans {
  padding: 15px;
  display: flex;
  flex-direction: column;
  gap: 10px;
  max-height: 500px;
  overflow-y: auto;
}

.day-card {
  border: 1px solid #e8e8e8;
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s ease;
  background: #fafafa;
}

.day-card:hover {
  border-color: #667eea;
  box-shadow: 0 2px 8px rgba(102, 126, 234, 0.2);
}

.day-card.active {
  border-color: #667eea;
  background: white;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.25);
}

.day-badge {
  background: #667eea;
  color: white;
  padding: 6px 15px;
  font-weight: bold;
  font-size: 0.85rem;
  display: inline-block;
}

.day-content {
  padding: 12px 15px;
}

.day-theme {
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 8px;
  font-size: 0.95rem;
}

.spots-preview {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-bottom: 8px;
}

.spot-tag {
  background: #f0f0f5;
  color: #666;
  padding: 3px 10px;
  border-radius: 20px;
  font-size: 0.78rem;
  display: flex;
  align-items: center;
  gap: 4px;
}

.spot-tag i {
  font-size: 0.7rem;
  color: #888;
}

.day-budget {
  color: #f39c12;
  font-size: 0.85rem;
  font-weight: 500;
}

.day-detail {
  border-top: 1px solid #eee;
  padding: 15px;
  background: white;
}

.spots-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.spot-item {
  display: flex;
  gap: 12px;
  align-items: flex-start;
}

.spot-time-slot {
  flex-shrink: 0;
}

.time-badge {
  background: #667eea;
  color: white;
  padding: 4px 10px;
  border-radius: 12px;
  font-size: 0.75rem;
  font-weight: 500;
  white-space: nowrap;
}

.spot-info {
  flex: 1;
}

.spot-name {
  font-weight: 600;
  color: #2c3e50;
  font-size: 0.95rem;
  margin-bottom: 4px;
}

.spot-name i {
  margin-right: 5px;
}

.color-scenic { color: #e74c3c; }
.color-restaurant { color: #f39c12; }
.color-hotel { color: #3498db; }

.spot-address {
  color: #888;
  font-size: 0.8rem;
  margin-bottom: 4px;
}

.spot-address i {
  margin-right: 4px;
}

.spot-meta {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.meta-item {
  color: #aaa;
  font-size: 0.78rem;
  display: flex;
  align-items: center;
  gap: 3px;
}

.transport-tip {
  margin-top: 12px;
  padding: 10px 12px;
  background: #f0f4ff;
  border-radius: 8px;
  color: #555;
  font-size: 0.82rem;
  line-height: 1.5;
}

.transport-tip i {
  color: #667eea;
  margin-right: 6px;
}
</style>
