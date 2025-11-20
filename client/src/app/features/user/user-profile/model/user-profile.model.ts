export interface UpdateUserRequest{
    name: string;
    surname: string;
}

export interface ChangePasswordRequest {
    oldPassword: string;
    newPassword: string;
    passwordConfirmation: string
}

export interface TfaQRCode {
    qrCodeUri: string
}

export interface TfaVerifyRequest {
    code: string
}