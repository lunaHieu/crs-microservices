import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import ProtectedRoute from './components/ProtectedRoute';
import Navbar from './components/Navbar';
import LoginPage from './pages/LoginPage';
import CoursesPage from './pages/CoursesPage';
import AdminCoursesPage from './pages/AdminCoursesPage';
import RegisterCoursePage from './pages/RegisterCoursePage';

function App() {
    return (
        <BrowserRouter>
            <AuthProvider>
                <Navbar />
                <Routes>
                    <Route path="/" element={<Navigate to="/courses" replace />} />
                    <Route path="/login" element={<LoginPage />} />
                    <Route path="/courses" element={<CoursesPage />} />
                    <Route
                        path="/admin/courses"
                        element={
                            <ProtectedRoute requiredRole="ADMIN">
                                <AdminCoursesPage />
                            </ProtectedRoute>
                        }
                    />
                    <Route
                        path="/register-course"
                        element={
                            <ProtectedRoute requiredRole="STUDENT">
                                <RegisterCoursePage />
                            </ProtectedRoute>
                        }
                    />
                    <Route path="*" element={<Navigate to="/courses" replace />} />
                </Routes>
            </AuthProvider>
        </BrowserRouter>
    );
}

export default App;
