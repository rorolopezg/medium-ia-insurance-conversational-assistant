'use client';

import React, { useEffect, useRef, useState } from 'react';
import { createStompClient } from '@/lib/stompClient';
import { getOrCreateSessionId } from '@/lib/session';
import { MessageSquare, User, Bot } from 'lucide-react';
import ReactMarkdown from 'react-markdown';
import remarkGfm from 'remark-gfm';

type ChatResponse = {
  sessionId: string;
  type: 'assistant' | 'typing' | 'error';
  content: string;
};

type ChatMessage = {
  sessionId: string;
  text: string;
};

// Normaliza y “arregla” Markdown para que las listas se vean bien
function normalizeMarkdown(raw: string) {
  if (!raw) return '';
  return raw.replace(/\r\n/g, '\n').replace(/\\n/g, '\n');
}


export default function Chat() {
  const WS_URL = process.env.NEXT_PUBLIC_WS_BROKER_URL!;
  const sessionIdRef = useRef<string>(getOrCreateSessionId());
  const clientRef = useRef<any>(null);

  const [connected, setConnected] = useState(false);
  const [typing, setTyping] = useState(false);
  const [messages, setMessages] = useState<
    { role: 'user' | 'assistant' | 'error'; content: string }[]
  >([]);
  const [input, setInput] = useState('');

  useEffect(() => {
    const client = createStompClient(WS_URL, {
      onConnect: () => {
        setConnected(true);
        client.subscribe(`/topic/chat/${sessionIdRef.current}`, (msg) => {
          try {
            const payload: ChatResponse = JSON.parse(msg.body);
            if (payload.type === 'typing') {
              setTyping(true);
              return;
            }
            setTyping(false);
            setMessages((prev) => [
              ...prev,
              {
                role: payload.type === 'assistant' ? 'assistant' : 'error',
                content: payload.content,
              },
            ]);
          } catch {
            setTyping(false);
          }
        });
      },
      onDisconnect: () => setConnected(false),
      onError: () => setConnected(false),
    });

    client.activate();
    clientRef.current = client;
    return () => client.deactivate();
  }, [WS_URL]);

  const sendMessage = (e: React.FormEvent) => {
    e.preventDefault();
    if (!input.trim() || !clientRef.current) return;

    const text = input.trim();
    setMessages((prev) => [...prev, { role: 'user', content: text }]);
    setInput('');

    const payload: ChatMessage = { sessionId: sessionIdRef.current, text };
    clientRef.current.publish({
      destination: '/app/chat.send',
      body: JSON.stringify(payload),
    });
  };

  return (
    <div className="flex flex-col items-center min-h-screen bg-gradient-to-b from-gray-50 to-gray-100 p-6">
      <div className="w-full max-w-2xl bg-white shadow-xl rounded-2xl flex flex-col">
        {/* Header */}
        <header className="flex items-center gap-2 border-b px-5 py-3 bg-blue-600 text-white rounded-t-2xl">
          <MessageSquare size={20} />
          <h1 className="font-semibold">AI Insurance Assistant</h1>
          <span className="ml-auto text-xs opacity-80">
            {connected ? 'Connected' : 'Disconnected'}
          </span>
        </header>

        {/* Messages */}
        <div className="flex-1 p-5 overflow-y-auto space-y-4 bg-gray-50">
          {messages.length === 0 && (
            <div className="text-center text-gray-400 text-sm">
              💬 Start the conversation…
            </div>
          )}

          {messages.map((m, i) => (
            <div key={i} className="flex items-start gap-2">
              {m.role === 'assistant' ? (
                <>
                  <div className="bg-blue-100 p-2 rounded-full">
                    <Bot size={18} className="text-blue-700" />
                  </div>

                  {/* Contenedor Markdown con estilos de lista forzados */}
                  <div
                    className="
                      bg-white border rounded-xl px-4 py-2 shadow-sm text-sm text-gray-800 max-w-[80%]
                      prose prose-sm prose-blue
                      [&>p]:my-3
                      [&>ul]:my-3 [&>ul]:list-disc [&>ul]:pl-5
                      [&>ol]:my-3 [&>ol]:list-decimal [&>ol]:pl-5
                      marker:text-blue-600
                    "
                  >
                    <ReactMarkdown
                      remarkPlugins={[remarkGfm]}
                      components={{
                        ul: ({ node, ...props }) => <ul className="list-disc pl-5 my-2" {...props} />,
                        ol: ({ node, ...props }) => <ol className="list-decimal pl-5 my-2" {...props} />,
                        li: ({ node, ...props }) => <li className="my-1" {...props} />,
                        // Opcional: títulos más compactos si el LLM los usa
                        h3: ({ node, ...props }) => <h3 className="text-base font-semibold mt-3 mb-1" {...props} />,
                        strong: ({ node, ...props }) => <strong className="font-semibold" {...props} />,
                      }}
                    >
                      {normalizeMarkdown(m.content)}
                    </ReactMarkdown>
                  </div>
                </>
              ) : m.role === 'user' ? (
                <div className="flex ml-auto gap-2 items-start">
                  <div className="bg-blue-600 text-white rounded-xl px-4 py-2 text-sm max-w-[80%]">
                    {m.content}
                  </div>
                  <div className="bg-gray-200 p-2 rounded-full">
                    <User size={18} className="text-gray-700" />
                  </div>
                </div>
              ) : (
                <div className="text-red-600 text-sm italic">{m.content}</div>
              )}
            </div>
          ))}

          {typing && (
            <div className="flex items-center gap-2 mt-2">
              <div className="bg-blue-100 p-2 rounded-full">
                <Bot size={18} className="text-blue-700" />
              </div>
              <div className="flex space-x-1">
                <span className="w-2 h-2 bg-blue-500 rounded-full animate-bounce"></span>
                <span className="w-2 h-2 bg-blue-500 rounded-full animate-bounce [animation-delay:0.1s]"></span>
                <span className="w-2 h-2 bg-blue-500 rounded-full animate-bounce [animation-delay:0.2s]"></span>
              </div>
            </div>
          )}
        </div>

        {/* Input */}
        <form
          onSubmit={sendMessage}
          className="flex items-center gap-3 border-t px-4 py-3 bg-white rounded-b-2xl"
        >
          <input
            value={input}
            onChange={(e) => setInput(e.target.value)}
            className="flex-1 border border-gray-300 rounded-lg px-4 py-2 text-gray-900 placeholder-gray-400 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
            placeholder="Type your message…"
          />
          <button
            disabled={!connected || !input.trim()}
            className="bg-blue-600 text-white px-4 py-2 rounded-lg text-sm font-medium hover:bg-blue-700 disabled:opacity-50"
          >
            Send
          </button>
        </form>
      </div>
    </div>
  );
}
