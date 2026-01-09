export class ApiPaths {
  private static readonly API_ROOT = '/api';
  
  private static readonly API_V1 = `${ApiPaths.API_ROOT}/v1`;

  public static readonly Auth = {
    BASE: `${ApiPaths.API_V1}/auth`,
    LOGIN: `${ApiPaths.API_V1}/auth/login`,
    LOGIN_2FA: `${ApiPaths.API_V1}/auth/login/2fa`,
    REGISTER: `${ApiPaths.API_V1}/auth/register`,
    REFRESH_TOKEN: `${ApiPaths.API_V1}/auth/refresh`,
    LOGOUT: `${ApiPaths.API_V1}/auth/logout`,
    VERIFY_EMAIL: `${ApiPaths.API_V1}/auth/verify`,
    RESEND_VERIFICATION: `${ApiPaths.API_V1}/auth/resend-verification`,
    RESET_PASSWORD_REQUEST: `${ApiPaths.API_V1}/auth/password-reset-request`,
    RESET_PASSWORD_CONFIRM: `${ApiPaths.API_V1}/auth/password-reset-confirm`,
  };

  public static readonly User = {
    BASE: `${ApiPaths.API_V1}/users`,
    ME: `${ApiPaths.API_V1}/users/me`,
    CHANGE_PASSWORD: `${ApiPaths.API_V1}/users/me/change-password`,
    CLOSE_ACCOUNT: `${ApiPaths.API_V1}/users/me/close-account`,
    SESSIONS: `${ApiPaths.API_V1}/users/me/sessions`,
    
    Tfa: {
      SETUP: `${ApiPaths.API_V1}/users/me/2fa/setup`,
      VERIFY: `${ApiPaths.API_V1}/users/me/2fa/verify`,
      DISABLE: `${ApiPaths.API_V1}/users/me/2fa/disable`,
    }
  };

  public static readonly Account = {
    BASE: `${ApiPaths.API_V1}/accounts`,
    BY_ID: (id: number) => `${ApiPaths.API_V1}/accounts/${id}`,
    ACTICATE: (id: number) => `${ApiPaths.API_V1}/accounts/${id}/activate`,
    DEACTIVATE: (id: number) => `${ApiPaths.API_V1}/accounts/${id}/deactivate`,
  };

  public static readonly Transactions = {
    BASE: `${ApiPaths.API_V1}/transactions`,
    BY_ID: (id: number) => `${ApiPaths.API_V1}/transactions/${id}`,
    Attachments: {
      INIT_UPLOAD: (id: number) => `${ApiPaths.API_V1}/transactions/${id}/attachment/presigned-upload-url`,
      CONFIRM_UPLOAD: (id: number) => `${ApiPaths.API_V1}/transactions/${id}/attachment`,
      GET_DOWNLOAD_URL: (id: number) => `${ApiPaths.API_V1}/transactions/${id}/attachment`,
    }
  };

  public static readonly RecurringTemplates = {
    BASE: `${ApiPaths.API_V1}/recurring-transactions`,
    BY_ID: (id: number) => `${ApiPaths.API_V1}/recurring-transactions/${id}`,
    STATUS: (id: number) => `${ApiPaths.API_V1}/recurring-transactions/${id}/status`,
    UPPCOMMING: `${ApiPaths.API_V1}/recurring-transactions/upcoming`,
  };

  public static readonly Chats = {
    BASE: `${ApiPaths.API_V1}/chats`,
    BY_ID: (id: number) => `${ApiPaths.API_V1}/chats/${id}`,
  };

  public static readonly Analytics = {
    Account: {
      BASE: `${ApiPaths.API_V1}/analytics/accounts`,
      BALANCE_HISTORY: (id: number) => `${ApiPaths.API_V1}/analytics/accounts/${id}/balance-history`,
      CATEGORY_BREAKDOWN: (id: number) => `${ApiPaths.API_V1}/analytics/accounts/${id}/categories`,
      CASH_FLOW: (id: number)  => `${ApiPaths.API_V1}/analytics/accounts/${id}/cash-flow`,
      REPORT: (id: number) => `${ApiPaths.API_V1}/analytics/accounts/${id}/generate-report`,
    },
    Global: {
      BASE: `${ApiPaths.API_V1}/analytics/global`,
      SUMMARY: `${ApiPaths.API_V1}/analytics/global/summary`,
      BALANCE_HISTORY: `${ApiPaths.API_V1}/analytics/global/balance-history`,
      CATEGORY_BREAKDOWN: `${ApiPaths.API_V1}/analytics/global/categories`,
      CASH_FLOW: `${ApiPaths.API_V1}/analytics/global/cash-flow`,
    }
  };

  public static readonly Categories = {
    BASE: `${ApiPaths.API_V1}/categories`,
    BY_ID: (id: number) => `${ApiPaths.API_V1}/categories/${id}`,
    REASSIGN: (id: number) => `${ApiPaths.API_V1}/categories/${id}/reassign`,
  };

  public static readonly ReferenceData = {
    BASE: `${ApiPaths.API_V1}/reference-data`,
  };

  public static readonly Notifications = {
    BASE: `${ApiPaths.API_V1}/notifications`,
    UNREAD: `${ApiPaths.API_V1}/notifications/unread`,
    MARK_ALL_READ: `${ApiPaths.API_V1}/notifications/mark-as-read`,
    MARK_ONE_READ: (id: number) => `${ApiPaths.API_V1}/notifications/${id}/mark-as-read`,
  };
}