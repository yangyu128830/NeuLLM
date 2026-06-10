<template>
  <div class="food-result-container">
    <div class="food-header">
      <i class="fas fa-utensils"></i>
      <div class="header-text">
        <h3>美食推荐</h3>
        <p class="sub">
          <span class="tag">腾讯地图 POI</span>
          <span v-if="data.keyword">「{{ data.keyword }}」</span>
          <span v-if="data.region"> · {{ data.region }}</span>
          <span v-if="data.adcode"> · adcode {{ data.adcode }}</span>
          <span v-if="data.distance_order" class="sort-hint">
            · 排序：{{ data.distance_order === 'far_first' ? '由远到近' : '由近到远' }}
          </span>
        </p>
      </div>
    </div>

    <div v-if="data.error" class="food-error">
      <i class="fas fa-exclamation-circle"></i>
      {{ data.message || '查询失败' }}
    </div>

    <div v-else class="food-meta-bar">
      共 {{ data.count ?? (data.pois && data.pois.length) ?? 0 }} 条结果
    </div>

    <div v-if="!data.error" class="food-list">
      <div
        v-for="(poi, idx) in (data.pois || [])"
        :key="poi.id || idx"
        class="food-card"
      >
        <div class="food-card-title">
          <span class="name">{{ poi.title || '未知' }}</span>
          <span v-if="poi.category" class="cat">{{ poi.category }}</span>
        </div>
        <div class="food-row" v-if="poi.address">
          <i class="fas fa-map-marker-alt"></i>
          {{ poi.address }}
        </div>
        <div class="food-row" v-if="poi.tel">
          <i class="fas fa-phone"></i>
          {{ poi.tel }}
        </div>
        <div class="food-row dist" v-if="poi.distance_m != null">
          <i class="fas fa-location-arrow"></i>
          距参考点约 {{ Math.round(poi.distance_m) }} m
        </div>
      </div>
      <div v-if="!(data.pois && data.pois.length)" class="food-empty">暂无 POI 数据</div>
    </div>
  </div>
</template>

<script setup>
defineProps({
  data: {
    type: Object,
    default: () => ({})
  }
});
</script>

<style scoped>
.food-result-container {
  background: linear-gradient(145deg, #fff9f5 0%, #fff 100%);
  border-radius: 12px;
  padding: 16px;
  border: 1px solid #f0e0d6;
  max-width: 640px;
}

.food-header {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  margin-bottom: 14px;
  padding-bottom: 12px;
  border-bottom: 1px solid #eee;
}

.food-header > .fa-utensils {
  font-size: 1.6rem;
  color: #e67e22;
  margin-top: 4px;
}

.header-text h3 {
  margin: 0;
  font-size: 1.15rem;
  color: #2c3e50;
}

.sub {
  margin: 6px 0 0;
  font-size: 0.85rem;
  color: #7f8c8d;
}

.sort-hint {
  color: #16a085;
  font-weight: 500;
}

.tag {
  display: inline-block;
  background: #27ae60;
  color: #fff;
  font-size: 0.72rem;
  padding: 2px 8px;
  border-radius: 4px;
  margin-right: 6px;
}

.food-error {
  color: #c0392b;
  padding: 12px;
  background: #fdeaea;
  border-radius: 8px;
  font-size: 0.95rem;
}

.food-error .fa-exclamation-circle {
  margin-right: 8px;
}

.food-meta-bar {
  font-size: 0.82rem;
  color: #95a5a6;
  margin-bottom: 10px;
}

.food-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.food-card {
  background: #fff;
  border: 1px solid #eee;
  border-radius: 10px;
  padding: 12px 14px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.06);
}

.food-card-title {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.food-card-title .name {
  font-weight: 600;
  color: #2c3e50;
  font-size: 1rem;
}

.food-card-title .cat {
  font-size: 0.75rem;
  color: #7f8c8d;
  background: #ecf0f1;
  padding: 2px 8px;
  border-radius: 4px;
}

.food-row {
  font-size: 0.88rem;
  color: #555;
  margin-top: 4px;
  line-height: 1.4;
}

.food-row .fa-map-marker-alt,
.food-row .fa-phone,
.food-row .fa-location-arrow {
  width: 18px;
  color: #e67e22;
  margin-right: 6px;
}

.food-row.dist {
  color: #16a085;
  font-size: 0.82rem;
}

.food-empty {
  text-align: center;
  color: #95a5a6;
  padding: 20px;
  font-size: 0.9rem;
}
</style>
