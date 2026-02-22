package com.taskmaster.util;

import java.awt.*;

public class AppTheme {
    // Matte Black Palette
    public static final Color BG_PRIMARY     = new Color(18, 18, 18);
    public static final Color BG_SECONDARY   = new Color(28, 28, 28);
    public static final Color BG_CARD        = new Color(36, 36, 36);
    public static final Color BG_HOVER       = new Color(48, 48, 48);
    public static final Color BG_SELECTED    = new Color(55, 55, 55);
    public static final Color BORDER         = new Color(60, 60, 60);
    public static final Color BORDER_SUBTLE  = new Color(42, 42, 42);

    // Text
    public static final Color TEXT_PRIMARY   = new Color(240, 240, 240);
    public static final Color TEXT_SECONDARY = new Color(160, 160, 160);
    public static final Color TEXT_MUTED     = new Color(100, 100, 100);
    public static final Color TEXT_DISABLED  = new Color(70, 70, 70);

    // Accent Colors
    public static final Color ACCENT_BLUE    = new Color(88, 166, 255);
    public static final Color ACCENT_PURPLE  = new Color(180, 120, 255);
    public static final Color ACCENT_GREEN   = new Color(60, 210, 130);
    public static final Color ACCENT_ORANGE  = new Color(255, 170, 60);
    public static final Color ACCENT_RED     = new Color(255, 90, 90);
    public static final Color ACCENT_CYAN    = new Color(60, 210, 220);

    // Priority Colors
    public static final Color PRIORITY_LOW      = new Color(60, 210, 130);
    public static final Color PRIORITY_MEDIUM   = new Color(88, 166, 255);
    public static final Color PRIORITY_HIGH     = new Color(255, 170, 60);
    public static final Color PRIORITY_CRITICAL = new Color(255, 90, 90);

    // Fonts
    public static final Font FONT_TITLE   = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font FONT_HEADER  = new Font("Segoe UI", Font.BOLD, 15);
    public static final Font FONT_BODY    = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_SMALL   = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font FONT_MONO    = new Font("Consolas", Font.PLAIN, 12);
    public static final Font FONT_BADGE   = new Font("Segoe UI", Font.BOLD, 10);

    public static Color getPriorityColor(com.taskmaster.model.Task.Priority p) {
        return switch (p) {
            case LOW -> PRIORITY_LOW;
            case MEDIUM -> PRIORITY_MEDIUM;
            case HIGH -> PRIORITY_HIGH;
            case CRITICAL -> PRIORITY_CRITICAL;
        };
    }

    public static String getPriorityIcon(com.taskmaster.model.Task.Priority p) {
        return switch (p) {
            case LOW -> "v";
            case MEDIUM -> "-";
            case HIGH -> "^";
            case CRITICAL -> "!";
        };
    }
}
