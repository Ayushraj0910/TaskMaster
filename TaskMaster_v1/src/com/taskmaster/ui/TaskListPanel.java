package com.taskmaster.ui;

import com.taskmaster.model.Task;
import com.taskmaster.service.TaskService;
import com.taskmaster.util.AppTheme;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class TaskListPanel extends JPanel {
    private final TaskService taskService;
    private JPanel taskContainer;
    private JPanel mainPanel;
    private JPanel statsPanel;
    private String sortMode = "priority";
    private JToggleButton sortByPriority, sortByDate;

    public TaskListPanel(TaskService taskService) {
        this.taskService = taskService;
        setBackground(AppTheme.BG_PRIMARY);
        setLayout(new BorderLayout());
        buildUI();
        taskService.addListener(this::refresh);
    }

    private void buildUI() {
        // Header bar
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(AppTheme.BG_SECONDARY);
        header.setBorder(new CompoundBorder(
            new MatteBorder(0, 0, 1, 0, AppTheme.BORDER),
            BorderFactory.createEmptyBorder(14, 20, 14, 20)
        ));

        JLabel title = new JLabel("DAILY TASK CHART");
        title.setFont(AppTheme.FONT_HEADER);
        title.setForeground(AppTheme.TEXT_PRIMARY);
        header.add(title, BorderLayout.WEST);

        // Sort controls
        JPanel sortPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        sortPanel.setBackground(AppTheme.BG_SECONDARY);
        JLabel sortLabel = new JLabel("Sort:");
        sortLabel.setFont(AppTheme.FONT_SMALL);
        sortLabel.setForeground(AppTheme.TEXT_MUTED);

        ButtonGroup bg = new ButtonGroup();
        sortByPriority = createToggleButton("Priority", true);
        sortByDate = createToggleButton("Date", false);
        bg.add(sortByPriority);
        bg.add(sortByDate);

        sortByPriority.addActionListener(e -> { sortMode = "priority"; refresh(); });
        sortByDate.addActionListener(e -> { sortMode = "date"; refresh(); });

        sortPanel.add(sortLabel);
        sortPanel.add(sortByPriority);
        sortPanel.add(sortByDate);

        // Clear Completed button
        sortPanel.add(Box.createHorizontalStrut(12));
        JButton clearDoneBtn = new JButton("Clear Completed") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color base = new Color(30, 215, 96);
                g2.setColor(getModel().isPressed() ? base.darker()
                    : getModel().isRollover() ? new Color(30, 215, 96, 220) : new Color(30, 215, 96, 160));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        clearDoneBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        clearDoneBtn.setForeground(Color.WHITE);
        clearDoneBtn.setOpaque(false);
        clearDoneBtn.setContentAreaFilled(false);
        clearDoneBtn.setBorder(BorderFactory.createEmptyBorder(5, 14, 5, 14));
        clearDoneBtn.setFocusPainted(false);
        clearDoneBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        clearDoneBtn.addActionListener(e -> {
            long count = taskService.getAllTasks().stream().filter(Task::isCompleted).count();
            if (count == 0) {
                JOptionPane.showMessageDialog(this, "No completed tasks to clear.", "Nothing to Clear", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            taskService.archiveCompleted();
        });
        sortPanel.add(clearDoneBtn);

        header.add(sortPanel, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // Stats bar
        JPanel statsBar = buildStatsBar();
        add(statsBar, BorderLayout.CENTER);

        // Task container in scroll
        taskContainer = new JPanel();
        taskContainer.setLayout(new BoxLayout(taskContainer, BoxLayout.Y_AXIS));
        taskContainer.setBackground(AppTheme.BG_PRIMARY);
        taskContainer.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

        JScrollPane scroll = new JScrollPane(taskContainer);
        scroll.setBorder(null);
        scroll.setBackground(AppTheme.BG_PRIMARY);
        scroll.getViewport().setBackground(AppTheme.BG_PRIMARY);
        scroll.getVerticalScrollBar().setBackground(AppTheme.BG_PRIMARY);
        scroll.getVerticalScrollBar().setPreferredSize(new Dimension(6, 0));

        // Layout
        setLayout(new BorderLayout());
        add(header, BorderLayout.NORTH);
        mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(AppTheme.BG_PRIMARY);
        statsPanel = buildStatsBar();
        mainPanel.add(statsPanel, BorderLayout.NORTH);
        mainPanel.add(scroll, BorderLayout.CENTER);
        add(mainPanel, BorderLayout.CENTER);

        refresh();
    }

    private JPanel buildStatsBar() {
        JPanel bar = new JPanel(new GridLayout(1, 4, 1, 0));
        bar.setBackground(AppTheme.BG_SECONDARY);
        bar.setBorder(new MatteBorder(0, 0, 1, 0, AppTheme.BORDER));
        bar.setPreferredSize(new Dimension(0, 70));

        List<Task> all = taskService.getAllTasks().stream().filter(t -> !t.isArchived()).collect(Collectors.toList());
        long total = all.size();
        long done = all.stream().filter(Task::isCompleted).count();
        long high = all.stream().filter(t -> !t.isCompleted() && (t.getPriority() == Task.Priority.HIGH || t.getPriority() == Task.Priority.CRITICAL)).count();
        long pending = all.stream().filter(t -> !t.isCompleted()).count();

        bar.add(createStatCard("Total", String.valueOf(total), AppTheme.ACCENT_BLUE));
        bar.add(createStatCard("Completed", String.valueOf(done), AppTheme.ACCENT_GREEN));
        bar.add(createStatCard("Pending", String.valueOf(pending), AppTheme.ACCENT_ORANGE));
        bar.add(createStatCard("High Priority", String.valueOf(high), AppTheme.PRIORITY_CRITICAL));

        return bar;
    }

    private JPanel createStatCard(String label, String value, Color accent) {
        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(AppTheme.BG_SECONDARY);
        card.setBorder(new CompoundBorder(
            new MatteBorder(0, 0, 0, 1, AppTheme.BORDER_SUBTLE),
            BorderFactory.createEmptyBorder(8, 16, 8, 16)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.WEST;

        JLabel valLabel = new JLabel(value);
        valLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        valLabel.setForeground(accent);

        JLabel nameLabel = new JLabel(label);
        nameLabel.setFont(AppTheme.FONT_SMALL);
        nameLabel.setForeground(AppTheme.TEXT_MUTED);

        JPanel vbox = new JPanel();
        vbox.setLayout(new BoxLayout(vbox, BoxLayout.Y_AXIS));
        vbox.setBackground(AppTheme.BG_SECONDARY);
        vbox.add(valLabel);
        vbox.add(Box.createVerticalStrut(2));
        vbox.add(nameLabel);

        card.add(vbox, gbc);
        return card;
    }

    private void refresh() {
        taskContainer.removeAll();

        List<Task> tasks = sortMode.equals("priority")
            ? taskService.getTasksSortedByPriority()
            : taskService.getTasksSortedByDate();

        // Hide archived tasks (cleared completed) — they live on in Calendar history only
        tasks = tasks.stream().filter(t -> !t.isArchived()).collect(Collectors.toList());

        if (tasks.isEmpty()) {
            JPanel empty = new JPanel(new GridBagLayout());
            empty.setBackground(AppTheme.BG_PRIMARY);
            empty.setPreferredSize(new Dimension(0, 200));
            JLabel lbl = new JLabel("No tasks yet. Add one to get started!");
            lbl.setFont(AppTheme.FONT_BODY);
            lbl.setForeground(AppTheme.TEXT_MUTED);
            empty.add(lbl);
            taskContainer.add(empty);
        } else {
            // Group by priority or date header
            Task.Priority lastPriority = null;
            String lastDate = null;

            for (Task task : tasks) {
                if (sortMode.equals("priority")) {
                    if (task.getPriority() != lastPriority) {
                        lastPriority = task.getPriority();
                        taskContainer.add(createGroupHeader(lastPriority.name() + " PRIORITY", AppTheme.getPriorityColor(lastPriority)));
                        taskContainer.add(Box.createVerticalStrut(4));
                    }
                } else {
                    String dateStr = task.getPrimaryDate() != null ? task.getPrimaryDate().toString() : "No Date";
                    if (!dateStr.equals(lastDate)) {
                        lastDate = dateStr;
                        taskContainer.add(createGroupHeader(dateStr.equals("No Date") ? "NO DATE SET" : task.getPrimaryDate().format(DateTimeFormatter.ofPattern("EEEE, d MMMM")), AppTheme.ACCENT_BLUE));
                        taskContainer.add(Box.createVerticalStrut(4));
                    }
                }
                taskContainer.add(createTaskCard(task));
                taskContainer.add(Box.createVerticalStrut(6));
            }
        }

        taskContainer.add(Box.createVerticalGlue());
        taskContainer.revalidate();
        taskContainer.repaint();

        // Rebuild stats bar in place
        if (mainPanel != null && statsPanel != null) {
            mainPanel.remove(statsPanel);
            statsPanel = buildStatsBar();
            mainPanel.add(statsPanel, BorderLayout.NORTH);
            mainPanel.revalidate();
            mainPanel.repaint();
        }
    }

    private JLabel createGroupHeader(String text, Color color) {
        JLabel header = new JLabel(text);
        header.setFont(AppTheme.FONT_BADGE);
        header.setForeground(color);
        header.setBorder(BorderFactory.createEmptyBorder(12, 4, 4, 0));
        header.setAlignmentX(LEFT_ALIGNMENT);
        return header;
    }

    private JPanel createTaskCard(Task task) {
        JPanel card = new JPanel(new BorderLayout(12, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(AppTheme.BG_CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 105));
        card.setAlignmentX(LEFT_ALIGNMENT);
        card.setBorder(new CompoundBorder(
            new EmptyBorder(0, 0, 0, 0),
            new CompoundBorder(
                new MatteBorder(0, 4, 0, 0, AppTheme.getPriorityColor(task.getPriority())),
                BorderFactory.createEmptyBorder(12, 16, 12, 16)
            )
        ));

        // Checkbox
        JCheckBox check = new JCheckBox();
        check.setSelected(task.isCompleted());
        check.setBackground(AppTheme.BG_CARD);
        check.setForeground(AppTheme.ACCENT_GREEN);
        check.setFocusPainted(false);
        check.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        check.addActionListener(e -> {
            taskService.toggleComplete(task.getId());
            if (check.isSelected()) {
                // Small toast notification
                JWindow toast = new JWindow(SwingUtilities.getWindowAncestor(this));
                JPanel tp = new JPanel(new BorderLayout()) {
                    @Override protected void paintComponent(Graphics g) {
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        g2.setColor(new Color(30, 215, 96, 220));
                        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                        g2.dispose();
                    }
                };
                tp.setOpaque(false);
                tp.setBorder(BorderFactory.createEmptyBorder(10, 18, 10, 18));
                JLabel lbl = new JLabel("✔  Marked as done");
                lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
                lbl.setForeground(Color.WHITE);
                tp.add(lbl);
                toast.add(tp);
                toast.pack();
                Window win = SwingUtilities.getWindowAncestor(this);
                if (win != null) {
                    toast.setLocation(win.getX() + win.getWidth() - toast.getWidth() - 20, win.getY() + 55);
                }
                toast.setVisible(true);
                new Timer(1800, ev -> toast.dispose()) {{ setRepeats(false); start(); }};
            }
        });

        // Title + desc
        JPanel info = new JPanel();
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.setBackground(AppTheme.BG_CARD);

        JLabel titleLbl = new JLabel(task.getTitle());
        titleLbl.setFont(task.isCompleted()
            ? new Font("Segoe UI", Font.PLAIN, 14)
            : new Font("Segoe UI", Font.BOLD, 14));
        titleLbl.setForeground(task.isCompleted() ? AppTheme.TEXT_MUTED : AppTheme.TEXT_PRIMARY);

        info.add(titleLbl);

        if (task.getDescription() != null && !task.getDescription().isBlank()) {
            JLabel desc = new JLabel(task.getDescription().length() > 60
                ? task.getDescription().substring(0, 57) + "..." : task.getDescription());
            desc.setFont(AppTheme.FONT_SMALL);
            desc.setForeground(AppTheme.TEXT_MUTED);
            info.add(Box.createVerticalStrut(2));
            info.add(desc);
        }

        // Right side: priority badge + date
        JPanel right = new JPanel();
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
        right.setBackground(AppTheme.BG_CARD);
        right.setAlignmentY(TOP_ALIGNMENT);

        JPanel badge = createPriorityBadge(task.getPriority());
        badge.setAlignmentX(RIGHT_ALIGNMENT);
        right.add(badge);
        right.add(Box.createVerticalStrut(6));

        if (task.getPlannedDate() != null) {
            JLabel planLbl = new JLabel("[Plan] " + task.getPlannedDate().format(DateTimeFormatter.ofPattern("d MMM")));
            planLbl.setFont(AppTheme.FONT_SMALL);
            planLbl.setForeground(AppTheme.ACCENT_CYAN);
            planLbl.setAlignmentX(RIGHT_ALIGNMENT);
            right.add(planLbl);
        }
        if (task.getDueDate() != null) {
            String dueStr = "[Due] " + task.getDueDate().format(DateTimeFormatter.ofPattern("d MMM"));
            if (task.getDueTime() != null) dueStr += " " + task.getDueTime().format(DateTimeFormatter.ofPattern("HH:mm"));
            JLabel dateLbl = new JLabel(dueStr);
            dateLbl.setFont(AppTheme.FONT_SMALL);
            dateLbl.setForeground(AppTheme.ACCENT_ORANGE);
            dateLbl.setAlignmentX(RIGHT_ALIGNMENT);
            right.add(dateLbl);
        }

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        left.setBackground(AppTheme.BG_CARD);
        left.add(check);

        card.add(left, BorderLayout.WEST);
        card.add(info, BorderLayout.CENTER);
        card.add(right, BorderLayout.EAST);

        // Hover effect
        card.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { card.setBackground(AppTheme.BG_HOVER); card.repaint(); }
            public void mouseExited(MouseEvent e) { card.setBackground(AppTheme.BG_CARD); card.repaint(); }
        });

        return card;
    }

    private JPanel createPriorityBadge(Task.Priority priority) {
        Color color = AppTheme.getPriorityColor(priority);
        JPanel badge = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 2)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 25));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.dispose();
            }
        };
        badge.setOpaque(false);
        badge.setBorder(new LineBorder(new Color(color.getRed(), color.getGreen(), color.getBlue(), 80), 1, true));

        JLabel lbl = new JLabel(priority.name());
        lbl.setFont(AppTheme.FONT_BADGE);
        lbl.setForeground(color);
        badge.add(lbl);
        return badge;
    }

    private JToggleButton createToggleButton(String text, boolean selected) {
        JToggleButton btn = new JToggleButton(text, selected);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setForeground(selected ? AppTheme.ACCENT_BLUE : AppTheme.TEXT_PRIMARY);
        btn.setBackground(selected ? AppTheme.BG_CARD : AppTheme.BG_SECONDARY);
        btn.setOpaque(true);
        btn.setBorder(new CompoundBorder(
            new LineBorder(selected ? AppTheme.ACCENT_BLUE : AppTheme.BORDER, 1, true),
            BorderFactory.createEmptyBorder(5, 12, 5, 12)
        ));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }
}
