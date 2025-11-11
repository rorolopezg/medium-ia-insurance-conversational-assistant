'use client';

import React, { useEffect, useRef, useState } from 'react';
import { createStompClient } from '@/lib/stompClient';
import { getOrCreateSessionId } from '@/lib/session';
import { MessageSquare, User, Bot, Copy } from 'lucide-react';
import ReactMarkdown from 'react-markdown';
import remarkGfm from 'remark-gfm';

type ChatMsg = { role: 'user' | 'assistant' | 'error'; content: string };

export default function Chat() {
  const WS_URL = process.env.NEXT_PUBLIC_WS_BROKER_URL!;
  const clientRef = useRef<any>(null);
  const endRef = useRef<HTMLDivElement | null>(null);

  const [sessionId, setSessionId] = useState<string | null>(null);
  const [connected, setConnected] = useState(false);
  const [typing, setTyping] = useState(false);
  const [messages, setMessages] = useState<ChatMsg[]>([]);
  const [input, setInput] = useState('');

  // Genera sessionId solo en cliente (evita SSR hydration issues)
  useEffect(() => {
    try {
      const id = getOrCreateSessionId(false);
      setSessionId(id);
    } catch {
      // si algo falla, no bloquees la UI
      setSessionId(null);
    }
  }, []);

  // Auto-scroll cuando llegan mensajes
  useEffect(() => {
    endRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages, typing]);

  // Conexión STOMP cuando ya hay sessionId
  useEffect(() => {
    if (!sessionId) return;
    const client = createStompClient(WS_URL, {
      onConnect: () => {
        setConnected(true);
        const topic = `/topic/chat/${sessionId}`;
        client.subscribe(topic, (msg: any) => {
          try {
            const payload = JSON.parse(msg.body ?? '{}');
            if (!payload?.type) return;

            if (payload.type === 'typing') {
              setTyping(true);
              return;
            }

            if (payload.type === 'assistant') {
              setTyping(false);
              setMessages(prev => [...prev, { role: 'assistant', content: payload.content ?? '' }]);
              return;
            }

            if (payload.type === 'error') {
              setTyping(false);
              setMessages(prev => [...prev, { role: 'error', content: payload.content ?? 'Unknown error' }]);
              return;
            }

            // tipos no contemplados
            setTyping(false);
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
  }, [WS_URL, sessionId]);

  // Enviar mensaje
  const sendMessage = (e: React.FormEvent) => {
    e.preventDefault();
    if (!input.trim() || !clientRef.current || !sessionId) return;
    const text = input.trim();
    setMessages(prev => [...prev, { role: 'user', content: text }]);
    setInput('');
    clientRef.current.publish({
      destination: '/app/chat.send',
      body: JSON.stringify({ sessionId, text }),
    });
  };

  const copySessionId = async () => {
    if (!sessionId) return;
    try {
      await navigator.clipboard.writeText(sessionId);
    } catch {
      // noop
    }
  };

  // UI base
  return (
    <div className="min-h-screen bg-gray-50 p-6">
      <div className="mx-auto max-w-3xl bg-white rounded-2xl shadow-lg flex flex-col h-[80vh]">
      <header className="bg-gradient-to-r from-blue-600 to-blue-800 text-white px-6 py-4 flex items-center justify-between rounded-t-2xl shadow-sm">
        <div className="flex items-center gap-3">
          <MessageSquare className="text-white" size={22} />
          <div>
            <h1 className="text-lg font-semibold">SuperChat – Part 5</h1>
            <p className="text-xs text-blue-100">
              {connected ? 'WebSocket connected' : 'Connecting…'}
            </p>
          </div>
        </div>
        <div className="text-xs">
          <div className="flex items-center gap-2">
            <code className="bg-blue-500/40 rounded px-2 py-1 text-blue-50">
              session: {sessionId ?? '—'}
            </code>
            <button
              type="button"
              onClick={copySessionId}
              title="Copy session id"
              className="hover:bg-blue-500/40 rounded p-1"
            >
              <Copy size={14} className="text-white" />
            </button>
          </div>
        </div>
      </header>

        {/* Messages */}
        <div className="flex-1 p-5 overflow-y-auto space-y-4 bg-gray-50">
          {messages.length === 0 && (
            <div className="text-center text-gray-400 text-sm">💬 Start the conversation…</div>
          )}

          {messages.map((m, i) => {
            if (m.role === 'assistant') {
              return (
                <div key={i} className="flex items-start gap-2">
                  <div className="bg-blue-100 p-2 rounded-full">
                    <Bot size={18} className="text-blue-700" />
                  </div>
                  <div className="bg-white border rounded-xl px-4 py-3 text-sm prose prose-sm max-w-none
                                  [&>p]:my-2 [&>ul]:my-3 [&>ul]:list-disc [&>ul]:pl-5
                                  [&>ol]:my-3 [&>ol]:list-decimal [&>ol]:pl-5">
                    <ReactMarkdown remarkPlugins={[remarkGfm]}>
                      {m.content}
                    </ReactMarkdown>
                  </div>
                </div>
              );
            }

            if (m.role === 'error') {
              return (
                <div key={i} className="text-red-600 text-sm">
                  {m.content}
                </div>
              );
            }

            // user
            return (
              <div key={i} className="flex items-start gap-2 justify-end">
                <div className="bg-gray-100 border rounded-xl px-4 py-2 text-sm">
                  {m.content}
                </div>
                <div className="bg-gray-200 p-2 rounded-full">
                  <User size={18} className="text-gray-700" />
                </div>
              </div>
            );
          })}

          {typing && (
            <div className="flex items-center gap-2 text-gray-500 text-sm">
              <Bot size={16} /> typing…
            </div>
          )}

          <div ref={endRef} />
        </div>

        {/* Input */}
        <form onSubmit={sendMessage} className="border-t p-4 flex items-center gap-3">
          <input
            value={input}
            onChange={e => setInput(e.target.value)}
            className="flex-1 border rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-200"
            placeholder="Type your message…"
          />
          <button
            disabled={!connected || !input.trim()}
            className="bg-blue-600 text-white px-4 py-2 rounded-lg text-sm font-medium hover:bg-blue-700 disabled:opacity-50"
            type="submit"
          >
            Send
          </button>
        </form>
      </div>
    </div>
  );
}
