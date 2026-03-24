<template>
  <div class="page">
    <div class="page-header">
      <div>
        <a class="back-link" @click.prevent="$router.back()">← Quay lại nhóm</a>
        <h1 class="page-title">📊 Báo cáo</h1>
      </div>
    </div>

    <!-- Date range -->
    <div style="display:flex;gap:12px;margin-bottom:24px;flex-wrap:wrap;align-items:flex-end">
      <div class="form-group">
        <label>Từ ngày</label>
        <input class="input" type="date" v-model="dateFrom" style="width:170px" />
      </div>
      <div class="form-group">
        <label>Đến ngày</label>
        <input class="input" type="date" v-model="dateTo" style="width:170px" />
      </div>
      <button class="btn btn-primary btn-sm" @click="loadAll">Xem báo cáo</button>
    </div>

    <div class="loading-center" v-if="loading"><div class="spinner"></div></div>

    <template v-if="!loading">
      <!-- Summary Stats -->
      <div class="stats-grid" v-if="summary">
        <div class="stat-card">
          <div class="stat-label">Tổng phiên</div>
          <div class="stat-value">{{ summary.totalSessions }}</div>
        </div>
        <div class="stat-card">
          <div class="stat-label">Tổng Buy-in</div>
          <div class="stat-value money">{{ formatMoney(summary.totalBuyIn) }}</div>
        </div>
        <div class="stat-card">
          <div class="stat-label">Tổng Cash-out</div>
          <div class="stat-value money">{{ formatMoney(summary.totalCashOut) }}</div>
        </div>
        <div class="stat-card">
          <div class="stat-label">Tổng Profit</div>
          <div :class="['stat-value', 'money', diffClass(summary.totalProfit)]">{{ formatMoney(summary.totalProfit) }}</div>
        </div>
      </div>

      <!-- Tabs -->
      <div class="tabs">
        <button :class="['tab', { active: tab === 'player' }]" @click="tab = 'player'">Theo người chơi</button>
        <button :class="['tab', { active: tab === 'day' }]" @click="tab = 'day'">Theo ngày</button>
      </div>

      <!-- By Player -->
      <div class="table-wrap" v-if="tab === 'player' && byPlayer.length">
        <table>
          <thead>
            <tr>
              <th>Người chơi</th>
              <th style="text-align:right">Số phiên</th>
              <th style="text-align:right">Tổng Buy-in</th>
              <th style="text-align:right">Tổng Cash-out</th>
              <th style="text-align:right">Tổng Profit</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="r in byPlayer" :key="r.playerName">
              <td><strong>{{ r.playerName }}</strong></td>
              <td style="text-align:right">{{ r.sessionCount }}</td>
              <td style="text-align:right" class="money">{{ formatMoney(r.totalBuyIn) }}</td>
              <td style="text-align:right" class="money">{{ formatMoney(r.totalCashOut) }}</td>
              <td style="text-align:right" :class="['money', diffClass(r.totalProfit)]">{{ formatMoney(r.totalProfit) }}</td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- By Day -->
      <div class="table-wrap" v-if="tab === 'day' && byDay.length">
        <table>
          <thead>
            <tr>
              <th>Ngày</th>
              <th style="text-align:right">Người chơi</th>
              <th style="text-align:right">Tổng Buy-in</th>
              <th style="text-align:right">Tổng Cash-out</th>
              <th style="text-align:right">Tổng Profit</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="r in byDay" :key="r.sessionDate">
              <td><strong>{{ formatShortDate(r.sessionDate) }}</strong></td>
              <td style="text-align:right">{{ r.playerCount }}</td>
              <td style="text-align:right" class="money">{{ formatMoney(r.totalBuyIn) }}</td>
              <td style="text-align:right" class="money">{{ formatMoney(r.totalCashOut) }}</td>
              <td style="text-align:right" :class="['money', diffClass(r.totalProfit)]">{{ formatMoney(r.totalProfit) }}</td>
            </tr>
          </tbody>
        </table>
      </div>

      <div v-if="!loading && ((tab === 'player' && !byPlayer.length) || (tab === 'day' && !byDay.length))" class="empty-state">
        <p>Không có dữ liệu trong khoảng thời gian này.</p>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute } from 'vue-router'
import { getReportSummary, getReportByPlayer, getReportByDay } from '../api'
import { useAppStore } from '../stores/app'

const route = useRoute()
const app = useAppStore()
const groupId = computed(() => route.params.id)

const today = new Date()
const dateFrom = ref(new Date(today.getFullYear(), today.getMonth(), 1).toISOString().split('T')[0])
const dateTo = ref(today.toISOString().split('T')[0])

const loading = ref(true)
const tab = ref('player')
const summary = ref(null)
const byPlayer = ref([])
const byDay = ref([])

onMounted(() => loadAll())

async function loadAll() {
  loading.value = true
  try {
    const [s, p, d] = await Promise.all([
      getReportSummary(groupId.value, dateFrom.value, dateTo.value),
      getReportByPlayer(groupId.value, dateFrom.value, dateTo.value),
      getReportByDay(groupId.value, dateFrom.value, dateTo.value)
    ])
    summary.value = s.data
    byPlayer.value = p.data
    byDay.value = d.data
  } catch (e) {
    app.toast('Lỗi load báo cáo', 'error')
  } finally {
    loading.value = false
  }
}

function formatMoney(v) {
  if (v == null) return '—'
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND', maximumFractionDigits: 0 }).format(v)
}
function formatShortDate(d) {
  if (!d) return ''
  return new Date(d + 'T00:00:00').toLocaleDateString('vi-VN', { day: '2-digit', month: '2-digit', year: 'numeric' })
}
function diffClass(v) { return v > 0 ? 'positive' : v < 0 ? 'negative' : 'zero' }
</script>

<style scoped>
.back-link { font-size: 13px; color: var(--text-muted); display: block; margin-bottom: 8px; text-decoration: none; cursor: pointer; }
.back-link:hover { color: var(--accent); }
</style>
