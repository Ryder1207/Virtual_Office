package com.example.virtual_office.controller;

public class AuthRequest {
    private String address;    // 錢包地址
    private String message;    // 簽名的原始訊息
    private String signature;  // 簽名後的結果 (Hex)

    // Getter 與 Setter (重要！Spring 需要這些來注入資料)
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getSignature() { return signature; }
    public void setSignature(String signature) { this.signature = signature; }
}