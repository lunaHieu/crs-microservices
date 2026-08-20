export interface ApiErrorResponse {
    message?: string;
    [field: string]: string | undefined;
}