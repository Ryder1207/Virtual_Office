package com.example.virtual_office.controller;

import com.example.virtual_office.service.Web3Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private Web3Service web3Service;

    @PostMapping("/verify")
    public boolean verify(@RequestBody AuthRequest req) {
        // 呼叫 Web3Service 進行核心驗證
        boolean isValid = web3Service.verifySignature(
            req.getAddress(), 
            req.getMessage(), 
            req.getSignature()
        );

        if (isValid) {
            System.out.println("✅ 驗證成功: " + req.getAddress());
        } else {
            System.out.println("❌ 驗證失敗!");
        }

        return isValid;
    }
}