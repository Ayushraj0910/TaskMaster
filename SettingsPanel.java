package com.taskmaster;

import com.taskmaster.service.TaskService;
import com.taskmaster.ui.MainWindow;
import com.taskmaster.util.AppTheme;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.net.URL;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            applyGlobalLookAndFeel();
            TaskService taskService = new TaskService();
            MainWindow window = new MainWindow(taskService);

            try {
                URL iconUrl = Main.class.getResource("/taskmaster_icon.png");
                if (iconUrl != null) {
                    Image icon = Toolkit.getDefaultToolkit().getImage(iconUrl);
                    window.setIconImage(icon);
                }
            } catch (Exception ignored) {}
        });
    }

    private static void applyGlobalLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        Color BG   = AppTheme.BG_SECONDARY;
        Color CARD = AppTheme.BG_CARD;
        Color HOVER= AppTheme.BG_HOVER;
        Color FG   = AppTheme.TEXT_PRIMARY;
        Color MUTED= AppTheme.TEXT_SECONDARY;
        Color BORDER= AppTheme.BORDER;
        Color BLUE = AppTheme.ACCENT_BLUE;

        // ── Panels & general ────────────────────────────────────────────────
        UIManager.put("Panel.background",           AppTheme.BG_PRIMARY);
        UIManager.put("Label.foreground",           FG);
        UIManager.put("SplitPane.background",       AppTheme.BG_PRIMARY);
        UIManager.put("SplitPaneDivider.background",BORDER);

        // ── Buttons (covers JOptionPane Yes/No/OK/Cancel) ───────────────────
        UIManager.put("Button.background",          CARD);
        UIManager.put("Button.foreground",          FG);
        UIManager.put("Button.select",              HOVER);
        UIManager.put("Button.focus",               new Color(0,0,0,0));
        UIManager.put("Button.border", new CompoundBorder(
            new LineBorder(BORDER, 1, true),
            BorderFactory.createEmptyBorder(6, 16, 6, 16)
        ));
        UIManager.put("Button.font",
            new Font("Segoe UI", Font.BOLD, 13));

        // ── OptionPane (dialogs: Delete confirm, error, etc.) ───────────────
        UIManager.put("OptionPane.background",              BG);
        UIManager.put("OptionPane.messageForeground",       FG);
        UIManager.put("OptionPane.messageFont",
            new Font("Segoe UI", Font.PLAIN, 13));
        UIManager.put("OptionPane.buttonFont",
            new Font("Segoe UI", Font.BOLD, 13));
        // The panel that holds the buttons inside OptionPane
        UIManager.put("OptionPane.buttonAreaBorder",
            BorderFactory.createEmptyBorder(8, 0, 4, 0));

        // ── Text fields ──────────────────────────────────────────────────────
        UIManager.put("TextField.background",       CARD);
        UIManager.put("TextField.foreground",       FG);
        UIManager.put("TextField.caretForeground",  FG);
        UIManager.put("TextField.selectionBackground", BLUE);
        UIManager.put("TextField.selectionForeground", Color.WHITE);
        UIManager.put("TextField.border", new CompoundBorder(
            new LineBorder(BORDER, 1),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));

        // ── ComboBox (dropdowns — the white-background ones) ─────────────────
        UIManager.put("ComboBox.background",        CARD);
        UIManager.put("ComboBox.foreground",        FG);
        UIManager.put("ComboBox.selectionBackground", HOVER);
        UIManager.put("ComboBox.selectionForeground", FG);
        UIManager.put("ComboBox.buttonBackground",  CARD);
        UIManager.put("ComboBox.disabledBackground",CARD);
        UIManager.put("ComboBox.border",            new LineBorder(BORDER, 1));
        UIManager.put("ComboBox.font",
            new Font("Segoe UI", Font.PLAIN, 13));

        // ── Dropdown popup list ──────────────────────────────────────────────
        UIManager.put("List.background",            CARD);
        UIManager.put("List.foreground",            FG);
        UIManager.put("List.selectionBackground",   HOVER);
        UIManager.put("List.selectionForeground",   FG);

        // ── PopupMenu (combobox dropdown container) ──────────────────────────
        UIManager.put("PopupMenu.background",       CARD);
        UIManager.put("PopupMenu.foreground",       FG);
        UIManager.put("PopupMenu.border",           new LineBorder(BORDER, 1));
        UIManager.put("MenuItem.background",        CARD);
        UIManager.put("MenuItem.foreground",        FG);
        UIManager.put("MenuItem.selectionBackground", HOVER);
        UIManager.put("MenuItem.selectionForeground", FG);

        // ── CheckBox ────────────────────────────────────────────────────────
        UIManager.put("CheckBox.background",        CARD);
        UIManager.put("CheckBox.foreground",        FG);

        // ── ToggleButton ─────────────────────────────────────────────────────
        UIManager.put("ToggleButton.background",    CARD);
        UIManager.put("ToggleButton.foreground",    FG);
        UIManager.put("ToggleButton.select",        HOVER);
        UIManager.put("ToggleButton.font",
            new Font("Segoe UI", Font.BOLD, 12));

        // ── ScrollPane / ScrollBar ───────────────────────────────────────────
        UIManager.put("ScrollPane.background",      AppTheme.BG_PRIMARY);
        UIManager.put("Viewport.background",        AppTheme.BG_PRIMARY);
        UIManager.put("ScrollBar.background",       AppTheme.BG_SECONDARY);
        UIManager.put("ScrollBar.thumb",            AppTheme.BG_HOVER);
        UIManager.put("ScrollBar.thumbHighlight",   AppTheme.BG_HOVER);
        UIManager.put("ScrollBar.thumbDarkShadow",  AppTheme.BG_SECONDARY);
        UIManager.put("ScrollBar.thumbShadow",      AppTheme.BG_SECONDARY);
        UIManager.put("ScrollBar.track",            AppTheme.BG_SECONDARY);
        UIManager.put("ScrollBar.trackHighlight",   AppTheme.BG_SECONDARY);
    }
}
