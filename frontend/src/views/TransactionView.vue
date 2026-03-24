<template>
  <div class="page">
    <div class="page-header">
      <h1 class="page-title">📋 Transaction Log</h1>
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
      <div class="form-group">
        <label>Trạng thái</label>
        <select class="select" v-model="statusFilter" style="width:140px">
          <option value="">Tất cả</option>
          <option value="UNPAID">UNPAID</option>
          <option value="PAID">PAID</option>
          <option value="PARTIAL">PARTIAL</option>
          <option value="DISPUTED">DISPUTED</option>
        </select>
      </div>
    </div>

    <div class="loading-center" v-if="loading"><div class="spinner"></div></div>

    <!-- Transaction List -->
    <div v-if="!loading && filteredRows.length">
      <p class="result-count">{{ filteredRows.length }} giao dịch</p>
      <div class="transaction-list">
        <div v-for="(dateGroup, date) in groupedByDate" :key="date" class="date-group">
          <div class="date-header">
            <span class="date-label">{{ formatDate(date) }}</span>
            <span class="date-count">{{ dateGroup.length }} entries</span>
          </div>
          <div v-for="t in dateGroup" :key="t.entryId" class="transaction-card" :class="statusClass(t.settlementStatus)">
            <div class="tx-left">
              <div class="tx-member">{{ t.memberName }}</div>
              <div class="tx-group text-muted">{{ t.groupName }}</div>
              <div class="tx-note text-muted" v-if="t.note">{{ t.note }}</div>
            </div>
            <div class="tx-right">
              <div class="tx-amounts">
                <span class="tx-buyin">↗ {{ formatMoney(t.buyInTotal) }}</span>
                <span class="tx-cashout">↙ {{ formatMoney(t.cashOutTotal) }}</span>
              </div>
              <div :class="['tx-profit', diffClass(t.profit)]">
                {{ formatMoney(t.profit) }}
              </div>
              <span :class="['status-pill', `pill-${t.settlementStatus?.toLowerCase()}`]">
                {{ t.settlementStatus }}
              </span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div v-if="!loading && filteredRows.length === 0" class="empty-state">
      <p>Không có giao dịch nào.</p>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { getGroups, getDashboardTransactions } from '../api'

const groups = ref([])
const rows = ref([])
const loading = ref(true)
const selectedGroupId = ref(null)
const statusFilter = ref('')

const today = new Date()
const dateFrom = ref(new Date(today.getFullYear(), today.getMonth(), 1).toISOString().split('T')[0])
const dateTo = ref(today.toISOString().split('T')[0])

const filteredRows = computed(() => {
  if (!statusFilter.value) return rows.value
  return rows.value.filter(r => r.settlementStatus === statusFilter.value)
})

const groupedByDate = computed(() => {
  const map = {}
  for (const r of filteredRows.value) {
    const d = r.sessionDate
    if (!map[d]) map[d] = []
    map[d].push(r)
  }
  return map
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
    const { data } = await getDashboardTransactions(dateFrom.value, dateTo.value, selectedGroupId.value)
    rows.value = data
  } finally {
    loading.value = false
  }
}

function formatMoney(v) {
  if (v == null) return '—'
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND', maximumFractionDigits: 0 }).format(v)
}
function formatDate(d) {
  if (!d) return ''
  return new Date(d + 'T00:00:00').toLocaleDateString('vi-VN', { weekday: 'long', day: '2-digit', month: '2-digit', year: 'numeric' })
}
function diffClass(v) { return v > 0 ? 'positive' : v < 0 ? 'negative' : 'zero' }
function statusClass(s) {
  return {
    PAID: 'tx-paid',
    UNPAID: 'tx-unpaid',
    PARTIAL: 'tx-partial',
    DISPUTED: 'tx-disputed'
  }[s] || ''
}
</script>

<style scoped>
.filter-bar {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
  margin-bottom: 24px;
}
.result-count {
  font-size: 13px;
  color: var(--text-muted);
  margin-bottom: 12px;
}
.text-muted { color: var(--text-muted); }

/* Date group */
.date-group { margin-bottom: 20px; }
.date-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
  border-bottom: 1px solid var(--border);
  margin-bottom: 8px;
}
.date-label {
  font-weight: 700;
  font-size: 15px;
  color: var(--text-primary);
}
.date-count {
  font-size: 12px;
  color: var(--text-muted);
}

/* Transaction cards */
.transaction-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  border-radius: var(--radius);
  margin-bottom: 6px;
  border-left: 4px solid transparent;
  background: var(--bg-secondary);
  transition: transform 0.15s, box-shadow 0.15s;
}
.transaction-card:hover {
  transform: translateX(4px);
  box-shadow: 0 2px 12px rgba(0,0,0,0.12);
}

/* Color-coded borders */
.tx-paid { border-left-color: var(--success, #22c55e); }
.tx-unpaid { border-left-color: var(--danger, #ef4444); }
.tx-partial { border-left-color: var(--warning, #f59e0b); }
.tx-disputed { border-left-color: var(--info, #3b82f6); }

.tx-left { flex: 1; }
.tx-member { font-weight: 600; font-size: 15px; margin-bottom: 2px; }
.tx-group { font-size: 12px; }
.tx-note { font-size: 12px; font-style: italic; margin-top: 2px; }

.tx-right {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 4px;
}
.tx-amounts {
  display: flex;
  gap: 12px;
  font-size: 13px;
}
.tx-buyin { color: var(--danger, #ef4444); }
.tx-cashout { color: var(--success, #22c55e); }
.tx-profit {
  font-size: 16px;
  font-weight: 700;
}

/* Status pills */
.status-pill {
  display: inline-block;
  padding: 2px 10px;
  border-radius: 12px;
  font-size: 11px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}
.pill-paid { background: rgba(34,197,94,0.15); color: var(--success, #22c55e); }
.pill-unpaid { background: rgba(239,68,68,0.15); color: var(--danger, #ef4444); }
.pill-partial { background: rgba(245,158,11,0.15); color: var(--warning, #f59e0b); }
.pill-disputed { background: rgba(59,130,246,0.15); color: var(--info, #3b82f6); }
</style>
