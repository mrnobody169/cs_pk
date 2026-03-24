<template>
  <div class="page">
    <!-- Header -->
    <div class="page-header">
      <div>
        <router-link to="/" class="back-link">← Về danh sách</router-link>
        <h1 class="page-title">{{ group?.name || 'Nhóm' }}</h1>
      </div>
      <div style="display:flex;gap:12px">
        <router-link :to="`/groups/${groupId}/reports`" class="btn btn-secondary">📊 Báo cáo</router-link>
        <button class="btn btn-primary" @click="openWizard" v-if="isAdmin">+ Phiên mới</button>
      </div>
    </div>

    <!-- Tabs -->
    <div class="tabs">
      <button :class="['tab', { active: tab === 'sessions' }]" @click="tab = 'sessions'">Phiên chơi</button>
      <button :class="['tab', { active: tab === 'members' }]" @click="tab = 'members'">Thành viên</button>
    </div>

    <div class="loading-center" v-if="loading"><div class="spinner"></div></div>

    <!-- Sessions Tab -->
    <div v-else-if="tab === 'sessions'">
      <div style="display:flex;gap:12px;margin-bottom:20px;flex-wrap:wrap">
        <div class="form-group">
          <label>Từ ngày</label>
          <input class="input" type="date" v-model="dateFrom" @change="loadSessions" style="width:170px" />
        </div>
        <div class="form-group">
          <label>Đến ngày</label>
          <input class="input" type="date" v-model="dateTo" @change="loadSessions" style="width:170px" />
        </div>
      </div>

      <div v-if="sessions.length === 0" class="empty-state"><p>Không có phiên nào trong khoảng thời gian này.</p></div>

      <div class="table-wrap" v-else>
        <table>
          <thead>
            <tr>
              <th>Ngày</th>
              <th>Trạng thái</th>
              <th>Tạo bởi</th>
              <th></th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="s in sessions" :key="s.id">
              <td><strong>{{ formatDate(s.sessionDate) }}</strong></td>
              <td>
                <span :class="['badge', s.dataLocked ? 'badge-danger' : 'badge-success']">
                  {{ s.dataLocked ? '🔒 Đã khóa' : '🔓 Mở' }}
                </span>
              </td>
              <td class="text-muted">{{ s.createdBy }}</td>
              <td>
                <router-link :to="`/sessions/${s.id}`" class="btn btn-sm btn-secondary">Xem chi tiết →</router-link>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- Members Tab -->
    <div v-else-if="tab === 'members'">
      <div style="display:flex;justify-content:flex-end;margin-bottom:16px" v-if="isAdmin">
        <button class="btn btn-primary btn-sm" @click="openAddMember">+ Thêm thành viên</button>
      </div>
      <div class="table-wrap" v-if="members.length">
        <table>
          <thead><tr><th>Tên</th><th>Email</th><th>Vai trò</th></tr></thead>
          <tbody>
            <tr v-for="m in members" :key="m.userId">
              <td><strong>{{ m.displayName || '—' }}</strong></td>
              <td class="text-muted">{{ m.email || '—' }}</td>
              <td>
                <span :class="['badge', m.role === 'ADMIN' ? 'badge-warning' : 'badge-info']">{{ m.role }}</span>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
      <div v-else class="empty-state"><p>Chưa có thành viên.</p></div>
    </div>

    <!-- ===== SESSION CREATION WIZARD ===== -->
    <div class="modal-overlay" v-if="wizardOpen" @click.self="wizardOpen = false">
      <div class="modal wizard-modal">

        <!-- Step indicator -->
        <div class="step-indicator">
          <span :class="['step-dot', { active: wizardStep === 1 }]">1</span>
          <span class="step-line"></span>
          <span :class="['step-dot', { active: wizardStep === 2 }]">2</span>
        </div>

        <!-- STEP 1: Date + Members -->
        <div v-if="wizardStep === 1">
          <h2 class="modal-title">Tạo phiên mới</h2>

          <div class="form-group">
            <label>Ngày chơi</label>
            <input class="input" type="date" v-model="wizardDate" />
          </div>

          <div class="form-group">
            <label>Chọn thành viên</label>
            <input class="input" type="text" v-model="wizardSearch" placeholder="🔍 Tìm theo tên / email..." style="margin-bottom:8px" />
          </div>

          <!-- Select all -->
          <label class="select-all-row" @click="toggleSelectAll">
            <input type="checkbox" :checked="allSelected" @click.stop="toggleSelectAll" />
            <span>Chọn tất cả ({{ members.length }})</span>
          </label>

          <div class="member-checklist">
            <label v-for="m in filteredMembers" :key="m.userId" class="member-check-item"
                   :class="{ checked: wizardSelected.has(m.userId) }">
              <input type="checkbox" :checked="wizardSelected.has(m.userId)" @change="toggleMember(m.userId)" />
              <div class="member-check-info">
                <strong>{{ m.displayName || m.email }}</strong>
                <span class="text-muted">{{ m.email }}</span>
              </div>
            </label>
            <div v-if="filteredMembers.length === 0" class="empty-state" style="padding:16px">
              <p>{{ wizardSearch ? 'Không tìm thấy' : 'Chưa có thành viên' }}</p>
            </div>
          </div>

          <div class="modal-actions">
            <button type="button" class="btn btn-secondary" @click="wizardOpen = false">Hủy</button>
            <button type="button" class="btn btn-primary" @click="wizardStep = 2" :disabled="wizardSelected.size === 0">
              Tiếp theo → ({{ wizardSelected.size }})
            </button>
          </div>
        </div>

        <!-- STEP 2: Buy-in / Cash-out Table -->
        <div v-if="wizardStep === 2">
          <h2 class="modal-title">Nhập Buy-in / Cash-out</h2>
          <p class="text-muted" style="margin-bottom:16px">📅 {{ formatDate(wizardDate) }} · {{ selectedMembers.length }} thành viên</p>

          <div class="entry-table-wrap">
            <table class="entry-table">
              <thead>
                <tr>
                  <th>Thành viên</th>
                  <th style="width:140px">Buy-in</th>
                  <th style="width:140px">Cash-out</th>
                  <th style="width:100px;text-align:right">Profit</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="(m, i) in selectedMembers" :key="m.userId">
                  <td><strong>{{ m.displayName || m.email }}</strong></td>
                  <td>
                    <input class="input entry-input" type="number" min="0"
                           v-model.number="wizardEntries[m.userId].buyIn"
                           :ref="el => { if (i === 0) firstInput = el }"
                           @keydown.enter.prevent="focusNext($event)" />
                  </td>
                  <td>
                    <input class="input entry-input" type="number" min="0"
                           v-model.number="wizardEntries[m.userId].cashOut"
                           @keydown.enter.prevent="focusNext($event)" />
                  </td>
                  <td style="text-align:right" :class="['money', diffClass(wizardEntries[m.userId].cashOut - wizardEntries[m.userId].buyIn)]">
                    <strong>{{ formatMoney(wizardEntries[m.userId].cashOut - wizardEntries[m.userId].buyIn) }}</strong>
                  </td>
                </tr>
              </tbody>
              <tfoot>
                <tr>
                  <td><strong>Tổng</strong></td>
                  <td class="money"><strong>{{ formatMoney(totalWizBuyIn) }}</strong></td>
                  <td class="money"><strong>{{ formatMoney(totalWizCashOut) }}</strong></td>
                  <td style="text-align:right" :class="['money', diffClass(totalWizCashOut - totalWizBuyIn)]">
                    <strong>{{ formatMoney(totalWizCashOut - totalWizBuyIn) }}</strong>
                  </td>
                </tr>
              </tfoot>
            </table>
          </div>

          <div class="modal-actions">
            <button type="button" class="btn btn-secondary" @click="wizardStep = 1">← Quay lại</button>
            <button type="button" class="btn btn-primary" @click="handleCreateSession" :disabled="wizardSubmitting">
              <span class="spinner" v-if="wizardSubmitting" style="width:16px;height:16px;border-width:2px"></span>
              {{ wizardSubmitting ? '' : '🚀 Tạo phiên' }}
            </button>
          </div>
        </div>

      </div>
    </div>

    <!-- Add Member Modal -->
    <div class="modal-overlay" v-if="showAddMember" @click.self="showAddMember = false">
      <div class="modal" style="max-width:520px">
        <h2 class="modal-title">Thêm thành viên</h2>
        <div class="form-group">
          <label>Tìm kiếm</label>
          <input class="input" type="text" v-model="memberSearchAdd" placeholder="Tìm theo email hoặc tên..." />
        </div>
        <div class="member-checklist">
          <div v-if="filteredAvailableUsers.length === 0" class="empty-state" style="padding:16px">
            <p>{{ memberSearchAdd ? 'Không tìm thấy' : 'Không có user nào để thêm' }}</p>
          </div>
          <label class="member-check-item" v-for="u in filteredAvailableUsers" :key="u.id"
                 :class="{ checked: addMemberIds.has(u.id) }">
            <input type="checkbox" :checked="addMemberIds.has(u.id)" @change="toggleAddUser(u.id)" />
            <div class="member-check-info">
              <strong>{{ u.displayName || u.email }}</strong>
              <span class="text-muted">{{ u.email }}</span>
            </div>
          </label>
        </div>
        <div class="form-group" style="margin-top:12px">
          <label>Vai trò</label>
          <select class="select" v-model="addMemberRole">
            <option value="MEMBER">MEMBER</option>
            <option value="ADMIN">ADMIN</option>
          </select>
        </div>
        <div v-if="addMemberIds.size" style="font-size:13px;color:var(--text-muted);margin-bottom:8px">
          Đã chọn {{ addMemberIds.size }} thành viên
        </div>
        <div class="modal-actions">
          <button type="button" class="btn btn-secondary" @click="showAddMember = false">Hủy</button>
          <button type="button" class="btn btn-primary" @click="handleAddMembers" :disabled="!addMemberIds.size || addingMembers">
            {{ addingMembers ? 'Đang thêm...' : 'Thêm' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getGroups, getSessions, getMembers, addMember, createSession, getApprovedUsers, bulkCreateEntries } from '../api'
import { useAppStore } from '../stores/app'
import { useAuthStore } from '../stores/auth'

const route = useRoute()
const router = useRouter()
const app = useAppStore()
const auth = useAuthStore()
const groupId = computed(() => route.params.id)
const isAdmin = computed(() => auth.user?.isAdmin)

const group = ref(null)
const tab = ref('sessions')
const loading = ref(true)
const sessions = ref([])
const members = ref([])

const today = new Date()
const dateFrom = ref(new Date(today.getFullYear(), today.getMonth(), 1).toISOString().split('T')[0])
const dateTo = ref(today.toISOString().split('T')[0])

// ===== Wizard state =====
const wizardOpen = ref(false)
const wizardStep = ref(1)
const wizardDate = ref(today.toISOString().split('T')[0])
const wizardSearch = ref('')
const wizardSelected = ref(new Set())
const wizardEntries = reactive({})
const wizardSubmitting = ref(false)
const firstInput = ref(null)

const filteredMembers = computed(() => {
  if (!wizardSearch.value.trim()) return members.value
  const q = wizardSearch.value.toLowerCase()
  return members.value.filter(m =>
    (m.displayName && m.displayName.toLowerCase().includes(q)) ||
    (m.email && m.email.toLowerCase().includes(q))
  )
})

const allSelected = computed(() => members.value.length > 0 && wizardSelected.value.size === members.value.length)

const selectedMembers = computed(() => members.value.filter(m => wizardSelected.value.has(m.userId)))

const totalWizBuyIn = computed(() => selectedMembers.value.reduce((s, m) => s + (wizardEntries[m.userId]?.buyIn || 0), 0))
const totalWizCashOut = computed(() => selectedMembers.value.reduce((s, m) => s + (wizardEntries[m.userId]?.cashOut || 0), 0))

function openWizard() {
  wizardStep.value = 1
  wizardDate.value = today.toISOString().split('T')[0]
  wizardSearch.value = ''
  wizardSelected.value = new Set()
  // Pre-select all members
  members.value.forEach(m => wizardSelected.value.add(m.userId))
  // Init entries
  members.value.forEach(m => {
    wizardEntries[m.userId] = { buyIn: 0, cashOut: 0 }
  })
  wizardOpen.value = true
}

function toggleMember(userId) {
  const s = new Set(wizardSelected.value)
  if (s.has(userId)) s.delete(userId)
  else {
    s.add(userId)
    if (!wizardEntries[userId]) wizardEntries[userId] = { buyIn: 0, cashOut: 0 }
  }
  wizardSelected.value = s
}

function toggleSelectAll() {
  if (allSelected.value) {
    wizardSelected.value = new Set()
  } else {
    const s = new Set()
    members.value.forEach(m => {
      s.add(m.userId)
      if (!wizardEntries[m.userId]) wizardEntries[m.userId] = { buyIn: 0, cashOut: 0 }
    })
    wizardSelected.value = s
  }
}

function focusNext(e) {
  const inputs = Array.from(document.querySelectorAll('.entry-input'))
  const idx = inputs.indexOf(e.target)
  if (idx >= 0 && idx < inputs.length - 1) {
    inputs[idx + 1].focus()
    inputs[idx + 1].select()
  }
}

async function handleCreateSession() {
  wizardSubmitting.value = true
  try {
    // Step 1: Create session
    const { data: session } = await createSession(groupId.value, wizardDate.value)

    // Step 2: Bulk create entries
    const entries = selectedMembers.value
      .filter(m => wizardEntries[m.userId])
      .map(m => ({
        memberUserId: m.userId,
        buyInTotal: wizardEntries[m.userId].buyIn || 0,
        cashOutTotal: wizardEntries[m.userId].cashOut || 0
      }))

    if (entries.length > 0) {
      await bulkCreateEntries(session.id, entries)
    }

    wizardOpen.value = false
    app.toast(`Tạo phiên thành công với ${entries.length} entries!`)
    router.push(`/sessions/${session.id}`)
  } catch (e) {
    app.toast(e.response?.data?.message || 'Lỗi tạo phiên', 'error')
  } finally {
    wizardSubmitting.value = false
  }
}

// ===== Add Member =====
const showAddMember = ref(false)
const allApprovedUsers = ref([])
const memberSearchAdd = ref('')
const addMemberIds = ref(new Set())
const addMemberRole = ref('MEMBER')
const addingMembers = ref(false)

const filteredAvailableUsers = computed(() => {
  const existingIds = new Set(members.value.map(m => m.userId))
  const available = allApprovedUsers.value.filter(u => !existingIds.has(u.id))
  if (!memberSearchAdd.value.trim()) return available
  const q = memberSearchAdd.value.toLowerCase()
  return available.filter(u =>
    (u.email && u.email.toLowerCase().includes(q)) ||
    (u.displayName && u.displayName.toLowerCase().includes(q))
  )
})

async function openAddMember() {
  showAddMember.value = true
  addMemberIds.value = new Set()
  memberSearchAdd.value = ''
  try {
    const { data } = await getApprovedUsers()
    allApprovedUsers.value = data
  } catch (e) {
    app.toast('Lỗi load danh sách users', 'error')
  }
}

function toggleAddUser(userId) {
  const s = new Set(addMemberIds.value)
  if (s.has(userId)) s.delete(userId)
  else s.add(userId)
  addMemberIds.value = s
}

async function handleAddMembers() {
  addingMembers.value = true
  let added = 0, errorCount = 0
  for (const userId of addMemberIds.value) {
    const user = allApprovedUsers.value.find(u => u.id === userId)
    if (!user) continue
    try {
      await addMember(groupId.value, { email: user.email, role: addMemberRole.value })
      added++
    } catch (e) { errorCount++ }
  }
  const { data } = await getMembers(groupId.value)
  members.value = data
  showAddMember.value = false
  addingMembers.value = false
  if (added > 0) app.toast(`Đã thêm ${added} thành viên!`)
  if (errorCount > 0) app.toast(`${errorCount} lỗi khi thêm`, 'error')
}

// ===== Init =====
onMounted(async () => {
  try {
    const [grps, sess, mems] = await Promise.all([
      getGroups(),
      getSessions(groupId.value, dateFrom.value, dateTo.value),
      getMembers(groupId.value)
    ])
    group.value = grps.data.find(g => g.id == groupId.value) || { name: 'Nhóm' }
    sessions.value = sess.data
    members.value = mems.data
  } finally {
    loading.value = false
  }
})

async function loadSessions() {
  try {
    const { data } = await getSessions(groupId.value, dateFrom.value, dateTo.value)
    sessions.value = data
  } catch (e) {
    app.toast('Lỗi load sessions', 'error')
  }
}

function formatDate(d) {
  if (!d) return ''
  return new Date(d + 'T00:00:00').toLocaleDateString('vi-VN', { weekday: 'short', day: '2-digit', month: '2-digit', year: 'numeric' })
}
function formatMoney(v) {
  if (v == null) return '—'
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND', maximumFractionDigits: 0 }).format(v)
}
function diffClass(v) { return v > 0 ? 'positive' : v < 0 ? 'negative' : 'zero' }
</script>

<style scoped>
.back-link { font-size: 13px; color: var(--text-muted); display: block; margin-bottom: 8px; text-decoration: none; }
.back-link:hover { color: var(--accent); }
.text-muted { color: var(--text-muted); }

/* Wizard */
.wizard-modal { max-width: 600px; }

.step-indicator {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0;
  margin-bottom: 20px;
}
.step-dot {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  font-weight: 700;
  background: var(--bg-secondary);
  color: var(--text-muted);
  border: 2px solid var(--border);
  transition: all 0.2s;
}
.step-dot.active {
  background: var(--accent);
  color: #fff;
  border-color: var(--accent);
}
.step-line {
  width: 40px;
  height: 2px;
  background: var(--border);
}

.select-all-row {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 12px;
  font-size: 14px;
  cursor: pointer;
  border-bottom: 1px solid var(--border);
  color: var(--text-secondary);
}
.select-all-row input { accent-color: var(--accent); width: 18px; height: 18px; cursor: pointer; }

.member-checklist {
  max-height: 250px;
  overflow-y: auto;
  border: 1px solid var(--border);
  border-radius: 8px;
  background: var(--bg-secondary);
  margin-bottom: 16px;
}
.member-check-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 14px;
  cursor: pointer;
  border-bottom: 1px solid var(--border);
  transition: background 0.15s;
}
.member-check-item:last-child { border-bottom: none; }
.member-check-item:hover, .member-check-item.checked {
  background: rgba(var(--accent-rgb, 99, 102, 241), 0.08);
}
.member-check-item input[type="checkbox"] { width: 18px; height: 18px; accent-color: var(--accent); cursor: pointer; }
.member-check-info { display: flex; flex-direction: column; gap: 2px; font-size: 14px; }
.member-check-info span { font-size: 12px; }

/* Entry table */
.entry-table-wrap {
  max-height: 350px;
  overflow-y: auto;
  margin-bottom: 16px;
  border: 1px solid var(--border);
  border-radius: 8px;
}
.entry-table {
  width: 100%;
  border-collapse: collapse;
}
.entry-table th, .entry-table td {
  padding: 10px 12px;
  text-align: left;
  border-bottom: 1px solid var(--border);
}
.entry-table thead th {
  background: var(--bg-secondary);
  font-size: 12px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  color: var(--text-muted);
  position: sticky;
  top: 0;
  z-index: 1;
}
.entry-table tfoot td {
  background: var(--bg-secondary);
  position: sticky;
  bottom: 0;
}
.entry-input {
  width: 100%;
  padding: 8px !important;
  font-size: 15px !important;
  text-align: right;
}
</style>
