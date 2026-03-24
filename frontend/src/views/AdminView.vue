<template>
  <div class="page">
    <div class="page-header">
      <h1 class="page-title">🔑 Quản lý tài khoản</h1>
      <button class="btn btn-primary" @click="showBatch = true">+ Tạo nhiều tài khoản</button>
    </div>

    <!-- Tabs -->
    <div class="tabs">
      <button :class="['tab', { active: tab === 'pending' }]" @click="tab = 'pending'">
        Chờ duyệt <span class="count-badge" v-if="pendingCount">{{ pendingCount }}</span>
      </button>
      <button :class="['tab', { active: tab === 'all' }]" @click="tab = 'all'; loadAll()">Tất cả</button>
    </div>

    <div class="loading-center" v-if="loading"><div class="spinner"></div></div>

    <!-- Pending Users -->
    <template v-if="!loading && tab === 'pending'">
      <div v-if="pendingUsers.length === 0" class="empty-state">
        <p>Không có tài khoản nào đang chờ duyệt 👍</p>
      </div>
      <div class="table-wrap" v-else>
        <table>
          <thead>
            <tr>
              <th>Email</th>
              <th>Tên hiển thị</th>
              <th>Đăng ký lúc</th>
              <th style="text-align:right">Hành động</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="u in pendingUsers" :key="u.id">
              <td><strong>{{ u.email }}</strong></td>
              <td>{{ u.displayName }}</td>
              <td class="text-muted">{{ formatDate(u.createdAt) }}</td>
              <td style="text-align:right">
                <div style="display:flex;gap:8px;justify-content:flex-end">
                  <button class="btn btn-sm btn-primary" @click="handleApprove(u)" :disabled="u.processing">✅ Duyệt</button>
                  <button class="btn btn-sm btn-danger" @click="handleReject(u)" :disabled="u.processing">❌ Từ chối</button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </template>

    <!-- All Users -->
    <template v-if="!loading && tab === 'all'">
      <div v-if="allUsers.length === 0" class="empty-state"><p>Không có user nào.</p></div>
      <div class="table-wrap" v-else>
        <table>
          <thead>
            <tr>
              <th>Email</th>
              <th>Tên hiển thị</th>
              <th>Trạng thái</th>
              <th>Admin</th>
              <th>Đăng ký lúc</th>
              <th style="text-align:right">Hành động</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="u in allUsers" :key="u.id">
              <td><strong>{{ u.email }}</strong></td>
              <td>{{ u.displayName }}</td>
              <td>
                <span :class="['badge', statusBadge(u.status)]">{{ statusLabel(u.status) }}</span>
              </td>
              <td>
                <span class="badge badge-warning" v-if="u.isAdmin">ADMIN</span>
                <span class="text-muted" v-else>—</span>
              </td>
              <td class="text-muted">{{ formatDate(u.createdAt) }}</td>
              <td style="text-align:right">
                <div style="display:flex;gap:8px;justify-content:flex-end">
                  <button class="btn btn-sm btn-primary" v-if="u.status !== 'APPROVED'" @click="handleApprove(u)" :disabled="u.processing">Duyệt</button>
                  <button class="btn btn-sm btn-danger" v-if="u.status !== 'REJECTED'" @click="handleReject(u)" :disabled="u.processing">Từ chối</button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </template>

    <!-- Batch Create Modal -->
    <div class="modal-overlay" v-if="showBatch" @click.self="showBatch = false">
      <div class="modal" style="max-width:560px">
        <h2 class="modal-title">Tạo nhiều tài khoản</h2>

        <!-- Password mode toggle -->
        <div class="form-group">
          <label>Chế độ mật khẩu</label>
          <div class="tabs" style="margin:0">
            <button :class="['tab', { active: batchMode === 'shared' }]" @click="batchMode = 'shared'" type="button">Chung 1 password</button>
            <button :class="['tab', { active: batchMode === 'individual' }]" @click="batchMode = 'individual'" type="button">Mỗi người riêng</button>
          </div>
        </div>

        <div class="form-group" v-if="batchMode === 'shared'">
          <label>Mật khẩu chung</label>
          <input class="input" type="text" v-model="sharedPassword" placeholder="VD: poker2026" required />
        </div>

        <div class="form-group">
          <label>Danh sách email (mỗi dòng 1 email{{ batchMode === 'individual' ? ', password, tên (ngăn cách bằng dấu phẩy)' : '' }})</label>
          <textarea class="input" v-model="batchText" rows="8"
            :placeholder="batchMode === 'shared'
              ? 'user1@email.com\nuser2@email.com\nuser3@email.com'
              : 'user1@email.com,pass123,Tên User 1\nuser2@email.com,pass456,Tên User 2'"
            style="resize:vertical;font-family:monospace;font-size:13px"></textarea>
        </div>

        <!-- Preview -->
        <div v-if="parsedUsers.length" style="margin-bottom:16px">
          <p style="color:var(--text-muted);font-size:13px">{{ parsedUsers.length }} tài khoản sẽ được tạo</p>
        </div>

        <!-- Results -->
        <div v-if="batchResult" style="margin-bottom:16px">
          <div v-if="batchResult.createdCount" style="color:var(--success);font-size:14px;margin-bottom:6px">
            ✅ Đã tạo {{ batchResult.createdCount }} tài khoản
          </div>
          <div v-if="batchResult.errorCount" style="margin-top:8px">
            <p style="color:var(--danger);font-size:14px">❌ {{ batchResult.errorCount }} lỗi:</p>
            <ul style="margin:4px 0 0 16px;font-size:13px;color:var(--text-muted)">
              <li v-for="(err, i) in batchResult.errors" :key="i">{{ err }}</li>
            </ul>
          </div>
        </div>

        <div class="modal-actions">
          <button type="button" class="btn btn-secondary" @click="closeBatch">Đóng</button>
          <button type="button" class="btn btn-primary" @click="handleBatch" :disabled="!parsedUsers.length || batchSubmitting">
            <span class="spinner" v-if="batchSubmitting" style="width:16px;height:16px;border-width:2px"></span>
            {{ batchSubmitting ? '' : 'Tạo tài khoản' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { getPendingUsers, getAllUsers, approveUser, rejectUser, batchRegister } from '../api'
import { useAppStore } from '../stores/app'

const app = useAppStore()
const loading = ref(true)
const tab = ref('pending')
const pendingUsers = ref([])
const allUsers = ref([])

// Batch
const showBatch = ref(false)
const batchMode = ref('shared') // 'shared' or 'individual'
const sharedPassword = ref('')
const batchText = ref('')
const batchSubmitting = ref(false)
const batchResult = ref(null)

const pendingCount = computed(() => pendingUsers.value.length)

const parsedUsers = computed(() => {
  if (!batchText.value.trim()) return []
  return batchText.value.trim().split('\n').filter(l => l.trim()).map(line => {
    const parts = line.split(',').map(s => s.trim())
    return {
      email: parts[0] || '',
      password: batchMode.value === 'individual' ? (parts[1] || '') : undefined,
      displayName: batchMode.value === 'individual' ? (parts[2] || parts[0]) : (parts[1] || parts[0])
    }
  }).filter(u => u.email.includes('@'))
})

onMounted(async () => {
  try {
    const { data } = await getPendingUsers()
    pendingUsers.value = data.map(u => ({ ...u, processing: false }))
  } finally {
    loading.value = false
  }
})

async function loadAll() {
  loading.value = true
  try {
    const { data } = await getAllUsers()
    allUsers.value = data.map(u => ({ ...u, processing: false }))
  } finally {
    loading.value = false
  }
}

async function handleApprove(user) {
  user.processing = true
  try {
    await approveUser(user.id)
    user.status = 'APPROVED'
    pendingUsers.value = pendingUsers.value.filter(u => u.id !== user.id)
    app.toast(`Đã duyệt: ${user.email}`)
  } catch (e) {
    app.toast(e.response?.data?.message || 'Lỗi duyệt', 'error')
  } finally {
    user.processing = false
  }
}

async function handleReject(user) {
  if (!confirm(`Từ chối tài khoản ${user.email}?`)) return
  user.processing = true
  try {
    await rejectUser(user.id)
    user.status = 'REJECTED'
    pendingUsers.value = pendingUsers.value.filter(u => u.id !== user.id)
    app.toast(`Đã từ chối: ${user.email}`)
  } catch (e) {
    app.toast(e.response?.data?.message || 'Lỗi từ chối', 'error')
  } finally {
    user.processing = false
  }
}

async function handleBatch() {
  batchSubmitting.value = true
  batchResult.value = null
  try {
    const payload = {
      users: parsedUsers.value,
      sharedPassword: batchMode.value === 'shared' ? sharedPassword.value : null
    }
    const { data } = await batchRegister(payload)
    batchResult.value = data
    if (data.createdCount > 0) {
      app.toast(`Đã tạo ${data.createdCount} tài khoản!`)
      batchText.value = ''
    }
  } catch (e) {
    app.toast(e.response?.data?.message || 'Lỗi tạo batch', 'error')
  } finally {
    batchSubmitting.value = false
  }
}

function closeBatch() {
  showBatch.value = false
  batchResult.value = null
  batchText.value = ''
  sharedPassword.value = ''
}

function formatDate(dt) {
  if (!dt) return ''
  return new Date(dt).toLocaleString('vi-VN')
}
function statusBadge(s) {
  return { APPROVED: 'badge-success', PENDING: 'badge-warning', REJECTED: 'badge-danger' }[s] || 'badge-info'
}
function statusLabel(s) {
  return { APPROVED: '✅ Đã duyệt', PENDING: '⏳ Chờ duyệt', REJECTED: '❌ Từ chối' }[s] || s
}
</script>

<style scoped>
.text-muted { color: var(--text-muted); }
.count-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 20px;
  height: 20px;
  padding: 0 6px;
  border-radius: 10px;
  background: var(--danger);
  color: #fff;
  font-size: 11px;
  font-weight: 700;
  margin-left: 6px;
}
</style>
