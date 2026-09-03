import axiosClient from './axiosClient';
import type { ApiKey, ApiKeyCreateRequest } from '../types/apiKey';

export const getApiKeys = () =>
    axiosClient.get<ApiKey[]>('/api/api-keys');

export const createApiKey = (payload: ApiKeyCreateRequest) =>
    axiosClient.post<ApiKey>('/api/api-keys', payload);

export const revokeApiKey = (id: number) =>
    axiosClient.delete(`/api/api-keys/${id}`);