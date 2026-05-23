import axios from 'axios';
import { env } from '../config/env';

export const apiClient = axios.create({
    baseURL: env.apiBaseUrl,
    headers: {
        'Content-Type': 'application/json',
    },
});

apiClient.interceptors.request.use((config) => {
    const token = localStorage.getItem('node_health_tracker_token');

    if (token) {
        config.headers.Authorization = `Bearer ${token}`;
    }

    return config;
});