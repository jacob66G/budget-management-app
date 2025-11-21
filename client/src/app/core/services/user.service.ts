import { HttpClient } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { ChangePasswordRequest, TfaQRCode, TfaVerifyRequest, UpdateUserRequest } from "../../features/user/user-profile/model/user-profile.model";
import { User } from "../models/user.model";
import { Observable } from "rxjs";
import { ApiPaths } from "../../constans/api-paths";
import { ResponseMessage } from "../models/response-message.model";

@Injectable({
  providedIn: 'root'
})
export class UserService {

  constructor(private http: HttpClient) { }

  updateUser(userData: UpdateUserRequest): Observable<User> {
    return this.http.patch<User>(ApiPaths.User.ME, userData);
  }

  changePassword(changePasswordData: ChangePasswordRequest): Observable<void> {
    return this.http.post<void>(ApiPaths.User.CHANGE_PASSWORD, changePasswordData);
  }

  tfaDisable(tfaVerifyData: TfaVerifyRequest): Observable<void> {
    return this.http.post<void>(ApiPaths.User.TFA_DISABLE, tfaVerifyData);
  }

  verifyTfaSetup(tfaVerifyData: TfaVerifyRequest): Observable<void> {
    return this.http.post<void>(ApiPaths.User.TFA_VERIFY, tfaVerifyData);
  }

  tfaSetup(): Observable<TfaQRCode> {
    return this.http.get<TfaQRCode>(ApiPaths.User.TFA_SETUP);
  }

  closeAccount(): Observable<ResponseMessage> {
    return this.http.delete<ResponseMessage>(ApiPaths.User.CLOSE_ACCOUNT);
  }
  
}