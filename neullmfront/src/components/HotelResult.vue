<template>
  <div class="hotel-result-container">
    <!-- 头部 -->
    <div class="hotel-header">
      <i class="fas fa-hotel"></i>
      <div class="header-info">
        <h3>{{ result.query.city }} · 酒店推荐</h3>
        <p>{{ result.query.checkInDate }} 入住 · {{ result.query.nights }} 晚 · {{ result.query.guests }} 位旅客</p>
      </div>
      <span class="data-source" :class="result.dataSource">
        {{ result.dataSource === 'xotelo_api' ? '🟢 实时数据' : result.dataSource === 'amap_api' ? '🟢 高德数据' : '🟡 演示数据' }}
      </span>
    </div>

    <!-- 酒店列表 -->
    <div class="hotel-list">
      <div
        v-for="(hotel, idx) in result.hotels"
        :key="idx"
        class="hotel-card"
      >
        <!-- 名称 + 评分 -->
        <div class="hotel-card-header">
          <div class="hotel-name">
            <i class="fas fa-building"></i>
            {{ hotel.hotelName || hotel.name || '未知酒店' }}
            <!-- 星级标签 -->
            <span class="star-badge" v-if="getStarLevel(hotel) > 0">
              {{ getStarLevel(hotel) }}星级
            </span>
          </div>
          <div class="hotel-rating" v-if="getRating(hotel) > 0">
            <i class="fas fa-star"></i> {{ getRating(hotel) }}
            <span class="review-count" v-if="getReviewCount(hotel) > 0">
              ({{ getReviewCount(hotel) }}条)
            </span>
          </div>
        </div>

        <!-- 价格区域 -->
        <div class="hotel-price-section">
          <!-- 实时报价 -->
          <div v-if="hotel.liveRates && hotel.liveRates.length > 0" class="live-rates">
            <div class="rate-item" v-for="(rate, rIdx) in hotel.liveRates.slice(0, 3)" :key="rIdx">
              <span class="platform-name">{{ rate.platform }}</span>
              <span class="platform-price">¥{{ getCNY(rate.price) }}<span class="price-unit">/晚</span></span>
            </div>
            <div class="price-summary" v-if="hotel.lowestRateCNY_approx">
              最低 ¥{{ hotel.lowestRateCNY_approx }}/晚
            </div>
          </div>

          <!-- Mock 数据价格 -->
          <div v-else-if="hotel.pricePerNight_CNY || hotel.pricePerNight" class="mock-price">
            <span class="price-label">参考价</span>
            <span class="price-value">¥{{ hotel.pricePerNight_CNY || hotel.pricePerNight }}</span>
            <span class="price-unit">/晚</span>
            <span class="total-price" v-if="result.query.nights">
              · 共 ¥{{ (hotel.pricePerNight_CNY || hotel.pricePerNight) * result.query.nights }}
            </span>
          </div>

          <!-- 估算价格 -->
          <div v-else-if="hotel.estimatedMinPrice" class="estimated-price">
            <span class="price-label">估算价</span>
            <span class="price-value">¥{{ Math.round(hotel.estimatedMinPrice * 7.1) }}</span>
            <span class="price-unit">/晚起</span>
          </div>

          <!-- 高德POI数据：显示联系咨询 -->
          <div v-else-if="hotel.priceInfo" class="price-info">
            <i class="fas fa-info-circle"></i> {{ hotel.priceInfo }}
          </div>

          <!-- 无价格信息 -->
          <div v-else class="no-price">价格暂不可用</div>
        </div>

        <!-- 标签 -->
        <div class="hotel-tags" v-if="getTags(hotel).length > 0">
          <span class="tag" v-for="(tag, tIdx) in getTags(hotel).slice(0, 4)" :key="tIdx">{{ tag }}</span>
        </div>

        <!-- 地址 -->
        <div class="hotel-address" v-if="hotel.address">
          <i class="fas fa-map-marker-alt"></i> {{ hotel.address }}
        </div>

        <!-- 电话 -->
        <div class="hotel-phone" v-if="hotel.contactPhone">
          <i class="fas fa-phone-alt"></i>
          <a :href="'tel:' + hotel.contactPhone">{{ hotel.contactPhone }}</a>
        </div>

        <!-- 地图链接 -->
        <div class="hotel-map-link" v-if="hotel.lng && hotel.lat">
          <a :href="'https://uri.amap.com/marker?position=' + hotel.lng + ',' + hotel.lat + '&name=' + encodeURIComponent(hotel.hotelName || hotel.name)" target="_blank">
            <i class="fas fa-external-link-alt"></i> 在高德地图查看
          </a>
        </div>

        <!-- 操作按钮 -->
        <div class="hotel-actions">
          <a
            v-if="hotel.url || hotel.bookingLink"
            :href="hotel.url || hotel.bookingLink"
            target="_blank"
            class="booking-btn"
          >
            <i class="fas fa-external-link-alt"></i> 查看详情
          </a>
          <button class="book-btn" @click="$emit('book', hotel)">
            <i class="fas fa-calendar-check"></i> 预订此酒店
          </button>
        </div>
      </div>
    </div>

    <!-- 提示 -->
    <div class="hotel-tips" v-if="result.tips">
      <i class="fas fa-info-circle"></i> {{ result.tips }}
    </div>
    <div class="price-note" v-if="result.priceNote">
      <i class="fas fa-calculator"></i> {{ result.priceNote }}
    </div>
  </div>
</template>

<script setup>
const props = defineProps({
  result: {
    type: Object,
    required: true
  }
});

const emit = defineEmits(['book']);

const getRating = (hotel) => {
  const r = hotel.rating || (hotel.review_summary && hotel.review_summary.rating) || 0;
  return r ? parseFloat(r).toFixed(1) : '';
};

const getReviewCount = (hotel) => {
  return hotel.reviewCount || (hotel.review_summary && hotel.review_summary.count) || 0;
};

const getTags = (hotel) => {
  if (hotel.tags && Array.isArray(hotel.tags)) return hotel.tags;
  if (hotel.mentions && Array.isArray(hotel.mentions)) return hotel.mentions;
  return [];
};

const getStarLevel = (hotel) => {
  return hotel.starLevel || 0;
};

// Xotelo 返回 USD，简单换算人民币
const getCNY = (usdPrice) => {
  if (!usdPrice) return '—';
  return Math.round(usdPrice * 7.1);
};
</script>

<style scoped>
.hotel-result-container {
  background: white;
  border-radius: 14px;
  overflow: hidden;
  margin-top: 12px;
  box-shadow: 0 4px 16px rgba(0,0,0,0.08);
}

/* 头部渐变 */
.hotel-header {
  background: linear-gradient(135deg, #2980b9, #3498db);
  color: white;
  padding: 16px 20px;
  display: flex;
  align-items: center;
  gap: 14px;
}
.hotel-header i {
  font-size: 1.8rem;
  opacity: 0.9;
}
.header-info h3 {
  margin: 0 0 4px 0;
  font-size: 1.15rem;
}
.header-info p {
  margin: 0;
  font-size: 0.85rem;
  opacity: 0.85;
}
.data-source {
  margin-left: auto;
  font-size: 0.75rem;
  background: rgba(255,255,255,0.2);
  padding: 3px 10px;
  border-radius: 12px;
}
.data-source.xotelo_api { background: rgba(46,204,113,0.3); }
.data-source.amap_api { background: rgba(46,204,113,0.3); }
.data-source.mock_fallback { background: rgba(241,196,15,0.3); }

/* 酒店列表 */
.hotel-list {
  padding: 12px 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;
  max-height: 520px;
  overflow-y: auto;
}

/* 单张酒店卡片 */
.hotel-card {
  border: 1px solid #e8e8e8;
  border-radius: 12px;
  padding: 14px 16px;
  background: #fafbfd;
  transition: all 0.25s ease;
}
.hotel-card:hover {
  border-color: #3498db;
  box-shadow: 0 4px 12px rgba(52,152,219,0.15);
}

/* 名称 + 评分 */
.hotel-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}
.hotel-name {
  font-size: 1.05rem;
  font-weight: 600;
  color: #2c3e50;
  display: flex;
  align-items: center;
  gap: 6px;
}
.hotel-name i { color: #3498db; }
.star-badge {
  font-size: 0.7rem;
  background: linear-gradient(135deg, #f6d365, #fda085);
  color: white;
  padding: 2px 8px;
  border-radius: 10px;
  margin-left: 8px;
  font-weight: 500;
}
.hotel-rating {
  background: #f39c12;
  color: white;
  padding: 3px 10px;
  border-radius: 12px;
  font-size: 0.82rem;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 4px;
  white-space: nowrap;
}
.review-count {
  font-weight: 400;
  opacity: 0.85;
  font-size: 0.75rem;
}

/* 价格区域 */
.hotel-price-section {
  margin-bottom: 8px;
}

/* 实时报价 */
.live-rates {
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.rate-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: white;
  padding: 6px 12px;
  border-radius: 8px;
  border: 1px solid #eef;
}
.platform-name {
  font-size: 0.85rem;
  color: #555;
}
.platform-price {
  font-size: 1rem;
  font-weight: 700;
  color: #e74c3c;
}
.price-unit {
  font-size: 0.75rem;
  font-weight: 400;
  color: #888;
}
.price-summary {
  margin-top: 4px;
  font-size: 0.85rem;
  color: #27ae60;
  font-weight: 600;
}

/* Mock 价格 */
.mock-price {
  background: white;
  padding: 8px 14px;
  border-radius: 8px;
  border: 1px solid #e8e8e8;
  display: flex;
  align-items: baseline;
  gap: 6px;
  flex-wrap: wrap;
}
.price-label {
  font-size: 0.8rem;
  color: #888;
}
.price-value {
  font-size: 1.2rem;
  font-weight: 700;
  color: #e74c3c;
}
.total-price {
  font-size: 0.85rem;
  color: #555;
  margin-left: 4px;
}

/* 估算价格 */
.estimated-price {
  background: #fffbeb;
  padding: 8px 14px;
  border-radius: 8px;
  display: flex;
  align-items: baseline;
  gap: 6px;
  flex-wrap: wrap;
}

.no-price {
  color: #aaa;
  font-size: 0.85rem;
  padding: 6px 0;
}

.price-info {
  background: #f0f7ff;
  color: #2980b9;
  padding: 8px 14px;
  border-radius: 8px;
  font-size: 0.85rem;
  display: flex;
  align-items: center;
  gap: 6px;
}

/* 标签 */
.hotel-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 5px;
  margin-bottom: 8px;
}
.tag {
  background: #eef4ff;
  color: #2980b9;
  padding: 2px 10px;
  border-radius: 12px;
  font-size: 0.75rem;
}

/* 地址 */
.hotel-address {
  font-size: 0.82rem;
  color: #888;
  margin-bottom: 6px;
  display: flex;
  align-items: center;
  gap: 4px;
}

/* 电话 */
.hotel-phone {
  font-size: 0.82rem;
  color: #3498db;
  margin-bottom: 6px;
  display: flex;
  align-items: center;
  gap: 4px;
}

.hotel-phone a {
  color: #3498db;
  text-decoration: none;
}

.hotel-phone a:hover {
  text-decoration: underline;
}

/* 地图链接 */
.hotel-map-link {
  margin-bottom: 10px;
}

.hotel-map-link a {
  font-size: 0.82rem;
  color: #27ae60;
  text-decoration: none;
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.hotel-map-link a:hover {
  text-decoration: underline;
}

/* 操作按钮 */
.hotel-actions {
  display: flex;
  gap: 10px;
}
.booking-btn {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  padding: 8px 12px;
  background: #f0f7ff;
  color: #2980b9;
  border: 1px solid #bfdbfe;
  border-radius: 8px;
  font-size: 0.85rem;
  text-decoration: none;
  transition: all 0.2s;
}
.booking-btn:hover {
  background: #dbeafe;
}

.book-btn {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  padding: 8px 12px;
  background: #27ae60;
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 0.85rem;
  cursor: pointer;
  transition: all 0.2s;
}
.book-btn:hover {
  background: #219a52;
  transform: translateY(-1px);
}

/* 底部提示 */
.hotel-tips {
  padding: 10px 16px;
  background: #f8f9fa;
  font-size: 0.82rem;
  color: #555;
  display: flex;
  align-items: flex-start;
  gap: 6px;
  border-top: 1px solid #eee;
}
.price-note {
  padding: 8px 16px;
  background: #f0fdf4;
  font-size: 0.78rem;
  color: #15803d;
  display: flex;
  align-items: center;
  gap: 6px;
}
</style>

