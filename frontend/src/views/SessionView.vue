<template>
  <div class="page">
    <!-- Header -->
    <div class="page-header">
      <div>
        <a class="back-link" @click.prevent="$router.back()">← Quay lại</a>
        <h1 class="page-title">Phiên {{ formatDate(session?.sessionDate) }}</h1>
      </div>
      <div style="display:flex;gap:12px;align-items:center">
        <span :class="['badge', session?.dataLocked ? 'badge-danger' : 'badge-success']" v-if="session">
          {{ session.dataLocked ? '🔒 Đã khóa' : '🔓 Mở' }}
        </span>
        <template v-if="isAdmin && session">
          <button class="btn btn-sm" :class="session?.dataLocked ? 'btn-secondary' : 'btn-danger'"
                  @click="handleLock">
            {{ session.dataLocked ? 'Mở khóa' : 'Khóa phiên' }}
          </button>
          <button class="btn btn-primary btn-sm" @click="showAddEntry = true" v-if="!session.dataLocked">
            + Thêm entry
          </button>
        </template>
      </div>
    </div>

    <div class="loading-center" v-if="loading"><div class="spinner"></div></div>

    <!-- Stats -->
    <div class="stats-grid" v-if="!loading">
      <div class="stat-card">
        <div class="stat-label">Tổng Buy-in</div>
        <div class="stat-value money">{{ formatMoney(totalBuyIn) }}</div>
      </div>
      <div class="stat-card">
        <div class="stat-label">Tổng Cash-out</div>
        <div class="stat-value money">{{ formatMoney(totalCashOut) }}</div>
      </div>
      <div class="stat-card">
        <div class="stat-label">Chênh lệch</div>
        <div :class="['stat-value', 'money', diffClass(totalCashOut - totalBuyIn)]">
          {{ formatMoney(totalCashOut - totalBuyIn) }}
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-label">Số entries</div>
        <div class="stat-value">{{ entries.length }}</div>
      </div>
    </div>

    <!-- Entries Table -->
    <div class="table-wrap" v-if="!loading && entries.length">
      <table>
        <thead>
          <tr>
            <th>Thành viên</th>
            <th style="text-align:right">Buy-in</th>
            <th style="text-align:right">Cash-out</th>
            <th style="text-align:right">Profit</th>
            <th>Thanh toán</th>
            <th>Ghi chú</th>
            <th v-if="isAdmin && !session?.dataLocked"></th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="e in entries" :key="e.id">
            <td><strong>{{ e.memberName || `User #${e.memberUserId}` }}</strong></td>
            <td style="text-align:right" class="money">{{ formatMoney(e.buyInTotal) }}</td>
            <td style="text-align:right" class="money">{{ formatMoney(e.cashOutTotal) }}</td>
            <td style="text-align:right" :class="['money', diffClass(e.profit)]">{{ formatMoney(e.profit) }}</td>
            <td>
              <span :class="['badge', settleBadge(e.settlementStatus)]">{{ e.settlementStatus }}</span>
            </td>
            <td class="text-muted" style="max-width:150px;overflow:hidden;text-overflow:ellipsis">{{ e.note || '—' }}</td>
            <td v-if="isAdmin && !session?.dataLocked">
              <div style="display:flex;gap:6px">
                <button class="btn-icon" title="Sửa" @click="startEdit(e)">✏️</button>
                <button class="btn-icon" title="Thanh toán" @click="startSettle(e)" v-if="e.settlementStatus !== 'PAID'">💰</button>
                <button class="btn-icon" title="Xóa" @click="handleDelete(e.id)">🗑️</button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <div v-if="!loading && entries.length === 0" class="empty-state">
      <p>Chưa có entry nào.{{ isAdmin ? ' Thêm entry đầu tiên!' : '' }}</p>
    </div>

    <!-- Add/Edit Entry Modal -->
    <div class="modal-overlay" v-if="showAddEntry || editingEntry" @click.self="closeEntryModal">
      <div class="modal">
        <h2 class="modal-title">{{ editingEntry ? 'Sửa entry' : 'Thêm entry' }}</h2>
        <form @submit.prevent="editingEntry ? handleUpdate() : handleCreate()">
          <div class="form-group" v-if="!editingEntry">
            <label>Thành viên</label>
            <select class="select" v-model="entryForm.memberUserId" required>
              <option value="" disabled>-- Chọn thành viên --</option>
              <option v-for="m in members" :key="m.userId" :value="m.userId">{{ m.displayName || m.email }}</option>
            </select>
          </div>
          <div class="form-group">
            <label>Buy-in</label>
            <input class="input" type="number" v-model.number="entryForm.buyInTotal" min="0" required />
          </div>
          <div class="form-group">
            <label>Cash-out</label>
            <input class="input" type="number" v-model.number="entryForm.cashOutTotal" min="0" required />
          </div>
          <div class="form-group">
            <label>Ghi chú</label>
            <input class="input" v-model="entryForm.note" placeholder="Ghi chú (tùy chọn)" />
          </div>
          <div class="modal-actions">
            <button type="button" class="btn btn-secondary" @click="closeEntryModal">Hủy</button>
            <button type="submit" class="btn btn-primary">{{ editingEntry ? 'Cập nhật' : 'Thêm' }}</button>
          </div>
        </form>
      </div>
    </div>

    <!-- Settlement Modal -->
    <div class="modal-overlay" v-if="settlingEntry" @click.self="settlingEntry = null">
      <div class="modal">
        <h2 class="modal-title">Thanh toán — {{ settlingEntry.memberName }}</h2>
        <form @submit.prevent="handleSettle">
          <div class="form-group">
            <label>Trạng thái</label>
            <select class="select" v-model="settleForm.status" required>
              <option value="PAID">PAID</option>
              <option value="PARTIAL">PARTIAL</option>
              <option value="DISPUTED">DISPUTED</option>
              <option value="UNPAID">UNPAID</option>
            </select>
          </div>
          <div class="form-group" v-if="settleForm.status === 'PARTIAL'">
            <label>Số tiền đã thanh toán</label>
            <input class="input" type="number" v-model.number="settleForm.settledAmount" min="0" />
          </div>
          <div class="form-group" v-if="settleForm.status === 'DISPUTED'">
            <label>Ghi chú tranh chấp</label>
            <input class="input" v-model="settleForm.settlementNote" required placeholder="Lý do..." />
          </div>
          <div class="modal-actions">
            <button type="button" class="btn btn-secondary" @click="settlingEntry = null">Hủy</button>
            <button type="submit" class="btn btn-primary">Cập nhật</button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import {
  getSession, getEntries, getMembers, createEntry, updateEntry, deleteEntry,
  settleEntry, lockSession
} from '../api'
import { useAppStore } from '../stores/app'
import { useAuthStore } from '../stores/auth'

const route = useRoute()
const app = useAppStore()
const auth = useAuthStore()
const sessionId = computed(() => route.params.id)
const isAdmin = computed(() => auth.user?.isAdmin)

const session = ref(null)
const entries = ref([])
const members = ref([])
const loading = ref(true)

// Entry form
const showAddEntry = ref(false)
const editingEntry = ref(null)
const entryForm = reactive({ memberUserId: '', buyInTotal: 0, cashOutTotal: 0, note: '' })

// Settlement form
const settlingEntry = ref(null)
const settleForm = reactive({ status: 'PAID', settledAmount: null, settlementNote: '' })

const totalBuyIn = computed(() => entries.value.reduce((s, e) => s + (e.buyInTotal || 0), 0))
const totalCashOut = computed(() => entries.value.reduce((s, e) => s + (e.cashOutTotal || 0), 0))

onMounted(async () => {
  try {
    const [sessRes, entriesRes] = await Promise.all([
      getSession(sessionId.value),
      getEntries(sessionId.value)
    ])
    session.value = sessRes.data
    entries.value = entriesRes.data

    // Load group members for the dropdown
    if (session.value.groupId) {
      const { data } = await getMembers(session.value.groupId)
      members.value = data
    }
  } finally {
    loading.value = false
  }
})

async function handleCreate() {
  try {
    const { data } = await createEntry(sessionId.value, {
      memberUserId: entryForm.memberUserId,
      buyInTotal: entryForm.buyInTotal,
      cashOutTotal: entryForm.cashOutTotal,
      note: entryForm.note || null
    })
    entries.value.push(data)
    closeEntryModal()
    app.toast('Thêm entry thành công!')
  } catch (e) {
    app.toast(e.response?.data?.message || 'Lỗi thêm entry', 'error')
  }
}

function startEdit(entry) {
  editingEntry.value = entry
  entryForm.memberUserId = entry.memberUserId
  entryForm.buyInTotal = entry.buyInTotal
  entryForm.cashOutTotal = entry.cashOutTotal
  entryForm.note = entry.note || ''
}

async function handleUpdate() {
  try {
    const { data } = await updateEntry(editingEntry.value.id, {
      buyInTotal: entryForm.buyInTotal,
      cashOutTotal: entryForm.cashOutTotal,
      note: entryForm.note || null
    })
    const idx = entries.value.findIndex(e => e.id === editingEntry.value.id)
    if (idx >= 0) entries.value[idx] = data
    closeEntryModal()
    app.toast('Cập nhật thành công!')
  } catch (e) {
    app.toast(e.response?.data?.message || 'Lỗi cập nhật', 'error')
  }
}

async function handleDelete(entryId) {
  if (!confirm('Xóa entry này?')) return
  try {
    await deleteEntry(entryId)
    entries.value = entries.value.filter(e => e.id !== entryId)
    app.toast('Đã xóa entry')
  } catch (e) {
    app.toast(e.response?.data?.message || 'Lỗi xóa', 'error')
  }
}

function startSettle(entry) {
  settlingEntry.value = entry
  settleForm.status = 'PAID'
  settleForm.settledAmount = null
  settleForm.settlementNote = ''
}

async function handleSettle() {
  try {
    const { data } = await settleEntry(settlingEntry.value.id, {
      status: settleForm.status,
      settledAmount: settleForm.status === 'PARTIAL' ? settleForm.settledAmount : null,
      settlementNote: settleForm.status === 'DISPUTED' ? settleForm.settlementNote : null
    })
    const idx = entries.value.findIndex(e => e.id === settlingEntry.value.id)
    if (idx >= 0) {
      entries.value[idx].settlementStatus = data.settlementStatus
      entries.value[idx].settledAmount = data.settledAmount
    }
    settlingEntry.value = null
    app.toast('Cập nhật thanh toán!')
  } catch (e) {
    app.toast(e.response?.data?.message || 'Lỗi thanh toán', 'error')
  }
}

async function handleLock() {
  const lock = !session.value.dataLocked
  try {
    const { data } = await lockSession(sessionId.value, lock)
    session.value = data
    app.toast(lock ? 'Đã khóa phiên' : 'Đã mở khóa')
  } catch (e) {
    app.toast(e.response?.data?.message || 'Lỗi lock', 'error')
  }
}

function closeEntryModal() {
  showAddEntry.value = false
  editingEntry.value = null
  entryForm.memberUserId = ''
  entryForm.buyInTotal = 0
  entryForm.cashOutTotal = 0
  entryForm.note = ''
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
function settleBadge(s) {
  const map = { PAID: 'badge-success', UNPAID: 'badge-danger', PARTIAL: 'badge-warning', DISPUTED: 'badge-info' }
  return map[s] || 'badge-info'
}
</script>

<style scoped>
.back-link { font-size: 13px; color: var(--text-muted); display: block; margin-bottom: 8px; text-decoration: none; cursor: pointer; }
.back-link:hover { color: var(--accent); }
.text-muted { color: var(--text-muted); }
</style>
