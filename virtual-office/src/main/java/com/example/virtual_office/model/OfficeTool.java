package com.example.virtual_office.model;

public class OfficeTool {
    private String id;        // 例如 "desk_1"
    private String type;      // "GOOGLE_DOC", "WHITEBOARD"
    private String currentUrl; // 目前開啟的網址
    private String owner;      // 誰目前正在編輯（Web3 位址）
    private boolean isLocked;  // 是否被鎖定（防止多人同時改網址
}
