export interface LoginRequest {
    username: string;
    password: string;
}

export interface LoginResponse {
    userId: number;
    token: string;
    username: string;
    role: 'ADMIN' | 'STUDENT';
}
