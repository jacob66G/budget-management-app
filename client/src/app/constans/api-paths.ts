export class ApiPaths {
    public static readonly BASE_URL = 'http://localhost:4200';
    public static readonly BASE_API = '/api';
    public static readonly AUTH = '/api/auth';
    public static readonly REGISTER = '/api/auth/register';
    public static readonly LOGIN = '/api/auth/login';
    public static readonly LOGIN_2FA = '/api/auth/login/2fa';
    public static readonly REFRESH_TOKEN = '/api/auth/refresh';
    public static readonly LOGOUT = '/api/auth/logout';
    public static readonly VERIFY_EMAIL = '/api/auth/verify';
    public static readonly RESEND_VERIFICATION_EMAIL = '/api/auth/resend-verification';
    public static readonly RESET_PASSWORD = '/api/auth/password-reset-request';
    public static readonly REST_PASSWORD_CONFIRM = '/api/auth/password-reset-confirm';
}    