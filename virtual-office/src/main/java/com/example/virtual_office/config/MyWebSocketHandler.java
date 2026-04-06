package com.example.virtual_office.config;

import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class MyWebSocketHandler extends TextWebSocketHandler {
    
    // 關鍵修改：使用 Map 管理房間。Key = roomID, Value = 該房間的所有連線
    private static final Map<String, CopyOnWriteArrayList<WebSocketSession>> roomMap = new ConcurrentHashMap<>();

    // 當新連線建立時
    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        String roomID = getRoomId(session); // 取得網址參數中的 room
        
        // 如果房間還不存在，就開一個新的清單；如果存在，就加入
        roomMap.computeIfAbsent(roomID, k -> new CopyOnWriteArrayList<>()).add(session);
        
        // 把房間 ID 存入 session 的自定義屬性中，方便之後讀取
        session.getAttributes().put("roomID", roomID);

        System.out.println("🏠 房間 [" + roomID + "] 新夥伴加入！ID: " + session.getId());
    }

    // 核心邏輯：只廣播給「同一個房間」的人
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String roomID = (String) session.getAttributes().get("roomID");
        
        if (roomID != null && roomMap.containsKey(roomID)) {
            CopyOnWriteArrayList<WebSocketSession> roomSessions = roomMap.get(roomID);
            
            for (WebSocketSession s : roomSessions) {
                if (s.isOpen()) {
                    s.sendMessage(message);
                }
            }
        }
    }

    // 當連線關閉時
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String roomID = (String) session.getAttributes().get("roomID");
        
        if (roomID != null && roomMap.containsKey(roomID)) {
            roomMap.get(roomID).remove(session);
            
            // 如果房間沒人了，就把房間刪掉節省記憶體
            if (roomMap.get(roomID).isEmpty()) {
                roomMap.remove(roomID);
            }
        }
        System.out.println("夥伴離開了房間 [" + roomID + "]");
    }

    // 輔助方法：解析 URI 中的 room 參數
    private String getRoomId(WebSocketSession session) {
        try {
            String query = session.getUri().getQuery(); // 取得 "room=Lobby" 這種字串
            if (query != null && query.contains("room=")) {
                // 簡單切分字串取得 room 名稱
                return query.split("room=")[1].split("&")[0];
            }
        } catch (Exception e) {
            System.err.println("解析房間 ID 失敗，預設進入 Lobby");
        }
        return "Lobby";
    }
}