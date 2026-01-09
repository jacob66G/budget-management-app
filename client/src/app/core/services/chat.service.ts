import { HttpClient, httpResource } from '@angular/common/http';
import { inject, Injectable, signal } from '@angular/core';
import { Observable } from 'rxjs';
import { ChatCreateResponse } from '../../features/assistant/models/chat-create-response.model';
import { ApiPaths } from '../../constans/api-paths';
import { ChatMessage } from '../../features/assistant/models/chat-message.model';

@Injectable({
  providedIn: 'root'
})
export class ChatService {

  private readonly http = inject(HttpClient);

  selectedChatId = signal<string | undefined>(undefined);

  createNewChat(message: string): Observable<ChatCreateResponse> {
    return this.http.post<ChatCreateResponse>(ApiPaths.Chats.BASE, {message})
  }

  continueChat(chatId: string, message: string): Observable<ChatMessage> {
    return this.http.post<ChatMessage>(`${ApiPaths.Chats.BY_ID(Number(chatId))}`, {message});
  }

  chatMessagesResource = httpResource<ChatMessage[]>(() => {
    const chatId = this.selectedChatId();
    return chatId ? `${ApiPaths.Chats.BY_ID(Number(chatId))}` : undefined;
  });

  selectChat(chatId: string): void {
    this.selectedChatId.set(chatId);
  }
}
