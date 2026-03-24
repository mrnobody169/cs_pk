import { defineStore } from 'pinia'

export const useAppStore = defineStore('app', {
    state: () => ({
        toasts: []
    }),
    actions: {
        toast(message, type = 'success') {
            const id = Date.now()
            this.toasts.push({ id, message, type })
            setTimeout(() => {
                this.toasts = this.toasts.filter(t => t.id !== id)
            }, 3000)
        }
    }
})
