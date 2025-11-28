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

    // accounts
    public static readonly ACCOUNTS = '/api/accounts';

    // categories
    public static readonly CATEGORIES = '/api/categories';

    // transactions
    public static readonly TRANSACTIONS = '/api/v1/transactions';

    private static readonly API = '/api';

    public static readonly AUTH_BASE = `${ApiPaths.API}/auth`;
    public static readonly USERS_BASE = `${ApiPaths.API}/users`;
    public static readonly  ACCOUNT_BASE = `${ApiPaths.API}/accounts`

    public static readonly Auth = {
        LOGIN: `${ApiPaths.AUTH_BASE}/login`,
        LOGIN_2FA: `${ApiPaths.AUTH_BASE}/login/2fa`,
        REGISTER: `${ApiPaths.AUTH_BASE}/register`,
        REFRESH_TOKEN: `${ApiPaths.AUTH_BASE}/refresh`,
        LOGOUT: `${ApiPaths.AUTH_BASE}/logout`,
        VERIFY_EMAIL: `${ApiPaths.AUTH_BASE}/verify`,
        RESEND_VERIFICATION: `${ApiPaths.AUTH_BASE}/resend-verification`,
        RESET_PASSWORD_REQUEST: `${ApiPaths.AUTH_BASE}/password-reset-request`,
        RESET_PASSWORD_CONFIRM: `${ApiPaths.AUTH_BASE}/password-reset-confirm`,
    };

    public static readonly User = {
        ME: `${ApiPaths.USERS_BASE}/me`,

        CHANGE_PASSWORD: `${ApiPaths.USERS_BASE}/me/change-password`,
        CLOSE_ACCOUNT: `${ApiPaths.USERS_BASE}/me/close-account`,

        TFA_SETUP: `${ApiPaths.USERS_BASE}/me/2fa/setup`,
        TFA_VERIFY: `${ApiPaths.USERS_BASE}/me/2fa/verify`,
        TFA_DISABLE: `${ApiPaths.USERS_BASE}/me/2fa/disable`,

        SESSIONS: `${ApiPaths.USERS_BASE}/me/sessions`,
    };

    public static readonly Account = {
        ACCOUNT: `${ApiPaths.ACCOUNT_BASE}`,
    }
}
