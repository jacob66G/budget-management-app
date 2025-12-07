export class ApiPaths {
    
    // transactions
    public static readonly TRANSACTIONS = '/api/v1/transactions';

    private static readonly API = '/api';

    public static readonly AUTH_BASE = `${ApiPaths.API}/auth`;
    public static readonly USERS_BASE = `${ApiPaths.API}/users`;
    public static readonly ACCOUNT_BASE = `${ApiPaths.API}/accounts`;
    public static readonly ANALYTICS_ACCOUNT_BASE = `${ApiPaths.API}/analytics/accounts`;
    public static readonly ANALYTICS_GLOBAL_BASE = `${ApiPaths.API}/analytics/global`;
    public static readonly REFERENCE_DATA_BASE = `${ApiPaths.API}/reference-data`;
    public static readonly CATEGORIES_BASE = `${ApiPaths.API}/categories`;

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

    public static readonly Analytics = {
        ANALYTICS_ACCOUNT: `${ApiPaths.ANALYTICS_ACCOUNT_BASE}`,
        GLOBAL_FINANCIAL_SUMMARY: `${ApiPaths.ANALYTICS_GLOBAL_BASE}/summary`,
        GLOBAL_BALANCE_HISTORY: `${ApiPaths.ANALYTICS_GLOBAL_BASE}/balance-history`,
        GLOBAL_CATEGORY_BREAKDOWN: `${ApiPaths.ANALYTICS_GLOBAL_BASE}/categories`,
        GLOBAL_CASH_FLOW: `${ApiPaths.ANALYTICS_GLOBAL_BASE}/cash-flow`
    }

    public static readonly ReferenceData = {
        REFERENCE_DATA: `${ApiPaths.REFERENCE_DATA_BASE}`
    }

    public static readonly Categories = {
        CATEGORIES: `${ApiPaths.CATEGORIES_BASE}`
    }
}
