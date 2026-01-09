import { inject, Injectable } from "@angular/core";
import { Subject } from "rxjs";
import { Client, Message } from "@stomp/stompjs";
import SockJS from "sockjs-client";
import { AuthService } from "./auth.service";

const WEBSOCKET_URL = 'http://localhost:8080/ws';

@Injectable({
    providedIn: 'root'
})
export class WebSocketService {
    private authService = inject(AuthService);
    private client: Client;
    private notificationSubject = new Subject<any>();
    public notification$ = this.notificationSubject.asObservable();

    constructor() {
        this.client = new Client({
            webSocketFactory: () => new SockJS(WEBSOCKET_URL),
            reconnectDelay: 5000,
        });

        this.client.onConnect = () => {
            this.client.subscribe('/user/queue/notifications', (message: Message) => {
                if (message.body) {
                    const notification = JSON.parse(message.body);
                    this.notificationSubject.next(notification);
                }
            });
        };
    }

    public connect(): void {
        const token = this.authService.accessToken();
        if (!token) return;

        this.client.connectHeaders = {
            Authorization: `Bearer ${token}`
        };

        if (!this.client.active) {
            this.client.activate();
        }
    }

    public disconnect(): void {
        if (this.client.active) {
            this.client.deactivate();
        }
    }
}