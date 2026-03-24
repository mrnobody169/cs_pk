<template>
  <div id="app">
    <!-- Navbar (only when logged in) -->
    <nav class="navbar" v-if="auth.isLoggedIn">
      <router-link to="/" class="navbar-brand">♠ SmartNote</router-link>
      <div class="navbar-links">
        <router-link to="/profit" class="nav-link">📊 Profit</router-link>
        <router-link to="/transactions" class="nav-link">📋 Transactions</router-link>
      </div>
      <div class="navbar-user">
        <router-link v-if="auth.user?.isAdmin" to="/admin" class="admin-link">
          🔑 Quản lý
        </router-link>
        <span>{{ auth.displayName }}</span>
        <button class="btn btn-sm btn-secondary" @click="handleLogout">Đăng xuất</button>
      </div>
    </nav>

    <!-- Page Content -->
    <router-view v-slot="{ Component }">
      <transition name="fade" mode="out-in">
        <component :is="Component" />
      </transition>
    </router-view>

    <!-- Toast notifications -->
    <div class="toast-container">
      <TransitionGroup name="slide">
        <div v-for="t in appStore.toasts" :key="t.id" :class="['toast', `toast-${t.type}`]">
          {{ t.message }}
        </div>
      </TransitionGroup>
    </div>
  </div>
</template>

<script setup>
import { useAuthStore } from './stores/auth'
import { useAppStore } from './stores/app'
import { useRouter } from 'vue-router'

const auth = useAuthStore()
const appStore = useAppStore()
const router = useRouter()

function handleLogout() {
  auth.logout()
  router.push('/login')
}
</script>

<style scoped>
.navbar-links {
  display: flex;
  gap: 4px;
}
.nav-link {
  color: var(--text-secondary);
  font-size: 14px;
  font-weight: 500;
  text-decoration: none;
  padding: 6px 12px;
  border-radius: var(--radius-sm);
  transition: background 0.2s, color 0.2s;
}
.nav-link:hover {
  background: rgba(255,255,255,0.08);
  color: var(--text-primary);
}
.nav-link.router-link-active {
  color: var(--accent);
  background: rgba(var(--accent-rgb, 99, 102, 241), 0.1);
}
.admin-link {
  color: var(--warning);
  font-size: 14px;
  font-weight: 500;
  text-decoration: none;
  padding: 4px 10px;
  border-radius: var(--radius-sm);
  transition: background 0.2s;
}
.admin-link:hover {
  background: var(--warning-bg);
  text-decoration: none;
}
</style>
