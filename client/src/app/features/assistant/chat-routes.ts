import { Routes } from "@angular/router";
import { ChatPageComponent } from "./pages/chat-page/chat-page.component";

export const CHATS_ROUTES: Routes = [
    {
        path: '',
        component: ChatPageComponent,
        title: 'chat'
    }
]