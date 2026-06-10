import { createRouter, createWebHistory } from 'vue-router';
import PublicLayout from '../layouts/PublicLayout.vue';
import LandingView from '../views/LandingView.vue';
import ChatView from '../views/ChatView.vue';
import WalkingRouteView from '../components/WalkingRoute.vue';
import LoginView from '../views/LoginView.vue';
import RegisterView from '../views/RegisterView.vue';
import StudentAssignmentsView from '../views/student/StudentAssignmentsView.vue';
import StudentMessagesView from '../views/student/StudentMessagesView.vue';
import StudentProfileView from '../views/student/StudentProfileView.vue';
import TeacherLayout from '../layouts/TeacherLayout.vue';
import TeacherProgressView from '../views/teacher/TeacherProgressView.vue';
import TeacherTasksView from '../views/teacher/TeacherTasksView.vue';
import TeacherGradingView from '../views/teacher/TeacherGradingView.vue';
import TeacherStudentsView from '../views/teacher/TeacherStudentsView.vue';
import TeacherProfileView from '../views/teacher/TeacherProfileView.vue';
import { getToken, getUser, isStudent, isTeacher } from '../stores/auth';
import authApi from '../services/authApi';
import { setAuth, clearAuth } from '../stores/auth';

const routes = [
    {
        path: '/',
        component: PublicLayout,
        meta: { public: true },
        children: [
            { path: '', name: 'Landing', component: LandingView },
            { path: 'login', name: 'Login', component: LoginView },
            { path: 'register', name: 'Register', component: RegisterView },
        ],
    },
    {
        path: '/chat',
        name: 'ChatView',
        component: ChatView,
        meta: { role: 'STUDENT' },
    },
    {
        path: '/assignments',
        name: 'StudentAssignments',
        component: StudentAssignmentsView,
        meta: { role: 'STUDENT' },
    },
    {
        path: '/messages',
        name: 'StudentMessages',
        component: StudentMessagesView,
        meta: { role: 'STUDENT' },
    },
    {
        path: '/profile',
        name: 'StudentProfile',
        component: StudentProfileView,
        meta: { role: 'STUDENT' },
    },
    {
        path: '/walkingRoute',
        name: 'WalkingRouteView',
        component: WalkingRouteView,
        meta: { role: 'STUDENT' },
    },
    {
        path: '/teacher',
        component: TeacherLayout,
        meta: { role: 'TEACHER' },
        children: [
            { path: '', redirect: { name: 'TeacherChat' } },
            {
                path: 'chat',
                name: 'TeacherChat',
                component: ChatView,
                props: { teacherMode: true, embedded: true },
            },
            {
                path: 'progress',
                name: 'TeacherProgress',
                component: TeacherProgressView,
            },
            {
                path: 'grading',
                name: 'TeacherGrading',
                component: TeacherGradingView,
            },
            {
                path: 'tasks',
                name: 'TeacherTasks',
                component: TeacherTasksView,
            },
            {
                path: 'students',
                name: 'TeacherStudents',
                component: TeacherStudentsView,
            },
            {
                path: 'profile',
                name: 'TeacherProfile',
                component: TeacherProfileView,
            },
        ],
    },
];

const router = createRouter({
    history: createWebHistory(),
    routes,
});

const ROUTE_HTML_CLASSES = ['public-route', 'teacher-route', 'student-route', 'chat-route'];

function isPublicRoute(to) {
    return to.matched.some((record) => record.meta.public);
}

function applyRouteShell(to) {
    document.documentElement.classList.remove(...ROUTE_HTML_CLASSES);
    if (isPublicRoute(to)) {
        document.documentElement.classList.add('public-route');
        return;
    }
    if (to.path === '/chat' || to.name === 'ChatView') {
        document.documentElement.classList.add('chat-route');
    } else if (to.path.startsWith('/teacher')) {
        document.documentElement.classList.add('teacher-route');
    } else if (
        to.path === '/assignments' ||
        to.path === '/messages' ||
        to.path === '/profile' ||
        to.name === 'StudentAssignments' ||
        to.name === 'StudentMessages' ||
        to.name === 'StudentProfile'
    ) {
        document.documentElement.classList.add('student-route');
    }
}

router.beforeEach(async (to) => {
    if (isPublicRoute(to)) {
        applyRouteShell(to);
        if (getToken()) {
            if (to.name === 'Login' || to.name === 'Register') {
                return isTeacher() ? '/teacher/chat' : '/chat';
            }
            if (to.name === 'Landing') {
                return isTeacher() ? '/teacher/chat' : '/chat';
            }
        }
        return true;
    }

    applyRouteShell(to);

    if (!getToken()) {
        return '/login';
    }

    if (!getUser()) {
        try {
            const res = await authApi.me();
            const u = res.data.data;
            setAuth(getToken(), { ...u, token: getToken() });
        } catch {
            clearAuth();
            return '/login';
        }
    }

    const requiredRole = to.meta.role;
    if (requiredRole === 'STUDENT' && !isStudent()) {
        return '/teacher/chat';
    }
    if (requiredRole === 'TEACHER' && !isTeacher()) {
        return '/chat';
    }
    return true;
});

router.afterEach((to) => {
    if (isPublicRoute(to)) {
        window.scrollTo(0, 0);
    }
});

export default router;
