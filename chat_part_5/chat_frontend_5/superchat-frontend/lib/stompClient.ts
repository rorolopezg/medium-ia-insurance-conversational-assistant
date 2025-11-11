import { Client } from '@stomp/stompjs';

export type StompCallbacks = {
  onConnect?: () => void;
  onDisconnect?: () => void;
  onError?: (e: any) => void;
};

export function createStompClient(url: string, cbs: StompCallbacks) {
  const client = new Client({
    brokerURL: url,
    reconnectDelay: 3000,
    heartbeatIncoming: 10000,
    heartbeatOutgoing: 10000,
    onConnect: () => cbs.onConnect?.(),
    onStompError: frame => cbs.onError?.(frame),
    onWebSocketClose: () => cbs.onDisconnect?.(),
    debug: () => {}, // silenciar
  });
  return client;
}
