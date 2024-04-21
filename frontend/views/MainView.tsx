import {MessageInput} from "@hilla/react-components/MessageInput";
import {AppLayout} from "@hilla/react-components/AppLayout";
import {MessageList, MessageListItem} from "@hilla/react-components/MessageList";
import {StreamingCompletionChatService} from "Frontend/generated/endpoints.js";
import { useState } from "react";
import {nanoid} from "nanoid";

const chatId = nanoid();
export default function MainView() {
  const [messages, setMessages] = useState<MessageListItem[]>([]);

    function addMessage(message: MessageListItem) {
        setMessages(messages => [...messages, message])
    }

    function appendToLastMessage(textChunk: string | any) {
        setMessages(messages => {
            const lastMessage = messages[messages.length - 1];
            lastMessage.text += textChunk;
            return [...messages.slice(0, -1), lastMessage];
        })

    }

    async function sendMessage(message: string) {
        addMessage({
            text: message,
            userName: 'You',
            userColorIndex: 1
        });

        let first = true;
        StreamingCompletionChatService.generateResponse(chatId, message)
            .onNext(textChunk => {
                if (first && textChunk) {
                    addMessage({
                        text: textChunk,
                        userName: 'Assistant',
                        userColorIndex: 2
                    });
                    first = false;
                } else {
                    appendToLastMessage(textChunk);
                }
            })
        
    }

    return (
        <AppLayout>

                <header className="flex flex-col gap-m">
                    <h1 className="text-l m-0">RAG Demo with Azure Open AI and Azure AI Search</h1>
                    <div className="p-m flex flex-col h-full box-border">
                        <MessageList items={messages} className="flex-grow"/>
                        <MessageInput onSubmit={e => sendMessage(e.detail.value)}/>
                    </div>
                </header>

        </AppLayout>
);
}
