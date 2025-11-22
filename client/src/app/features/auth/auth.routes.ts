import { Routes } from "@angular/router";

export const AUTH_ROUTES: Routes = [
    {
    path: 'login/2fa',
    loadComponent: () =>
      import('./pages/login2fa/login2fa').then((m) => m.Login2fa),
    title: 'Verification 2FA'
  },
  {
    path: 'login',
    loadComponent: () =>
      import('./pages/login/login').then((m) => m.Login),
    title: 'Login',
    pathMatch: 'full'
  },
  {
    path: 'register',
    loadComponent: () =>
      import('./pages/registration/registration').then((m) => m.Registration),
    title: 'Registration',
  },
  {
    path: 'verify',
    loadComponent: () =>
      import('./pages/verify-email/verify-email').then((m) => m.VerifyEmail),
    title: 'Verify Email'
  },
  {
    path: 'verifi-pending',
    loadComponent: () =>
        import('./pages/verification-email-sent/verification-email-sent').then((m) => m.VerificationEmailSent),
    title: 'Verification Email'
  },
  {
    path: 'recover-password',
    loadComponent: () =>
      import('./pages/recover-password/recover-password').then((m) => m.RecoverPassword),
    title: 'Recover Password'
  },
  {
    path: "reset-password",
    loadComponent: () =>
      import('./pages/reset-password/reset-password').then((m) => m.ResetPassword),
    title: "Reset Password"
  }
];