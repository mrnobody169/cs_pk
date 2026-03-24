<template>
  <div class="page">
    <div class="page-header">
      <h1 class="page-title">📊 Profit Dashboard</h1>
    </div>

    <!-- Filters -->
    <div class="filter-bar">
      <div class="form-group">
        <label>Nhóm</label>
        <select class="select" v-model="selectedGroupId" @change="load">
          <option :value="null">Tất cả nhóm</option>
          <option v-for="g in groups" :key="g.id" :value="g.id">{{ g.name }}</option>
        </select>
      </div>
      <div class="form-group">
        <label>Từ ngày</label>
        <input class="input" type="date" v-model="dateFrom" @change="load" style="width:160px" />
      </div>
      <div class="form-group">
        <label>Đến ngày</label>
        <input class="input" type="date" v-model="dateTo" @change="load" style="width:160px" />
      </div>
    </div>

    <div class="loading-center" v-if="loading"><div class="spinner"></div></div>

    <!-- Summary Stats -->
    <div class="stats-grid" v-if="!loading && rows.length">
      <div class="stat-card">
        <div class="stat-label">Tổng Buy-in</div>
        <div class="stat-value money">{{ formatMoney(totals.buyIn) }}</div>
      </div>
      <div class="stat-card">
        <div class="stat-label">Tổng Cash-out</div>
        <div class="stat-value money">{{ formatMoney(totals.cashOut) }}</div>
      </div>
      <div class="stat-card">
        <div class="stat-label">Tổng Profit</div>
        <div :class="['stat-value', 'money', diffClass(totals.profit)]">{{ formatMoney(totals.profit) }}</div>
      </div>
      <div class="stat-card">
        <div class="stat-label">Số thành viên</div>
        <div class="stat-value">{{ rows.length }}</div>
      </div>
    </div>

    <!-- Profit Table -->
    <div class="table-wrap" v-if="!loading && rows.length">
      <table>
        <thead>
          <tr>
            <th>#</th>
            <th>Thành viên</th>
            <th style="text-align:right">Ngày chơi</th>
            <th style="text-align:right">Buy-in</th>
            <th style="text-align:right">Cash-out</th>
            <th style="text-align:right">Profit</th>
            <th style="text-align:center">Chưa TT</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="(r, i) in rows" :key="r.memberUserId">
            <td class="text-muted">{{ i + 1 }}</td>
            <td>
              <div style="display:flex;align-items:center;gap:8px">
                <span class="rank-badge" v-if="i < 3" :class="`rank-${i+1}`">{{ ['🥇','🥈','🥉'][i] }}</span>
                <strong>{{ r.memberName }}</strong>
              </div>
            </td>
            <td style="text-align:right">{{ r.daysPlayed }}</td>
            <td style="text-align:right" class="money">{{ formatMoney(r.totalBuyIn) }}</td>
            <td style="text-align:right" class="money">{{ formatMoney(r.totalCashOut) }}</td>
            <td style="text-align:right" :class="['money', diffClass(r.totalProfit)]">
              <strong>{{ formatMoney(r.totalProfit) }}</strong>
            </td>
            <td style="text-align:center">
              <span class="badge badge-danger" v-if="r.unpaidCount > 0">{{ r.unpaidCount }}</span>
              <span class="text-muted" v-else>0</span>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <div v-if="!loading && rows.length === 0" class="empty-state">
      <p>Không có dữ liệu trong khoảng thời gian này.</p>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { getGroups, getDashboardProfit } from '../api'

const groups = ref([])
const rows = ref([])
const loading = ref(true)
const selectedGroupId = ref(null)

const today = new Date()
const dateFrom = ref(new Date(today.getFullYear(), today.getMonth(), 1).toISOString().split('T')[0])
const dateTo = ref(today.toISOString().split('T')[0])

const totals = computed(() => {
  const b = rows.value.reduce((s, r) => s + (r.totalBuyIn || 0), 0)
  const c = rows.value.reduce((s, r) => s + (r.totalCashOut || 0), 0)
  return { buyIn: b, cashOut: c, profit: c - b }
})

onMounted(async () => {
  try {
    const { data } = await getGroups()
    groups.value = data
  } catch (e) { /* ignore */ }
  await load()
})

async function load() {
  loading.value = true
  try {
    const { data } = await getDashboardProfit(dateFrom.value, dateTo.value, selectedGroupId.value)
    rows.value = data
  } finally {
    loading.value = false
  }
}

function formatMoney(v) {
  if (v == null) return '—'
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND', maximumFractionDigits: 0 }).format(v)
}
function diffClass(v) { return v > 0 ? 'positive' : v < 0 ? 'negative' : 'zero' }
</script>

<style scoped>
.filter-bar {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
  margin-bottom: 24px;
}
.rank-badge {
  font-size: 18px;
  line-height: 1;
}
.text-muted { color: var(--text-muted); }
</style>
