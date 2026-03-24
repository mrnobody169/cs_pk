import { defineStore } from 'pinia'
import { login as apiLogin, register as apiRegister, getMe } from '../api'

export const useAuthStore = defineStore('auth', {
    state: () => ({
        user: null,
        token: localStorage.getItem('token') || null,
        loading: false
    }),
    getters: {
        isLoggedIn: (state) => !!state.token,
        displayName: (state) => state.user?.displayName || state.user?.email || ''
    },
    actions: {
        async login(email, password) {
            this.loading = true
            try {
                const { data } = await apiLogin({ email, password })
                this.token = data.token
                this.user = data.user
                localStorage.setItem('token', data.token)
                return data
            } finally {
                this.loading = false
            }
        },
        async register(email, password, displayName) {
            this.loading = true
            try {
                const { data } = await apiRegister({ email, password, displayName })
                this.token = data.token
                this.user = data.user
                localStorage.setItem('token', data.token)
                return data
            } finally {
                this.loading = false
            }
        },
        async fetchMe() {
            if (!this.token) return
            try {
                const { data } = await getMe()
                this.user = data
            } catch {
                this.logout()
            }
        },
        logout() {
            this.token = null
            this.user = null
            localStorage.removeItem('token')
        }
    }
})
