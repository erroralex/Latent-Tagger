import { useBackendStore } from '../stores/useBackendStore'

async function fetchApi(path, options = {}) {
    const backend = useBackendStore()
    if (!backend.ready) {
        throw new Error('Backend not ready')
    }

    const url = `http://127.0.0.1:${backend.port}${path}`
    const headers = {
        ...options.headers,
        'Authorization': `Bearer ${backend.token}`,
        'Content-Type': 'application/json'
    }

    const response = await fetch(url, { ...options, headers });

    if (!response.ok) {
        const errorBody = await response.json().catch(() => ({}))
        const error = new Error(errorBody.error || `HTTP error! status: ${response.status}`);
        error.response = response;
        throw error;
    }

    return response.json()
}

export async function searchTags(q, limit = 20) {
    const data = await fetchApi(`/api/tags/search?q=${q}&limit=${limit}`)
    return data.results
}

export async function validateTags(tags) {
    const data = await fetchApi('/api/tags/validate', {
        method: 'POST',
        body: JSON.stringify({ tags })
    })
    return data.results
}

export async function getImplications(tag) {
    const data = await fetchApi(`/api/tags/implications?tag=${tag}`)
    return data.implications
}

export async function translate(naturalText, modelFamily) {
    return fetchApi('/api/translate', {
        method: 'POST',
        body: JSON.stringify({ naturalText, modelFamily })
    })
}

export async function getPresets() {
    return fetchApi('/api/presets')
}

export async function getHistory(limit = 50) {
    const data = await fetchApi(`/api/history?limit=${limit}`)
    return data.entries
}

export async function saveHistory(tags, nlInput, model) {
    return fetchApi('/api/history', {
        method: 'POST',
        body: JSON.stringify({ tags, nlInput, model })
    })
}

export async function getModelStatus() {
    return fetchApi('/api/model/status');
}

export async function downloadModel() {
    return fetchApi('/api/model/download', { method: 'POST' });
}

export async function getModelPath() {
    const data = await fetchApi('/api/model/path');
    return data.path;
}
