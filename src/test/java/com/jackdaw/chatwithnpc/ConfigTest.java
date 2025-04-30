package com.jackdaw.chatwithnpc;

public class ConfigTest {
    public static void setOllamaConfig() {
        SettingManager.apiURL = "http://192.168.122.74:11434";
        SettingManager.chat_model = "qwen3:8b";
    }
}
