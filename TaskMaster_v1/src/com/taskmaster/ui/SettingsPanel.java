package com.taskmaster.ui;

import com.taskmaster.service.TaskService;
import com.taskmaster.util.AppTheme;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;

public class SettingsPanel extends JPanel {
    private final TaskService taskService;

    public SettingsPanel(TaskService taskService) {
        this.taskService = taskService;
        setBackground(AppTheme.BG_PRIMARY);
        setLayout(new BorderLayout());
        buildUI();
    }

    private void buildUI() {
        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(AppTheme.BG_SECONDARY);
        header.setBorder(new CompoundBorder(
            new MatteBorder(0, 0, 1, 0, AppTheme.BORDER),
            BorderFactory.createEmptyBorder(14, 20, 14, 20)
        ));
        JLabel title = new JLabel("SETTINGS");
        title.setFont(AppTheme.FONT_HEADER);
        title.setForeground(AppTheme.TEXT_PRIMARY);
        header.add(title, BorderLayout.WEST);
        add(header, BorderLayout.NORTH);

        // Content
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(AppTheme.BG_PRIMARY);
        content.setBorder(BorderFactory.createEmptyBorder(32, 40, 32, 40));

        // --- Data Management Section ---
        JLabel sectionLabel = new JLabel("Data Management");
        sectionLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        sectionLabel.setForeground(AppTheme.TEXT_PRIMARY);
        sectionLabel.setAlignmentX(LEFT_ALIGNMENT);
        content.add(sectionLabel);
        content.add(Box.createVerticalStrut(6));

        JLabel sectionDesc = new JLabel("Manage your task history and stored data.");
        sectionDesc.setFont(AppTheme.FONT_SMALL);
        sectionDesc.setForeground(AppTheme.TEXT_MUTED);
        sectionDesc.setAlignmentX(LEFT_ALIGNMENT);
        content.add(sectionDesc);
        content.add(Box.createVerticalStrut(28));

        // Option 1: Clear completed from chart (calendar keeps them)
        content.add(buildSettingCard(
            "Clear Completed Task History",
            "Permanently removes all completed tasks from Calendar history too. Use this if you want to wipe old completed task records from the calendar.",
            "Clear History",
            new Color(230, 140, 0),
            e -> confirmAndClearHistory()
        ));
        content.add(Box.createVerticalStrut(16));

        // Option 2: Reset everything
        content.add(buildSettingCard(
            "Reset Everything",
            "Permanently delete ALL tasks — both active and completed. This cannot be undone. Use this to start completely fresh.",
            "Reset All Data",
            new Color(220, 60, 60),
            e -> confirmAndResetAll()
        ));

        content.add(Box.createVerticalGlue());

        // Wrap in scroll
        JScrollPane scroll = new JScrollPane(content);
        scroll.setBorder(null);
        scroll.setBackground(AppTheme.BG_PRIMARY);
        scroll.getViewport().setBackground(AppTheme.BG_PRIMARY);
        scroll.getVerticalScrollBar().setPreferredSize(new Dimension(6, 0));
        add(scroll, BorderLayout.CENTER);
    }

    private JPanel buildSettingCard(String title, String description, String buttonText, Color dangerColor, java.awt.event.ActionListener action) {
        JPanel card = new JPanel(new BorderLayout(16, 0)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(AppTheme.BG_CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(new Color(dangerColor.getRed(), dangerColor.getGreen(), dangerColor.getBlue(), 60));
                g2.setStroke(new java.awt.BasicStroke(1.5f));
                g2.drawRoundRect(1, 1, getWidth()-2, getHeight()-2, 12, 12);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        card.setAlignmentX(LEFT_ALIGNMENT);

        JPanel textArea = new JPanel();
        textArea.setLayout(new BoxLayout(textArea, BoxLayout.Y_AXIS));
        textArea.setOpaque(false);

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titleLbl.setForeground(AppTheme.TEXT_PRIMARY);
        titleLbl.setAlignmentX(LEFT_ALIGNMENT);

        JLabel descLbl = new JLabel("<html><body style='width:380px'>" + description + "</body></html>");
        descLbl.setFont(AppTheme.FONT_SMALL);
        descLbl.setForeground(AppTheme.TEXT_MUTED);
        descLbl.setAlignmentX(LEFT_ALIGNMENT);

        textArea.add(titleLbl);
        textArea.add(Box.createVerticalStrut(4));
        textArea.add(descLbl);

        JButton btn = new JButton(buttonText) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isPressed()
                    ? dangerColor.darker()
                    : (getModel().isRollover() ? dangerColor : new Color(dangerColor.getRed(), dangerColor.getGreen(), dangerColor.getBlue(), 200)));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setForeground(Color.WHITE);
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorder(BorderFactory.createEmptyBorder(8, 18, 8, 18));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(action);

        card.add(textArea, BorderLayout.CENTER);
        card.add(btn, BorderLayout.EAST);
        return card;
    }

    private void confirmAndClearHistory() {
        int result = JOptionPane.showConfirmDialog(
            this,
            "<html><b>Clear all completed task history?</b><br><br>" +
            "Completed tasks will be removed from your calendar history.<br>" +
            "Active tasks will not be affected.<br><br>" +
            "<font color='orange'>This cannot be undone.</font></html>",
            "Clear History",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );
        if (result == JOptionPane.YES_OPTION) {
            taskService.clearCompletedHistory();
            showSuccessToast("Completed task history cleared.");
        }
    }

    private void confirmAndResetAll() {
        int result = JOptionPane.showConfirmDialog(
            this,
            "<html><b>Reset ALL data?</b><br><br>" +
            "This will permanently delete EVERY task — active and completed.<br>" +
            "Your calendar will be completely cleared.<br><br>" +
            "<font color='red'><b>This CANNOT be undone.</b></font></html>",
            "Reset Everything",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.ERROR_MESSAGE
        );
        if (result == JOptionPane.YES_OPTION) {
            // Double-confirm for destructive action
            int confirm2 = JOptionPane.showConfirmDialog(
                this,
                "<html>Are you absolutely sure?<br>All your tasks will be permanently deleted.</html>",
                "Final Confirmation",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.ERROR_MESSAGE
            );
            if (confirm2 == JOptionPane.YES_OPTION) {
                taskService.resetAll();
                showSuccessToast("All data has been reset.");
            }
        }
    }

    private void showSuccessToast(String message) {
        JDialog toast = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), false);
        toast.setUndecorated(true);

        JPanel panel = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(50, 50, 60, 240));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.dispose();
            }
        };
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));

        JLabel lbl = new JLabel("✔  " + message);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lbl.setForeground(AppTheme.ACCENT_GREEN);
        panel.add(lbl, BorderLayout.CENTER);

        toast.add(panel);
        toast.pack();

        Window win = SwingUtilities.getWindowAncestor(this);
        if (win != null) {
            toast.setLocation(win.getX() + win.getWidth() / 2 - toast.getWidth() / 2,
                              win.getY() + win.getHeight() - 100);
        }
        toast.setVisible(true);
        Timer t = new Timer(2200, e -> toast.dispose());
        t.setRepeats(false);
        t.start();
    }
}
