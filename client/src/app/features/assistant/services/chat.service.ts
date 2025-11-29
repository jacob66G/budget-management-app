import { HttpClient, httpResource } from '@angular/common/http';
import { ElementRef, inject, Injectable, signal, viewChild } from '@angular/core';
import { Observable, single } from 'rxjs';
import { ChatCreateResponse } from '../models/chat-create-response.model';
import { ApiPaths } from '../../../constans/api-paths';
import { ChatMessage } from '../models/chat-message.model';

@Injectable({
  providedIn: 'root'
})
export class ChatService {

  private readonly http = inject(HttpClient);

  selectedChatId = signal<string | undefined>(undefined);

  createNewChat(message: string): Observable<ChatCreateResponse> {
    return this.http.post<ChatCreateResponse>(ApiPaths.CHATS, {message})
  }

  continueChat(chatId: string, message: string): Observable<ChatMessage> {
    return this.http.post<ChatMessage>(`${ApiPaths.CHATS}/${chatId}`, {message});
  }

  chatMessagesResource = httpResource<ChatMessage[]>(() => {
    const chatId = this.selectedChatId();
    return chatId ? `${ApiPaths.CHATS}/${chatId}` : undefined;
  });

  selectChat(chatId: string): void {
    this.selectedChatId.set(chatId);
  }
}
