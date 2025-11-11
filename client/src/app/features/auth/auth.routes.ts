import { Routes } from "@angular/router";
import { Login } from "./components/login/login";
import { Registration } from "./components/registration/registration";
import { Login2fa } from "./components/login2fa/login2fa";
import { VerificationEmailSent } from "./components/verification-email-sent/verification-email-sent";
import { RecoverPassword } from "./components/recover-password/recover-password";
import { ResetPassword } from "./components/reset-password/reset-password";
import { VerifyEmail } from "./components/verify-email/verify-email";

export const AUTH_ROUTES: Routes = [
  {
    path: 'login',
    component: Login,
    title: 'Login',
  },
  {
    path: 'login/2fa',
    component: Login2fa,
    title: 'Verification 2FA'
  },
  {
    path: 'register',
    component: Registration,
    title: 'Rejestracja',
  },
  {
    path: 'verify',
    component: VerifyEmail,
    title: 'Verify Email'
  },
  {
    path: 'verifi-pending',
    component: VerificationEmailSent,
    title: 'Verification Email'
  },
  {
    path: 'recover-password',
    component: RecoverPassword,
    title: 'Recover Password'
  },
  {
    path: "reset-password",
    component: ResetPassword,
    title: "Reset Password"
  }
];