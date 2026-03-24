import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const routes = [
    { path: '/login', name: 'Login', component: () => import('../views/LoginView.vue'), meta: { public: true } },
    { path: '/', name: 'Home', component: () => import('../views/DashboardView.vue') },
    { path: '/profit', name: 'ProfitDashboard', component: () => import('../views/ProfitDashboardView.vue') },
    { path: '/transactions', name: 'Transactions', component: () => import('../views/TransactionView.vue') },
    { path: '/groups/:id', name: 'Group', component: () => import('../views/GroupView.vue') },
    { path: '/sessions/:id', name: 'Session', component: () => import('../views/SessionView.vue') },
    { path: '/groups/:id/reports', name: 'Reports', component: () => import('../views/ReportView.vue') },
    { path: '/admin', name: 'Admin', component: () => import('../views/AdminView.vue') },
]

const router = createRouter({
    history: createWebHistory(),
    routes
})

router.beforeEach((to) => {
    const auth = useAuthStore()
    if (!to.meta.public && !auth.isLoggedIn) {
        return { name: 'Login' }
    }
})

export default router
