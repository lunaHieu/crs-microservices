import axiosClient from './axiosClient';
import type { Registration, RegistrationRequest } from '../types/registration';

export const registerCourse = (payload: RegistrationRequest) =>
    axiosClient.post<Registration>('/api/registrations', payload);

export const cancelRegistration = (id: number) =>
    axiosClient.delete<Registration>(`/api/registrations/${id}`);

export const getMyRegistrations = () =>
    axiosClient.get<Registration[]>('/api/registrations/my');
