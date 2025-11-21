import { Routes } from "@angular/router";

export const AUTH_ROUTES: Routes = [
    {
    path: 'login/2fa',
    loadComponent: () =>
      import('./components/login2fa/login2fa').then((m) => m.Login2fa),
    title: 'Verification 2FA'
  },
  {
    path: 'login',
    loadComponent: () =>
      import('./components/login/login').then((m) => m.Login),
    title: 'Login',
    pathMatch: 'full'
  },
  {
    path: 'register',
    loadComponent: () =>
      import('./components/registration/registration').then((m) => m.Registration),
    title: 'Registration',
  },
  {
    path: 'verify',
    loadComponent: () =>
      import('./components/verify-email/verify-email').then((m) => m.VerifyEmail),
    title: 'Verify Email'
  },
  {
    path: 'verifi-pending',
    loadComponent: () =>
        import('./components/verification-email-sent/verification-email-sent').then((m) => m.VerificationEmailSent),
    title: 'Verification Email'
  },
  {
    path: 'recover-password',
    loadComponent: () =>
      import('./components/recover-password/recover-password').then((m) => m.RecoverPassword),
    title: 'Recover Password'
  },
  {
    path: "reset-password",
    loadComponent: () =>
      import('./components/reset-password/reset-password').then((m) => m.ResetPassword),
    title: "Reset Password"
  }
];