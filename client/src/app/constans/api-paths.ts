export class ApiPaths {
    private static readonly API = '/api';

    public static readonly AUTH_BASE = `${ApiPaths.API}/auth`;
    public static readonly USERS_BASE = `${ApiPaths.API}/users`;

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
}
