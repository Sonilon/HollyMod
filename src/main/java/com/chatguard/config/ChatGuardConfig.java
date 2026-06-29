package com.chatguard.config;

import com.google.gson.*;
import net.fabricmc.loader.api.FabricLoader;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class ChatGuardConfig {

    private static final Path CONFIG_PATH = FabricLoader.getInstance()
            .getConfigDir().resolve("chatguard.json");

    private static ChatGuardConfig INSTANCE = new ChatGuardConfig();

    public boolean soundEnabled = true;
    public float soundVolume = 1.0f;
    public String muteCommand = "/mute {nick} {time} {rule} | {reason}";

    // Telegram настройки
    public String telegramBotToken = "";
    public String telegramChatId = "";
    public boolean telegramEnabled = false;

    public List<TriggerCategory> categories = new ArrayList<>();

    public static class TriggerCategory {
        public String name = "";
        public String command = "/mute {nick} {time} {rule} | {reason}";
        public String time = "60m";
        public String rule = "3.10";
        public String reason = "Нарушение правил";
        public boolean enabled = true;
        public List<String> words = new ArrayList<>();

        public TriggerCategory() {}

        public TriggerCategory(String name, String time, String rule, String reason, String... words) {
            this.name   = name;
            this.time   = time;
            this.rule   = rule;
            this.reason = reason;
            this.words.addAll(Arrays.asList(words));
        }
    }

    public static ChatGuardConfig getInstance() { return INSTANCE; }

    public static void load() {
        if (Files.exists(CONFIG_PATH)) {
            try (Reader r = new FileReader(CONFIG_PATH.toFile())) {
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                INSTANCE = gson.fromJson(r, ChatGuardConfig.class);
                if (INSTANCE == null) INSTANCE = new ChatGuardConfig();
                if (INSTANCE.categories == null) INSTANCE.categories = new ArrayList<>();
            } catch (Exception e) {
                System.err.println("[ChatGuard] Ошибка загрузки конфига: " + e.getMessage());
                INSTANCE = new ChatGuardConfig();
                addDefaultCategories(INSTANCE);
            }
        } else {
            INSTANCE = new ChatGuardConfig();
            addDefaultCategories(INSTANCE);
            save();
        }
    }

    private static void addDefaultCategories(ChatGuardConfig cfg) {
        TriggerCategory osk = new TriggerCategory(
            "Оскорбление игроков", "60m", "3.10", "Оскорбление игроков.",
            "идиот","тварь","даун","хуесос","далбаеб","долбаеб","далбаёб","долбаёб",
            "еблан","педик","пидарас","придурок","уебан","пидор","уебище","хуйло",
            "гандон","додик","конченный","конченый","хуила","хуеглот","ебанный",
            "ебаный","ебаные","ебанные","хуисос","долбоеб"
        );
        TriggerCategory amor = new TriggerCategory(
            "Аморальное поведение", "60m", "3.3", "Аморальное поведение.",
            "soso","сосо","sosi","соси","дососал","отсосал","otsosal",
            "выебан","выебал","соснул","сосешь","трахнул","sosnyl"
        );
        TriggerCategory rodnya = new TriggerCategory(
            "Оскорбление родных", "180m", "3.7", "Упом. родных или близких.",
            "сын","сынок","мамку","матуху","батька"
        );
        cfg.categories.add(osk);
        cfg.categories.add(amor);
        cfg.categories.add(rodnya);
    }

    public static void save() {
        try {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            Files.writeString(CONFIG_PATH, gson.toJson(INSTANCE));
        } catch (Exception e) {
            System.err.println("[ChatGuard] Ошибка сохранения конфига: " + e.getMessage());
        }
    }
}
