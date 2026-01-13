import { Component, effect, ElementRef, inject, OnInit, signal, viewChild } from '@angular/core';
import { MatCardModule } from '@angular/material/card';
import { MatIcon } from "@angular/material/icon";
import { MatInputModule } from '@angular/material/input';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { TextFieldModule } from '@angular/cdk/text-field';
import { MatTooltipModule } from '@angular/material/tooltip';
import { ChatMessage } from '../../models/chat-message.model';
import { ChatService } from '../../../../core/services/chat.service';
import { MessageType } from '../../models/message-type.enum';
import { ChatCreateResponse } from '../../models/chat-create-response.model';
import { MarkdownToHtmlPipe } from '../../pipes/markdown-to-html-pipe';

@Component({
  selector: 'app-chat-page',
  imports: [MatCardModule, MatIcon, MatInputModule, FormsModule, MatButtonModule, TextFieldModule, MatTooltipModule, MarkdownToHtmlPipe],
  templateUrl: './chat-page.component.html',
  styleUrl: './chat-page.component.scss'
})
export class ChatPageComponent implements OnInit{
  
  private readonly chat = viewChild.required<ElementRef>('chat');
  private readonly chatService = inject(ChatService);

  public messages = signal<ChatMessage[]>([]);

  public userInput: string = '';
  public isLoading: boolean = false;

  ngOnInit(): void {
    this.loadConversation();
  }

  private loadConversation(): void {
    if (this.chatService.selectedChatId() !== undefined) {
      this.chatService.chatMessagesResource.reload();
    }
  }

  onEnterPressed(event?: Event): void {
    event?.preventDefault(); 
    
    if (!this.isUserInputEmpty() && !this.isLoading) {
      this.onSendPressed();
    }
  }

  isUserInputEmpty(): boolean {
    if (this.userInput.trim() === '') {
      return true
    }
    return false;
  }

  onSendPressed(): void {
    if (this.isUserInputEmpty() || this.isLoading) {
      return;
    }

    this.userInput = this.userInput.trim();
    this.updateMessages(this.userInput, MessageType.USER);
    this.isLoading = true;
    this.sendChatMessage();
    this.clearUserInput();
  }

  private readonly logSize = effect( () => {
    this.messages();
  });

  clearUserInput(): void {
    this.userInput = '';
  }

  private sendChatMessage() {
    const currentChatId = this.chatService.selectedChatId();
    const message = this.userInput;

    if (currentChatId) {  // continuation existing conversation
      this.chatService.continueChat(currentChatId, message)
        .subscribe( {
          next: (response: ChatMessage) => {
            if (response) {
              this.updateMessages(response.content, MessageType.ASSISTANT);
            }
            this.isLoading = false;
          },
          error: () => {
            this.isLoading = false;
          }
        });
    } else {  // creating new conversation
      this.chatService.createNewChat(message)
        .subscribe( {
          next: (response: ChatCreateResponse) => {
            if (response) {
              this.chatService.selectChat(response.chatId);
              this.updateMessages(response.message, MessageType.ASSISTANT);
            }
            this.isLoading = false;
          },
          error: () => {
            this.isLoading = false;
          }
        });
    }
  }

  private updateMessages(content: string, type: MessageType): void {
    this.messages.update( (msgs) => [...msgs, {content, type}]);
  }

  private readonly autoScrollEffect = effect( () => {
    this.messages();
    setTimeout(() => this.scrollToBottom(), 50);
  });

  private scrollToBottom(): void {
    try {
      const chatElement = this.chat();
      if (chatElement?.nativeElement) {
        chatElement.nativeElement.scrollTop = chatElement.nativeElement.scrollHeight;
      }
    } catch (err) {
      console.error("Failder to scroll down", err);
    }
  }
}
