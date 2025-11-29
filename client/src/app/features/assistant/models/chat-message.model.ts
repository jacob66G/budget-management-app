import { MessageType } from "./message-type.enum";

export interface ChatMessage {
    content: string,
    type: MessageType
}