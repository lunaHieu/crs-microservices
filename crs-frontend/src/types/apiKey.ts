export interface ApiKey {
    id: number;
    keyValue: string;
    ownerName: string;
    scopes: string;
    status: 'ACTIVE' | 'REVOKED';
    expiresAt: string | null;
    createdAt: string;
}

export interface ApiKeyCreateRequest {
    ownerName: string;
    scopes: string;
    validDays?: number;
}