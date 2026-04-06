package com.example.virtual_office.config;

import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import com.example.virtual_office.model.TableState;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class MyWebSocketHandler extends TextWebSocketHandler {
    
    // 用於處理 JSON 解析
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    // 管理連線
    private static final Map<String, CopyOnWriteArrayList<WebSocketSession>> roomMap = new ConcurrentHashMap<>();
    
    // 管理桌子狀態 (桌子 ID -> 桌子狀態)
    private final Map<String, TableState> tables = new ConcurrentHashMap<>();

    // 建構子：在 Handler 啟動時就初始化桌子
    public MyWebSocketHandler() {
        initTables();
    }

    private void initTables() {
        tables.put("desk_1", new TableState("desk_1", "WORK", 1));
        tables.put("desk_2", new TableState("desk_2", "WORK", 1));
        tables.put("meeting_main", new TableState("meeting_main", "MEETING", 5));
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String roomID = getRoomId(session);
        roomMap.computeIfAbsent(roomID, k -> new CopyOnWriteArrayList<>()).add(session);
        session.getAttributes().put("roomID", roomID);

        // --- 關鍵加分項：新玩家進來時，立刻把目前的桌子狀態發送給他 ---
        String currentTablesMsg = objectMapper.writeValueAsString(Map.of(
            "type", "INITIAL_TABLES",
            "tables", tables
        ));
        session.sendMessage(new TextMessage(currentTablesMsg));

        System.out.println("🏠 房間 [" + roomID + "] 新夥伴加入！");
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        JsonNode root = objectMapper.readTree(message.getPayload());
        String type = root.has("type") ? root.get("type").asText() : "";
        String roomID = (String) session.getAttributes().get("roomID");

        // 1. 處理「新增工具到桌上」的邏輯
        if ("ADD_TOOL".equals(type)) {
            String tableId = root.get("tableId").asText();
            TableState table = tables.get(tableId);

            if (table != null && table.getTools().size() < table.getMaxSlots()) {
                TableState.ToolInfo newTool = new TableState.ToolInfo();
                newTool.name = root.get("toolName").asText();
                newTool.url = root.get("url").asText();
                // 這裡之後可以加入 Web3 位址紀錄 newTool.addedBy = ...
                table.getTools().add(newTool);

                // 準備桌子更新的廣播訊息
                String updateMsg = objectMapper.writeValueAsString(Map.of(
                    "type", "TABLE_UPDATE",
                    "tableId", tableId,
                    "tools", table.getTools()
                ));
                broadcastToRoom(roomID, updateMsg);
            }
        } 
        // 2. 處理原本的角色移動、聊天等廣播 (原封不動轉發)
        else {
            broadcastToRoom(roomID, message.getPayload());
        }
    }

    // 封裝一個只廣播給同房間的輔助方法
    private void broadcastToRoom(String roomID, String payload) throws IOException {
        if (roomID != null && roomMap.containsKey(roomID)) {
            TextMessage textMessage = new TextMessage(payload);
            for (WebSocketSession s : roomMap.get(roomID)) {
                if (s.isOpen()) {
                    s.sendMessage(textMessage);
                }
            }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String roomID = (String) session.getAttributes().get("roomID");
        if (roomID != null && roomMap.containsKey(roomID)) {
            roomMap.get(roomID).remove(session);
            if (roomMap.get(roomID).isEmpty()) {
                roomMap.remove(roomID);
            }
        }
    }

    private String getRoomId(WebSocketSession session) {
        try {
            String query = session.getUri().getQuery();
            if (query != null && query.contains("room=")) {
                return query.split("room=")[1].split("&")[0];
            }
        } catch (Exception e) {}
        return "Lobby";
    }
}