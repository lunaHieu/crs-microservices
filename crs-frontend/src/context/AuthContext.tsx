import { createContext, useContext, useState, type ReactNode } from 'react';
import type { LoginResponse } from '../types/auth';

interface AuthUser {
    username: string;
    role: LoginResponse['role'];
}

interface AuthContextValue {
    user: AuthUser | null;
    login: (data: LoginResponse) => void;
    logout: () => void;
    isAuthenticated: boolean;
}

const AuthContext = createContext<AuthContextValue | undefined>(undefined);
const TOKEN_KEY = 'crs_token';
const USER_KEY = 'crs_user';

export function AuthProvider({ children }: { children: ReactNode }) {
    const [user, setUser] = useState<AuthUser | null>(() => {
        const savedUser = localStorage.getItem(USER_KEY);
        const savedToken = localStorage.getItem(TOKEN_KEY);

        if (savedUser && savedToken) {
            try {
                return JSON.parse(savedUser) as AuthUser;
            } catch {
                localStorage.removeItem(USER_KEY);
                localStorage.removeItem(TOKEN_KEY);
                return null;
            }

        }

        return null;
    });

    const login = (data: LoginResponse) => {
        const authUser: AuthUser = {
            username: data.username,
            role: data.role,
        };

        localStorage.setItem(TOKEN_KEY, data.token);
        localStorage.setItem(USER_KEY, JSON.stringify(authUser));
        setUser(authUser);
    };

    const logout = () => {
        localStorage.removeItem(TOKEN_KEY);
        localStorage.removeItem(USER_KEY);
        setUser(null);
    };

    return (
        <AuthContext.Provider value={{ user, login, logout, isAuthenticated: !!user }}>
            {children}
        </AuthContext.Provider>
    );
}

// eslint-disable-next-line react-refresh/only-export-components
export function useAuth() {
    const context = useContext(AuthContext);

    if (!context) {
        throw new Error('useAuth phai duoc dung ben trong AuthProvider');
    }

    return context;
}
