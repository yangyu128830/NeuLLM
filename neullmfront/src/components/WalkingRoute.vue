<template>
  <div class="map-container">
    <div id="map-container" ref="mapContainer"></div>

    <div v-if="loading" class="loading-indicator">
      <div class="loader"></div>
      <span>正在规划路线...</span>
    </div>
    <div v-if="error" class="error-message">
      {{ error }}
    </div>
  </div>
</template>

<script>
import { ref, onMounted, watch } from 'vue'
import AMapLoader from '@amap/amap-jsapi-loader'

export default {
  props: {
    origin: {
      type: String,
      required: true
    },
    destination: {
      type: String,
      required: true
    }
  },
  setup(props) {
    const mapContainer = ref(null)
    const map = ref(null)
    const routeInfo = ref(null)
    const loading = ref(false)
    const error = ref(null)

    // 高德安全密钥配置（从环境变量读取）
    const securityConfig = {
      key: import.meta.env.VITE_AMAP_KEY || '',
      securityJsCode: import.meta.env.VITE_AMAP_SECURITY_CODE || ''
    }

    // 解析经纬度字符串
    const parseLngLat = (str) => {
      const [lng, lat] = str.split(',').map(Number)
      return [lng, lat]
    }

    // 初始化地图
    const initMap = async () => {
      try {
        // 设置全局安全密钥配置
        window._AMapSecurityConfig = {
          securityJsCode: securityConfig.securityJsCode
        }
        const AMap = await AMapLoader.load({
          key: securityConfig.key,
          version: '2.0',
          plugins: ['AMap.Walking'],
        })

        map.value = new AMap.Map(mapContainer.value, {
          zoom: 16,
          viewMode: '2D'
        })

        // 初始加载路线
        searchRoute(AMap)

      } catch (err) {
        console.error('地图加载失败:', err)
        error.value = `地图加载失败: ${err.message}`
      }
    }

    // 搜索步行路线
    const searchRoute = async (AMap) => {
      if (!AMap || !props.origin || !props.destination) return

      try {
        loading.value = true
        error.value = null

        // 解析起点和终点
        const startPoint = parseLngLat(props.origin)
        const endPoint = parseLngLat(props.destination)

        // 创建步行路线规划实例
        const walking = new AMap.Walking({
          map: map.value
        })

        // 执行路线规划
        walking.search(startPoint, endPoint, (status, result) => {
          loading.value = false

          if (status === 'complete') {
            const route = result.routes[0]

            // 提取路线信息
            routeInfo.value = {
              distance: route.distance,
              time: Math.ceil(route.time / 60), // 秒转分钟
              steps: route.steps.map(step => ({
                instruction: step.instruction.replace(/<[^>]+>/g, ''),
                orientation: getDirectionName(step.orientation),
                distance: step.distance
              }))
            }

            // 添加起点终点标记
            addMarkers(AMap, startPoint, endPoint)

          } else {
            console.error('路线规划失败:', result)
            error.value = `路线规划失败: ${result.message || '未知错误'}`
          }
        })

      } catch (err) {
        loading.value = false
        error.value = `路线规划错误: ${err.message}`
        console.error('路线规划错误:', err)
      }
    }

    // 添加起点终点标记
    const addMarkers = (AMap, start, end) => {
      // 清除旧标记
      map.value.clearMap()

      // 添加起点标记
      new AMap.Marker({
        position: start,
        map: map.value,
        content: '',
        offset: new AMap.Pixel(-15, -30)
      })

      // 添加终点标记
      new AMap.Marker({
        position: end,
        map: map.value,
        content: '',
        offset: new AMap.Pixel(-15, -30)
      })
    }

    // 将方向角度转换为中文名称
    const getDirectionName = (angle) => {
      const directions = [
        { min: 337.5, max: 360, name: '北' },
        { min: 0, max: 22.5, name: '北' },
        { min: 22.5, max: 67.5, name: '东北' },
        { min: 67.5, max: 112.5, name: '东' },
        { min: 112.5, max: 157.5, name: '东南' },
        { min: 157.5, max: 202.5, name: '南' },
        { min: 202.5, max: 247.5, name: '西南' },
        { min: 247.5, max: 292.5, name: '西' },
        { min: 292.5, max: 337.5, name: '西北' }
      ]

      const match = directions.find(d =>
          (angle >= d.min && angle < d.max) ||
          (d.min === 337.5 && angle >= 337.5)
      )

      return match ? match.name : '未知'
    }

    onMounted(initMap)

    // 监听参数变化
    watch(() => [props.origin, props.destination], () => {
      if (map.value) {
        searchRoute()
      }
    })

    return {
      mapContainer,
      routeInfo,
      loading,
      error
    }
  }
}
</script>

<style scoped>
.map-container {
  position: relative;
  width: 100%;
  height: 500px;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 10px rgba(0,0,0,0.1);
}

#map-container {
  width: 100%;
  height: 100%;
}

.route-info {
  position: absolute;
  bottom: 10px;
  left: 10px;
  background: rgba(255, 255, 255, 0.9);
  padding: 15px;
  border-radius: 8px;
  max-width: 300px;
  box-shadow: 0 2px 6px rgba(0,0,0,0.2);
  font-size: 14px;
  z-index: 10;
}

.route-info h3 {
  margin: 0 0 10px 0;
  font-size: 16px;
  color: #333;
}

.route-info ol {
  padding-left: 20px;
  margin: 0;
}

.route-info li {
  margin-bottom: 5px;
  line-height: 1.4;
}

.loading-indicator {
  position: absolute;
  top: 10px;
  left: 50%;
  transform: translateX(-50%);
  background: rgba(255, 255, 255, 0.9);
  padding: 10px 15px;
  border-radius: 20px;
  display: flex;
  align-items: center;
  gap: 10px;
  z-index: 10;
  box-shadow: 0 2px 5px rgba(0,0,0,0.1);
}

.loader {
  border: 3px solid #f3f3f3;
  border-top: 3px solid #3498db;
  border-radius: 50%;
  width: 20px;
  height: 20px;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.error-message {
  position: absolute;
  top: 10px;
  left: 50%;
  transform: translateX(-50%);
  background: rgba(231, 76, 60, 0.9);
  color: white;
  padding: 10px 20px;
  border-radius: 4px;
  z-index: 10;
  max-width: 90%;
  text-align: center;
}

/* 自定义标记样式 */
:deep(.start-marker) {
  background: #4CAF50;
  color: white;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  white-space: nowrap;
  box-shadow: 0 1px 3px rgba(0,0,0,0.3);
}

:deep(.end-marker) {
  background: #F44336;
  color: white;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  white-space: nowrap;
  box-shadow: 0 1px 3px rgba(0,0,0,0.3);
}
</style>