package resh.connect.mycall.server.handler;

import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import resh.connect.mycall.common.model.Participant;
import resh.connect.mycall.common.model.RoomUpdateMessage;
import resh.connect.mycall.common.util.JsonUtils;

import java.util.*;
import java.util.concurrent.*;

/**
 * WebSocketHandler для управления подключениями и комнатами
 * с поддержкой heartbeat и оповещением участников о событиях.
 */
public class WebSocketHandler extends TextWebSocketHandler {

    private static final long HEARTBEAT_TIMEOUT_MS = 15000;

    // clientId -> ClientSession
    private final Map<String, ClientSession> clients = new ConcurrentHashMap<>();

    // roomName -> множество клиентов (сессий)
    private final Map<String, Set<ClientSession>> rooms = new ConcurrentHashMap<>();

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public WebSocketHandler() {
        // Запуск задачи проверки heartbeat каждые 5 секунд
        scheduler.scheduleAtFixedRate(this::checkHeartbeats, 0, 5, TimeUnit.SECONDS);
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        // Можно логировать подключение или делать инициализацию
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();

        // Используем JsonUtils для разбора JSON в Map
        Map<String, String> msg = JsonUtils.fromJson(payload, Map.class);

        String type = msg.get("type");
        if ("join".equals(type)) {
            String clientId = msg.get("clientId");
            String room = msg.get("room");
            joinClient(session, clientId, room);
        } else if ("heartbeat".equals(type)) {
            String clientId = msg.get("clientId");
            heartbeat(clientId);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        removeClient(session);
    }

    private void joinClient(WebSocketSession session, String clientId, String room) throws Exception {
        ClientSession client = new ClientSession(clientId, room, session);
        clients.put(clientId, client);
        rooms.computeIfAbsent(room, k -> ConcurrentHashMap.newKeySet()).add(client);
        sendRoomUpdate(room);
    }

    private void heartbeat(String clientId) {
        ClientSession client = clients.get(clientId);
        if (client != null) {
            client.setLastHeartbeat(System.currentTimeMillis());
        }
    }

    private void checkHeartbeats() {
        long now = System.currentTimeMillis();
        for (ClientSession client : new ArrayList<>(clients.values())) {
            if (now - client.getLastHeartbeat() > HEARTBEAT_TIMEOUT_MS) {
                try {
                    disconnectClient(client);
                } catch (Exception ignored) {
                }
            }
        }
    }

    private void disconnectClient(ClientSession client) throws Exception {
        clients.remove(client.getClientId());
        Set<ClientSession> roomClients = rooms.get(client.getRoom());
        if (roomClients != null) {
            roomClients.remove(client);
            sendRoomUpdate(client.getRoom());
        }
        client.getSession().close();
    }

    private void removeClient(WebSocketSession session) {
        Optional<ClientSession> toRemove = clients.values().stream()
                .filter(c -> c.getSession().equals(session))
                .findFirst();
        toRemove.ifPresent(client -> {
            clients.remove(client.getClientId());
            Set<ClientSession> roomClients = rooms.get(client.getRoom());
            if (roomClients != null) {
                roomClients.remove(client);
                try {
                    sendRoomUpdate(client.getRoom());
                } catch (Exception ignored) {
                }
            }
        });
    }

    private void sendRoomUpdate(String room) throws Exception {
        Set<ClientSession> roomClients = rooms.get(room);
        if (roomClients == null) return;

        List<Participant> participants = new ArrayList<>();
        for (ClientSession c : roomClients) {
            participants.add(new Participant(c.getClientId(), false)); // micOn можно доработать
        }

        RoomUpdateMessage message = new RoomUpdateMessage(participants);

        // Используем JsonUtils для сериализации сообщения
        String jsonMessage = JsonUtils.toJson(message);

        for (ClientSession client : roomClients) {
            client.getSession().sendMessage(new TextMessage(jsonMessage));
        }
    }

    private static class ClientSession {
        private final String clientId;
        private final String room;
        private final WebSocketSession session;
        private volatile long lastHeartbeat;

        public ClientSession(String clientId, String room, WebSocketSession session) {
            this.clientId = clientId;
            this.room = room;
            this.session = session;
            this.lastHeartbeat = System.currentTimeMillis();
        }

        public String getClientId() {
            return clientId;
        }

        public String getRoom() {
            return room;
        }

        public WebSocketSession getSession() {
            return session;
        }

        public long getLastHeartbeat() {
            return lastHeartbeat;
        }

        public void setLastHeartbeat(long ts) {
            this.lastHeartbeat = ts;
        }
    }
}
