package com.example.virtual_office.service;

import org.springframework.stereotype.Service;
import org.web3j.crypto.Keys;
import org.web3j.crypto.Sign;
import org.web3j.utils.Numeric;

import java.math.BigInteger;
import java.util.Arrays;

@Service
public class Web3Service {

    public boolean verifySignature(String address, String message, String signature) {
        try {
            // 1. 強制使用 UTF-8 取得原始訊息的 byte array
            byte[] messageBytes = message.getBytes(java.nio.charset.StandardCharsets.UTF_8);

            // 2. 建立以太坊前綴 (注意：長度必須是 messageBytes.length 而不是 message.length())
            String prefix = "\u0019Ethereum Signed Message:\n" + messageBytes.length;
            byte[] prefixBytes = prefix.getBytes(java.nio.charset.StandardCharsets.UTF_8);

            // 3. 合併前綴與訊息
            byte[] totalMsg = new byte[prefixBytes.length + messageBytes.length];
            System.arraycopy(prefixBytes, 0, totalMsg, 0, prefixBytes.length);
            System.arraycopy(messageBytes, 0, totalMsg, prefixBytes.length, messageBytes.length);

            // 4. 解析簽名 (r, s, v)
            byte[] signatureBytes = Numeric.hexStringToByteArray(signature);
            
            // 處理 v 的偏移量
            byte v = signatureBytes[64];
            if (v < 27) v += 27;

            byte[] r = Arrays.copyOfRange(signatureBytes, 0, 32);
            byte[] s = Arrays.copyOfRange(signatureBytes, 32, 64);

            Sign.SignatureData sd = new Sign.SignatureData(v, r, s);

            // 5. 还原公鑰
            BigInteger publicKey = Sign.signedMessageToKey(totalMsg, sd);
            String recoveredAddress = "0x" + Keys.getAddress(publicKey);

            System.out.println("還原地址: " + recoveredAddress);
            System.out.println("宣稱地址: " + address);

            return recoveredAddress.equalsIgnoreCase(address);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}