import { useCallback, useState } from 'react';

export function useToast() {
    const [toast, setToast] = useState<{
        message: string;
        type: 'success' | 'error';
    } | null>(null);

    const showToast = useCallback((message: string, type: 'success' | 'error') => {
        setToast({ message, type });
    }, []);

    const clearToast = useCallback(() => {
        setToast(null);
    }, []);

    return { toast, showToast, clearToast };
}
