import { HttpClient } from "@angular/common/http";
import { inject, Injectable, signal } from "@angular/core";
import { WebSocketService } from "./web-socket.service";
import { ApiPaths } from "../../constans/api-paths";
import { NotificationDto } from "../models/notification.model";
import { ToastService } from "./toast-service";


@Injectable({
  providedIn: 'root'
})
export class NotificationService {
  private http = inject(HttpClient);
  public wsService = inject(WebSocketService);
  private toastService = inject(ToastService);

  notifications = signal<NotificationDto[]>([]);
  unreadCount = signal(0);

  constructor() {
    this.wsService.notification$.subscribe((notification: NotificationDto) => {
      this.handleNewNotification(notification);
    });
  }

  init(): void {
    this.loadUnreadNotifications();
    this.wsService.connect();
  }

  public loadUnreadNotifications(): void {
    this.http.get<NotificationDto[]>(ApiPaths.Notifications.UNREAD_NOTIFICATIONS).subscribe(data => {
      this.notifications.set(data);
      this.updateUnreadCount();
    });
  }

  private handleNewNotification(notification: NotificationDto): void {
    this.notifications.update(current => [notification, ...current]);
    this.updateUnreadCount();

    this.showToast(notification);
  }

  private updateUnreadCount(): void {
    const count = this.notifications().filter(n => !n.isRead).length;
    this.unreadCount.set(count);
  }

  private showToast(n: NotificationDto): void {
    this.toastService.showNotification(n.title, n.message, n.type);
  }


  markAsRead(id: number): void {
    this.http.post(ApiPaths.Notifications.MARK_AS_READ(id), {}).subscribe(() => {
      this.notifications.update(current => current.map(n => {
        if (n.id === id) {
          return { ...n, isRead: true };
        }
        return n;
      }));
      this.updateUnreadCount();
    });
  }

  markAllAsRead(): void {
    this.http.post(ApiPaths.Notifications.MARK_ALL_AS_READ, {}).subscribe(() => {
      this.notifications.update(current => current.map(n => ({
        ...n,
        isRead: true
      })));
      this.updateUnreadCount();
    });
  }
}