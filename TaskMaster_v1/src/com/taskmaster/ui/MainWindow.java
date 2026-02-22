package com.taskmaster.ui;

import com.taskmaster.service.TaskService;
import com.taskmaster.util.AppTheme;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;

public class MainWindow extends JFrame {
    private final TaskService taskService;
    private JPanel contentPanel;
    private CardLayout cardLayout;
    private JButton[] navButtons;
    private static final String[] TAB_NAMES = {"CALENDAR", "TASK CHART", "MANAGE TASKS", "SETTINGS"};
    

    public MainWindow(TaskService taskService) {
        this.taskService = taskService;
        setupFrame();
        buildUI();
        setVisible(true);
    }

    private void setupFrame() {
        setTitle("TaskMaster — Your Personal Task Manager");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 720);
        setMinimumSize(new Dimension(900, 600));
        setLocationRelativeTo(null);

        // Try to set a nice icon
        try {
            ImageIcon icon = new ImageIcon(new byte[0]);
            setIconImage(icon.getImage());
        } catch (Exception ignored) {}

        // Dark title bar on Windows
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        getContentPane().setBackground(AppTheme.BG_PRIMARY);
    }

    private void buildUI() {
        setLayout(new BorderLayout(0, 0));

        // Sidebar navigation
        JPanel sidebar = buildSidebar();
        add(sidebar, BorderLayout.WEST);

        // Content area
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(AppTheme.BG_PRIMARY);

        contentPanel.add(new CalendarPanel(taskService), "CALENDAR");
        contentPanel.add(new TaskListPanel(taskService), "TASK CHART");
        contentPanel.add(new TaskManagementPanel(taskService), "MANAGE TASKS");
        contentPanel.add(new SettingsPanel(taskService), "SETTINGS");

        add(contentPanel, BorderLayout.CENTER);

        // Activate first tab
        switchTab(0);
    }

    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(AppTheme.BG_SECONDARY);
        sidebar.setBorder(new MatteBorder(0, 0, 0, 1, AppTheme.BORDER));
        sidebar.setPreferredSize(new Dimension(230, 0));

        // App logo/title
        JPanel logoPanel = new JPanel(new BorderLayout());
        logoPanel.setBackground(AppTheme.BG_SECONDARY);
        logoPanel.setBorder(new CompoundBorder(
            new MatteBorder(0, 0, 1, 0, AppTheme.BORDER),
            BorderFactory.createEmptyBorder(20, 18, 20, 18)
        ));
        logoPanel.setMaximumSize(new Dimension(230, 80));

        JLabel logo = new JLabel("✦  TaskMaster");
        logo.setFont(new Font("Segoe UI Symbol", Font.BOLD, 16));
        logo.setForeground(AppTheme.TEXT_PRIMARY);
        logoPanel.add(logo, BorderLayout.CENTER);

        JLabel version = new JLabel("v1.0");
        version.setFont(AppTheme.FONT_BADGE);
        version.setForeground(AppTheme.TEXT_MUTED);
        logoPanel.add(version, BorderLayout.EAST);

        sidebar.add(logoPanel);
        sidebar.add(Box.createVerticalStrut(16));

        // Nav label
        JLabel navLabel = new JLabel("  NAVIGATION");
        navLabel.setFont(AppTheme.FONT_BADGE);
        navLabel.setForeground(AppTheme.TEXT_MUTED);
        navLabel.setAlignmentX(LEFT_ALIGNMENT);
        navLabel.setBorder(BorderFactory.createEmptyBorder(0, 18, 8, 0));
        navLabel.setMaximumSize(new Dimension(230, 24));
        sidebar.add(navLabel);

        // Nav buttons
        navButtons = new JButton[TAB_NAMES.length];
        for (int i = 0; i < TAB_NAMES.length; i++) {
            if (i == 3) {
                // Add spacer before settings
                sidebar.add(Box.createVerticalStrut(8));
                JLabel settingsLabel = new JLabel("  PREFERENCES");
                settingsLabel.setFont(AppTheme.FONT_BADGE);
                settingsLabel.setForeground(AppTheme.TEXT_MUTED);
                settingsLabel.setAlignmentX(LEFT_ALIGNMENT);
                settingsLabel.setBorder(BorderFactory.createEmptyBorder(0, 18, 8, 0));
                settingsLabel.setMaximumSize(new Dimension(230, 24));
                sidebar.add(settingsLabel);
            }
            navButtons[i] = createNavButton((i == 3 ? "⚙  " : "•  ") + TAB_NAMES[i], i);
            sidebar.add(navButtons[i]);
            sidebar.add(Box.createVerticalStrut(4));
        }

        sidebar.add(Box.createVerticalGlue());

        // Footer
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(AppTheme.BG_SECONDARY);
        footer.setBorder(new CompoundBorder(
            new MatteBorder(1, 0, 0, 0, AppTheme.BORDER),
            BorderFactory.createEmptyBorder(14, 18, 14, 18)
        ));
        footer.setMaximumSize(new Dimension(230, 60));

        JLabel footerText = new JLabel("Tasks saved locally");
        footerText.setFont(AppTheme.FONT_SMALL);
        footerText.setForeground(AppTheme.TEXT_MUTED);
        footer.add(footerText, BorderLayout.CENTER);

        sidebar.add(footer);

        return sidebar;
    }

    private JButton createNavButton(String text, int index) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getBackground() != AppTheme.BG_SECONDARY) {
                    g2.setColor(getBackground());
                    g2.fillRoundRect(4, 2, getWidth()-8, getHeight()-4, 8, 8);
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 13));
        btn.setForeground(AppTheme.TEXT_SECONDARY);
        btn.setBackground(AppTheme.BG_SECONDARY);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setOpaque(false);
        btn.setBorder(BorderFactory.createEmptyBorder(10, 18, 10, 18));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setMaximumSize(new Dimension(230, 42));
        btn.setAlignmentX(LEFT_ALIGNMENT);

        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                if (!isActiveTab(index)) btn.setBackground(AppTheme.BG_HOVER);
            }
            public void mouseExited(MouseEvent e) {
                if (!isActiveTab(index)) btn.setBackground(AppTheme.BG_SECONDARY);
            }
        });
        btn.addActionListener(e -> switchTab(index));
        return btn;
    }

    private boolean isActiveTab(int index) {
        return navButtons[index].getBackground().equals(AppTheme.BG_SELECTED);
    }

    private void switchTab(int index) {
        cardLayout.show(contentPanel, TAB_NAMES[index]);
        for (int i = 0; i < navButtons.length; i++) {
            if (i == index) {
                navButtons[i].setFont(new Font("Segoe UI Symbol", Font.BOLD, 13));
                navButtons[i].setForeground(AppTheme.ACCENT_BLUE);
                navButtons[i].setBackground(new Color(88, 166, 255, 15));
            } else {
                navButtons[i].setFont(new Font("Segoe UI Symbol", Font.PLAIN, 13));
                navButtons[i].setForeground(AppTheme.TEXT_SECONDARY);
                navButtons[i].setBackground(AppTheme.BG_SECONDARY);
            }
            navButtons[i].repaint();
        }
    }
}
