<template>
  <div class="login-page">
    <div class="login-card card">
      <div class="login-header">
        <h1 class="login-brand">♠ SmartNote</h1>
        <p class="login-subtitle">Poker Cash Group Tracker</p>
      </div>

      <!-- Success message after registration -->
      <div class="pending-msg" v-if="pendingMsg">
        <div class="pending-icon">⏳</div>
        <h3>Đăng ký thành công!</h3>
        <p>{{ pendingMsg }}</p>
        <button class="btn btn-secondary" @click="pendingMsg = ''; mode = 'login'">Quay lại đăng nhập</button>
      </div>

      <template v-else>
        <div class="tabs" style="margin: 0 auto 24px;">
          <button :class="['tab', { active: mode === 'login' }]" @click="mode = 'login'">Đăng nhập</button>
          <button :class="['tab', { active: mode === 'register' }]" @click="mode = 'register'">Đăng ký</button>
        </div>

        <form @submit.prevent="handleSubmit">
          <div class="form-group" v-if="mode === 'register'">
            <label>Tên hiển thị</label>
            <input class="input" v-model="form.displayName" placeholder="VD: Nguyễn Văn A" />
          </div>
          <div class="form-group">
            <label>Email</label>
            <input class="input" type="email" v-model="form.email" placeholder="admin@poker.com" required />
          </div>
          <div class="form-group">
            <label>Mật khẩu</label>
            <input class="input" type="password" v-model="form.password" placeholder="••••••" required />
          </div>

          <p class="error-msg" v-if="error">{{ error }}</p>

          <button class="btn btn-primary" type="submit" style="width: 100%; margin-top: 16px;" :disabled="loading">
            <span class="spinner" v-if="loading" style="width:16px;height:16px;border-width:2px;"></span>
            {{ loading ? '' : mode === 'login' ? 'Đăng nhập' : 'Tạo tài khoản' }}
          </button>
        </form>
      </template>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { register as apiRegister } from '../api'

const auth = useAuthStore()
const router = useRouter()
const mode = ref('login')
const error = ref('')
const loading = ref(false)
const pendingMsg = ref('')
const form = reactive({ email: '', password: '', displayName: '' })

async function handleSubmit() {
  error.value = ''
  loading.value = true
  try {
    if (mode.value === 'login') {
      await auth.login(form.email, form.password)
      router.push('/')
    } else {
      const { data } = await apiRegister({ email: form.email, password: form.password, displayName: form.displayName || form.email })
      if (data.status === 'PENDING') {
        pendingMsg.value = data.message || 'Vui lòng chờ admin duyệt tài khoản.'
      } else if (data.token) {
        // Fallback: if server returns token (shouldn't happen now)
        auth.token = data.token
        auth.user = data.user
        localStorage.setItem('token', data.token)
        router.push('/')
      }
    }
  } catch (e) {
    const msg = e.response?.data?.message || 'Có lỗi xảy ra. Vui lòng thử lại.'
    if (msg.includes('chờ admin duyệt') || msg.includes('chưa được duyệt')) {
      error.value = '⏳ Tài khoản chưa được admin duyệt. Vui lòng chờ.'
    } else if (msg.includes('bị từ chối')) {
      error.value = '❌ Tài khoản đã bị từ chối.'
    } else {
      error.value = msg
    }
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--bg-primary);
  background-image: radial-gradient(circle at 20% 50%, var(--accent-glow) 0%, transparent 50%),
                    radial-gradient(circle at 80% 50%, rgba(78, 205, 196, 0.1) 0%, transparent 50%);
}
.login-card {
  width: 400px;
  max-width: 90vw;
  padding: 40px;
  animation: slideUp 0.5s ease;
}
.login-header { text-align: center; margin-bottom: 32px; }
.login-brand {
  font-size: 36px;
  font-weight: 700;
  background: var(--accent-gradient);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}
.login-subtitle { color: var(--text-muted); font-size: 14px; margin-top: 8px; }
form { display: flex; flex-direction: column; gap: 16px; }
.error-msg { color: var(--danger); font-size: 13px; text-align: center; }
.pending-msg {
  text-align: center;
  padding: 20px 0;
}
.pending-icon { font-size: 48px; margin-bottom: 16px; }
.pending-msg h3 { color: var(--success); margin-bottom: 8px; }
.pending-msg p { color: var(--text-secondary); font-size: 14px; margin-bottom: 20px; }
</style>
