<template>
  <div class="page">
    <div class="page-header">
      <h1 class="page-title">Nhóm của tôi</h1>
      <button class="btn btn-primary" @click="showCreate = true">+ Tạo nhóm</button>
    </div>

    <div class="loading-center" v-if="loading"><div class="spinner"></div></div>

    <div v-else-if="groups.length === 0" class="empty-state">
      <p>Chưa có nhóm nào. Tạo nhóm đầu tiên ngay!</p>
    </div>

    <div class="groups-grid" v-else>
      <router-link v-for="g in groups" :key="g.id" :to="`/groups/${g.id}`" class="group-card card">
        <div class="group-icon">♠</div>
        <div class="group-info">
          <h3 class="group-name">{{ g.name }}</h3>
          <span class="group-date">{{ formatDate(g.createdAt) }}</span>
        </div>
        <div class="group-arrow">→</div>
      </router-link>
    </div>

    <!-- Create Modal -->
    <div class="modal-overlay" v-if="showCreate" @click.self="showCreate = false">
      <div class="modal">
        <h2 class="modal-title">Tạo nhóm mới</h2>
        <form @submit.prevent="handleCreate">
          <div class="form-group">
            <label>Tên nhóm</label>
            <input class="input" v-model="newName" placeholder="VD: Nhóm Thứ 6" required autofocus />
          </div>
          <div class="modal-actions">
            <button type="button" class="btn btn-secondary" @click="showCreate = false">Hủy</button>
            <button type="submit" class="btn btn-primary">Tạo</button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getGroups, createGroup } from '../api'
import { useAppStore } from '../stores/app'

const app = useAppStore()
const groups = ref([])
const loading = ref(true)
const showCreate = ref(false)
const newName = ref('')

onMounted(async () => {
  try {
    const { data } = await getGroups()
    groups.value = data
  } finally {
    loading.value = false
  }
})

async function handleCreate() {
  if (!newName.value.trim()) return
  try {
    const { data } = await createGroup(newName.value.trim())
    groups.value.push(data)
    showCreate.value = false
    newName.value = ''
    app.toast('Tạo nhóm thành công!')
  } catch (e) {
    app.toast(e.response?.data?.message || 'Lỗi tạo nhóm', 'error')
  }
}

function formatDate(dt) {
  if (!dt) return ''
  return new Date(dt).toLocaleDateString('vi-VN')
}
</script>

<style scoped>
.groups-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(320px, 1fr)); gap: 16px; }

.group-card {
  display: flex;
  align-items: center;
  gap: 16px;
  text-decoration: none;
  color: inherit;
  cursor: pointer;
}
.group-icon {
  width: 48px; height: 48px;
  border-radius: var(--radius-md);
  background: var(--accent-gradient);
  display: flex; align-items: center; justify-content: center;
  font-size: 22px;
  flex-shrink: 0;
}
.group-info { flex: 1; min-width: 0; }
.group-name { font-size: 16px; font-weight: 600; margin-bottom: 4px; }
.group-date { font-size: 13px; color: var(--text-muted); }
.group-arrow { font-size: 20px; color: var(--text-muted); transition: transform 0.2s; }
.group-card:hover .group-arrow { transform: translateX(4px); color: var(--accent); }
</style>
