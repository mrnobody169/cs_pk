import axios from 'axios'

const api = axios.create({ baseURL: '/api' })

// JWT interceptor
api.interceptors.request.use(config => {
    const token = localStorage.getItem('token')
    if (token) config.headers.Authorization = `Bearer ${token}`
    return config
})

api.interceptors.response.use(
    res => res,
    err => {
        if (err.response?.status === 401) {
            localStorage.removeItem('token')
            window.location.href = '/login'
        }
        return Promise.reject(err)
    }
)

// Auth
export const login = (data) => api.post('/auth/login', data)
export const register = (data) => api.post('/auth/register', data)
export const getMe = () => api.get('/auth/me')

// Groups
export const getGroups = () => api.get('/groups')
export const createGroup = (name) => api.post('/groups', { name })
export const getMembers = (groupId) => api.get(`/groups/${groupId}/members`)
export const addMember = (groupId, data) => api.post(`/groups/${groupId}/members`, data)

// Players
export const getPlayers = (groupId) => api.get(`/groups/${groupId}/players`)
export const createPlayer = (groupId, data) => api.post(`/groups/${groupId}/players`, data)
export const updatePlayer = (playerId, data) => api.patch(`/players/${playerId}`, data)
export const deactivatePlayer = (playerId) => api.post(`/players/${playerId}/deactivate`)

// Sessions
export const getSessions = (groupId, from, to) => api.get(`/groups/${groupId}/sessions`, { params: { from, to } })
export const createSession = (groupId, sessionDate) => api.post(`/groups/${groupId}/sessions`, { sessionDate })
export const getSession = (sessionId) => api.get(`/sessions/${sessionId}`)
export const lockSession = (sessionId, locked) => api.post(`/sessions/${sessionId}/lock`, { locked })

// Entries
export const getEntries = (sessionId) => api.get(`/sessions/${sessionId}/entries`)
export const createEntry = (sessionId, data) => api.post(`/sessions/${sessionId}/entries`, data)
export const updateEntry = (entryId, data) => api.patch(`/entries/${entryId}`, data)
export const deleteEntry = (entryId) => api.delete(`/entries/${entryId}`)
export const bulkCreateEntries = (sessionId, entries) => api.post(`/sessions/${sessionId}/entries/bulk`, { entries })

// Settlement
export const settleEntry = (entryId, data) => api.post(`/entries/${entryId}/settlement`, data)
export const bulkSettle = (sessionId, data) => api.post(`/sessions/${sessionId}/settlement/bulk`, data)

// Reports
export const getReportSummary = (groupId, from, to) => api.get(`/groups/${groupId}/reports/summary`, { params: { from, to } })
export const getReportByPlayer = (groupId, from, to) => api.get(`/groups/${groupId}/reports/by-player`, { params: { from, to } })
export const getReportByDay = (groupId, from, to) => api.get(`/groups/${groupId}/reports/by-day`, { params: { from, to } })

// Dashboard
export const getDashboardProfit = (from, to, groupId) => api.get('/dashboard/member-profit', { params: { from, to, groupId } })
export const getDashboardTransactions = (from, to, groupId) => api.get('/dashboard/transactions', { params: { from, to, groupId } })

// Admin
export const getPendingUsers = () => api.get('/admin/pending-users')
export const getAllUsers = () => api.get('/admin/users')
export const approveUser = (userId) => api.post(`/admin/users/${userId}/approve`)
export const rejectUser = (userId) => api.post(`/admin/users/${userId}/reject`)
export const getApprovedUsers = () => api.get('/admin/approved-users')
export const batchRegister = (data) => api.post('/admin/batch-register', data)

export default api
