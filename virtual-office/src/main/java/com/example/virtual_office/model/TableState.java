package com.example.virtual_office.model;

import java.util.ArrayList;
import java.util.List;

public class TableState {
    private String id;
    private String type;
    private int maxSlots;
    private List<ToolInfo> tools = new ArrayList<>();

    // 必須有這兩個內部屬性的 Getter，Jackson 才能解析
    public static class ToolInfo {
        public String name;
        public String url;
        public String addedBy;
        
        // 為了保險，建議補上 getter
        public String getName() { return name; }
        public String getUrl() { return url; }
    }

    public TableState(String id, String type, int maxSlots) {
        this.id = id;
        this.type = type;
        this.maxSlots = maxSlots;
    }

    // --- 必須補上這些 Getter，否則 ObjectMapper 會報錯 ---
    public String getId() { return id; }
    public String getType() { return type; }
    public int getMaxSlots() { return maxSlots; }
    public List<ToolInfo> getTools() { return tools; }
}