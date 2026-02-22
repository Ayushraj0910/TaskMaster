package com.taskmaster.ui;

import com.taskmaster.model.Task;
import com.taskmaster.service.TaskService;
import com.taskmaster.util.AppTheme;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class CalendarPanel extends JPanel {
    private final TaskService taskService;
    private YearMonth currentMonth;
    private LocalDate selectedDate;
    private JPanel calendarGrid;
    private JLabel monthLabel;
    private JPanel taskListPanel;
    private JLabel selectedDateLabel;

    private static final DateTimeFormatter SIDE_DATE_FMT =
        DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy");
    private static final DateTimeFormatter TIME_FMT =
        DateTimeFormatter.ofPattern("HH:mm");

    public CalendarPanel(TaskService taskService) {
        this.taskService = taskService;
        this.currentMonth = YearMonth.now();
        this.selectedDate = LocalDate.now();
        setBackground(AppTheme.BG_PRIMARY);
        setLayout(new BorderLayout(0, 0));
        buildUI();
        taskService.addListener(this::refresh);
    }

    private void buildUI() {
        add(createNavPanel(), BorderLayout.NORTH);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        split.setBackground(AppTheme.BG_PRIMARY);
        split.setBorder(null);
        split.setDividerSize(1);
        split.setDividerLocation(540);
        split.setResizeWeight(0.62);

        JPanel calendarContainer = new JPanel(new BorderLayout());
        calendarContainer.setBackground(AppTheme.BG_PRIMARY);
        calendarContainer.setBorder(BorderFactory.createEmptyBorder(14, 20, 10, 10));
        calendarContainer.add(buildDayHeaders(), BorderLayout.NORTH);

        calendarGrid = new JPanel(new GridLayout(0, 7, 5, 5));
        calendarGrid.setBackground(AppTheme.BG_PRIMARY);
        calendarContainer.add(calendarGrid, BorderLayout.CENTER);
        calendarContainer.add(buildLegend(), BorderLayout.SOUTH);

        split.setLeftComponent(calendarContainer);
        split.setRightComponent(buildTaskSidePanel());

        add(split, BorderLayout.CENTER);
        renderCalendar();
    }

    // ─── Nav Panel ───────────────────────────────────────────────────────────
    private JPanel createNavPanel() {
        JPanel nav = new JPanel(new BorderLayout());
        nav.setBackground(AppTheme.BG_SECONDARY);
        nav.setBorder(new CompoundBorder(
            new MatteBorder(0, 0, 1, 0, AppTheme.BORDER),
            BorderFactory.createEmptyBorder(14, 20, 14, 20)
        ));

        JLabel icon = new JLabel("CALENDAR");
        icon.setFont(AppTheme.FONT_HEADER);
        icon.setForeground(AppTheme.TEXT_PRIMARY);
        nav.add(icon, BorderLayout.WEST);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        controls.setBackground(AppTheme.BG_SECONDARY);

        JButton prev  = createNavArrow("<");
        JButton next  = createNavArrow(">");
        JButton today = createSmallButton("Today");

        monthLabel = new JLabel("", SwingConstants.CENTER);
        monthLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        monthLabel.setForeground(AppTheme.TEXT_PRIMARY);
        monthLabel.setPreferredSize(new Dimension(200, 30));

        prev.addActionListener(e  -> { currentMonth = currentMonth.minusMonths(1); renderCalendar(); });
        next.addActionListener(e  -> { currentMonth = currentMonth.plusMonths(1);  renderCalendar(); });
        today.addActionListener(e -> { currentMonth = YearMonth.now(); selectedDate = LocalDate.now(); renderCalendar(); updateTaskList(); });

        controls.add(prev);
        controls.add(monthLabel);
        controls.add(next);
        controls.add(Box.createHorizontalStrut(8));
        controls.add(today);
        nav.add(controls, BorderLayout.CENTER);
        return nav;
    }

    // ─── Day header row ───────────────────────────────────────────────────────
    private JPanel buildDayHeaders() {
        JPanel headers = new JPanel(new GridLayout(1, 7, 5, 0));
        headers.setBackground(AppTheme.BG_PRIMARY);
        headers.setBorder(BorderFactory.createEmptyBorder(0, 0, 6, 0));
        for (String day : new String[]{"SUN","MON","TUE","WED","THU","FRI","SAT"}) {
            JLabel lbl = new JLabel(day, SwingConstants.CENTER);
            lbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
            lbl.setForeground(AppTheme.TEXT_MUTED);
            headers.add(lbl);
        }
        return headers;
    }

    // ─── Legend ───────────────────────────────────────────────────────────────
    private JPanel buildLegend() {
        JPanel legend = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 8));
        legend.setBackground(AppTheme.BG_PRIMARY);
        addLegendDot(legend, AppTheme.PRIORITY_CRITICAL,    "Critical");
        addLegendDot(legend, new Color(255, 120, 40),       "Due");
        addLegendDot(legend, new Color(255, 200, 0),        "Shifted");
        addLegendDot(legend, new Color(255, 160, 0),        "Urgent");
        addLegendDot(legend, new Color(220, 60, 60),        "Overdue");
        addLegendDot(legend, AppTheme.ACCENT_GREEN,         "Done");
        return legend;
    }

    private void addLegendDot(JPanel parent, Color color, String label) {
        JPanel item = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        item.setBackground(AppTheme.BG_PRIMARY);
        JPanel dot = new JPanel() {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(color);
                g2.fillOval(0, 2, 9, 9);
            }
        };
        dot.setPreferredSize(new Dimension(9, 13));
        dot.setBackground(AppTheme.BG_PRIMARY);
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lbl.setForeground(AppTheme.TEXT_MUTED);
        item.add(dot);
        item.add(lbl);
        parent.add(item);
    }

    // ─── Right side task panel ───────────────────────────────────────────────
    private JPanel buildTaskSidePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(AppTheme.BG_SECONDARY);
        panel.setBorder(new MatteBorder(0, 1, 0, 0, AppTheme.BORDER));

        selectedDateLabel = new JLabel("", SwingConstants.LEFT);
        selectedDateLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        selectedDateLabel.setForeground(AppTheme.TEXT_PRIMARY);
        selectedDateLabel.setBorder(BorderFactory.createEmptyBorder(16, 16, 12, 16));
        panel.add(selectedDateLabel, BorderLayout.NORTH);

        taskListPanel = new JPanel();
        taskListPanel.setLayout(new BoxLayout(taskListPanel, BoxLayout.Y_AXIS));
        taskListPanel.setBackground(AppTheme.BG_SECONDARY);

        JScrollPane scroll = new JScrollPane(taskListPanel);
        scroll.setBackground(AppTheme.BG_SECONDARY);
        scroll.getViewport().setBackground(AppTheme.BG_SECONDARY);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setPreferredSize(new Dimension(5, 0));
        panel.add(scroll, BorderLayout.CENTER);

        updateTaskList();
        return panel;
    }

    // ─── Calendar rendering ───────────────────────────────────────────────────
    private void renderCalendar() {
        monthLabel.setText(currentMonth.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH)
            + " " + currentMonth.getYear());
        calendarGrid.removeAll();

        LocalDate first = currentMonth.atDay(1);
        int startDay = first.getDayOfWeek().getValue() % 7; // 0=Sun

        for (int i = 0; i < startDay; i++) calendarGrid.add(createEmptyCell());

        for (int day = 1; day <= currentMonth.lengthOfMonth(); day++) {
            calendarGrid.add(createDayCell(currentMonth.atDay(day)));
        }

        int used = startDay + currentMonth.lengthOfMonth();
        int remaining = (7 - (used % 7)) % 7;
        for (int i = 0; i < remaining; i++) calendarGrid.add(createEmptyCell());

        calendarGrid.revalidate();
        calendarGrid.repaint();
    }

    private JPanel createEmptyCell() {
        JPanel p = new JPanel();
        p.setBackground(AppTheme.BG_PRIMARY);
        return p;
    }

    // ─── Day cell with smart dot indicators ──────────────────────────────────
    private JPanel createDayCell(LocalDate date) {
        boolean isToday    = date.equals(LocalDate.now());
        boolean isSelected = date.equals(selectedDate);

        List<Task> dateTasks = taskService.getTasksByDate(date);

        // Compute per-task statuses
        List<TaskService.DateStatus> statuses = dateTasks.stream()
            .map(t -> taskService.getStatusForTaskOnDate(t, date))
            .collect(Collectors.toList());

        boolean hasTasks  = !dateTasks.isEmpty();
        boolean allDone   = hasTasks && statuses.stream().allMatch(s -> s == TaskService.DateStatus.COMPLETED);
        boolean hasOverdue = statuses.contains(TaskService.DateStatus.OVERDUE);
        boolean hasUrgent  = statuses.contains(TaskService.DateStatus.URGENT);

        // Check if any active task has HIGH or CRITICAL priority
        boolean hasHighOrCritical = dateTasks.stream()
            .filter(t -> !t.isCompleted())
            .anyMatch(t -> t.getPriority() == Task.Priority.HIGH || t.getPriority() == Task.Priority.CRITICAL);

        // Border color priority: OVERDUE > URGENT > ALL_DONE > HIGH/CRITICAL > HAS_TASKS(blue) > TODAY > SELECTED
        Color borderColor;
        float borderWidth;
        Color bgTint;
        if (hasOverdue) {
            borderColor = new Color(220, 60, 60);       // red
            borderWidth = 2f;
            bgTint = new Color(220, 60, 60, 14);
        } else if (hasUrgent) {
            borderColor = new Color(255, 160, 0);       // orange
            borderWidth = 2f;
            bgTint = new Color(255, 160, 0, 14);
        } else if (allDone) {
            borderColor = AppTheme.ACCENT_GREEN;        // green
            borderWidth = 2f;
            bgTint = new Color(60, 210, 130, 18);
        } else if (hasTasks && hasHighOrCritical) {
            // Highest priority color among HIGH/CRITICAL tasks
            boolean hasCritical = dateTasks.stream().filter(t -> !t.isCompleted())
                .anyMatch(t -> t.getPriority() == Task.Priority.CRITICAL);
            borderColor = hasCritical ? AppTheme.PRIORITY_CRITICAL : AppTheme.PRIORITY_HIGH;
            borderWidth = 2f;
            bgTint = new Color(borderColor.getRed(), borderColor.getGreen(), borderColor.getBlue(), 12);
        } else if (hasTasks && !allDone) {
            borderColor = AppTheme.ACCENT_BLUE;         // blue for any other task
            borderWidth = 1.5f;
            bgTint = new Color(88, 166, 255, 10);
        } else if (isToday) {
            borderColor = AppTheme.ACCENT_BLUE;
            borderWidth = 2f;
            bgTint = AppTheme.BG_CARD;
        } else if (isSelected) {
            borderColor = AppTheme.BORDER;
            borderWidth = 1.5f;
            bgTint = AppTheme.BG_SELECTED;
        } else {
            borderColor = null;
            borderWidth = 0f;
            bgTint = AppTheme.BG_CARD;
        }

        final Color finalBorderColor = borderColor;
        final float finalBorderWidth = borderWidth;
        final Color finalBgTint = bgTint;

        JPanel cell = new JPanel(new BorderLayout(0, 2)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(isSelected && finalBorderColor == null ? AppTheme.BG_SELECTED : finalBgTint);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                if (finalBorderColor != null) {
                    g2.setColor(finalBorderColor);
                    g2.setStroke(new BasicStroke(finalBorderWidth));
                    g2.drawRoundRect(1, 1, getWidth()-2, getHeight()-2, 10, 10);
                }
                g2.dispose();
            }
        };
        cell.setOpaque(false);
        cell.setBorder(BorderFactory.createEmptyBorder(5, 6, 5, 6));

        // Day number — red if overdue, orange if urgent, blue if today
        JLabel dayNum = new JLabel(String.valueOf(date.getDayOfMonth()));
        dayNum.setFont(new Font("Segoe UI", hasOverdue || hasUrgent || isToday ? Font.BOLD : Font.PLAIN, 13));
        dayNum.setForeground(
            hasOverdue ? new Color(220, 80, 80) :
            hasUrgent  ? new Color(255, 160, 0) :
            isToday    ? AppTheme.ACCENT_BLUE   :
                         AppTheme.TEXT_PRIMARY
        );
        cell.add(dayNum, BorderLayout.NORTH);

        // Dots — one per task, color driven by DateStatus
        if (hasTasks) {
            cell.add(buildSmartDotIndicator(dateTasks, date), BorderLayout.SOUTH);
        }

        cell.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        cell.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { cell.repaint(); }
            public void mouseExited(MouseEvent e)  { cell.repaint(); }
            public void mouseClicked(MouseEvent e) {
                selectedDate = date;
                renderCalendar();
                updateTaskList();
            }
        });

        return cell;
    }

    /** Returns the display color for a DateStatus dot. */
    private Color statusColor(TaskService.DateStatus status, Task task) {
        switch (status) {
            case COMPLETED: return AppTheme.ACCENT_GREEN;
            case OVERDUE:   return new Color(220, 60, 60);
            case URGENT:    return new Color(255, 160, 0);
            case SHIFTED:   return new Color(255, 200, 0);   // yellow — attention needed
            case DUE:       return new Color(255, 120, 40);  // orange-ish for due
            case PLANNED:   return AppTheme.getPriorityColor(task.getPriority());
            default:        return AppTheme.TEXT_MUTED;
        }
    }

    private JPanel buildSmartDotIndicator(List<Task> tasks, LocalDate date) {
        // Build list of (task, status, color) — skip NONE
        List<Color> colors = tasks.stream()
            .map(t -> {
                TaskService.DateStatus s = taskService.getStatusForTaskOnDate(t, date);
                return statusColor(s, t);
            })
            .collect(Collectors.toList());

        int count = colors.size();

        if (count <= 3) {
            JPanel row = new JPanel() {
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2 = (Graphics2D) g;
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    int totalW = count * 9 + (count - 1) * 3;
                    int startX = (getWidth() - totalW) / 2;
                    int y = (getHeight() - 9) / 2;
                    for (int i = 0; i < count; i++) {
                        g2.setColor(colors.get(i));
                        g2.fillOval(startX + i * 12, y, 9, 9);
                    }
                }
            };
            row.setOpaque(false);
            row.setPreferredSize(new Dimension(0, 13));
            return row;
        } else {
            JPanel wrapper = new JPanel(new BorderLayout(0, 0));
            wrapper.setOpaque(false);
            JPanel overlapDots = new JPanel() {
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2 = (Graphics2D) g;
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    int dotW = 9, overlap = 5;
                    int totalW = dotW + 2 * (dotW - overlap);
                    int startX = (getWidth() - totalW) / 2;
                    int y = (getHeight() - dotW) / 2;
                    for (int i = Math.min(2, count - 1); i >= 0; i--) {
                        g2.setColor(new Color(18, 18, 18));
                        g2.fillOval(startX + i * (dotW - overlap) - 1, y - 1, dotW + 2, dotW + 2);
                        g2.setColor(colors.get(i));
                        g2.fillOval(startX + i * (dotW - overlap), y, dotW, dotW);
                    }
                }
            };
            overlapDots.setOpaque(false);
            overlapDots.setPreferredSize(new Dimension(0, 12));
            JLabel many = new JLabel("many", SwingConstants.CENTER);
            many.setFont(new Font("Segoe UI", Font.BOLD, 8));
            many.setForeground(AppTheme.TEXT_MUTED);
            wrapper.add(overlapDots, BorderLayout.CENTER);
            wrapper.add(many, BorderLayout.SOUTH);
            return wrapper;
        }
    }

    // ─── Task side list ───────────────────────────────────────────────────────
    private void updateTaskList() {
        taskListPanel.removeAll();
        if (selectedDate == null) { taskListPanel.revalidate(); return; }

        selectedDateLabel.setText("  " + selectedDate.format(SIDE_DATE_FMT));

        List<Task> allTasksOnDay = taskService.getTasksByDate(selectedDate);

        // Active = not completed (show normally, user can check them)
        List<Task> active = allTasksOnDay.stream()
            .filter(t -> !t.isCompleted())
            .collect(Collectors.toList());

        // History = completed tasks (archived or not — ALWAYS shown in calendar as read-only info)
        List<Task> done = allTasksOnDay.stream()
            .filter(Task::isCompleted)
            .collect(Collectors.toList());

        if (active.isEmpty() && done.isEmpty()) {
            JLabel empty = new JLabel("No tasks for this day", SwingConstants.CENTER);
            empty.setFont(AppTheme.FONT_BODY);
            empty.setForeground(AppTheme.TEXT_MUTED);
            empty.setBorder(BorderFactory.createEmptyBorder(30, 0, 0, 0));
            empty.setAlignmentX(CENTER_ALIGNMENT);
            taskListPanel.add(empty);
        } else {
            // Active tasks shown normally
            for (Task task : active) {
                taskListPanel.add(createMiniTaskCard(task));
                taskListPanel.add(Box.createVerticalStrut(5));
            }
            // Completed tasks always shown as read-only history
            if (!done.isEmpty()) {
                if (!active.isEmpty()) taskListPanel.add(Box.createVerticalStrut(8));
                JLabel doneHeader = new JLabel("  ✔  COMPLETED");
                doneHeader.setFont(new Font("Segoe UI", Font.BOLD, 10));
                doneHeader.setForeground(AppTheme.ACCENT_GREEN);
                doneHeader.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
                taskListPanel.add(doneHeader);
                taskListPanel.add(Box.createVerticalStrut(4));
                for (Task task : done) {
                    taskListPanel.add(createCompletedInfoCard(task));
                    taskListPanel.add(Box.createVerticalStrut(5));
                }
            }
        }
        taskListPanel.add(Box.createVerticalGlue());
        taskListPanel.revalidate();
        taskListPanel.repaint();
    }

    private JPanel createMiniTaskCard(Task task) {
        TaskService.DateStatus status = taskService.getStatusForTaskOnDate(task, selectedDate);
        Color priColor = AppTheme.getPriorityColor(task.getPriority());

        // Left border color driven by status
        Color borderColor;
        switch (status) {
            case OVERDUE:  borderColor = new Color(220, 60, 60); break;
            case URGENT:   borderColor = new Color(255, 160, 0); break;
            case SHIFTED:  borderColor = new Color(255, 200, 0); break;
            case DUE:      borderColor = new Color(255, 120, 40); break;
            default:       borderColor = priColor; break;
        }

        JPanel card = new JPanel(new BorderLayout(10, 0));
        card.setBackground(AppTheme.BG_CARD);
        card.setBorder(new CompoundBorder(
            new MatteBorder(0, 4, 0, 0, borderColor),
            BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 72));

        // Status dot
        final Color dotColor = borderColor;
        JPanel dotPanel = new JPanel() {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(dotColor);
                g2.fillOval(0, 4, 10, 10);
            }
        };
        dotPanel.setOpaque(false);
        dotPanel.setPreferredSize(new Dimension(12, 18));

        JPanel info = new JPanel();
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.setBackground(AppTheme.BG_CARD);

        JLabel titleLbl = new JLabel(task.getTitle());
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        titleLbl.setForeground(status == TaskService.DateStatus.OVERDUE
            ? new Color(220, 80, 80) : AppTheme.TEXT_PRIMARY);

        // Sub-line: planned or due date + time
        String sub = task.getPriority().name();
        if (task.getPlannedDate() != null)
            sub = "Plan: " + task.getPlannedDate().format(DateTimeFormatter.ofPattern("d MMM"));
        if (task.getDueDate() != null)
            sub += "  Due: " + task.getDueDate().format(DateTimeFormatter.ofPattern("d MMM"));
        if (task.getDueTime() != null)
            sub += " " + task.getDueTime().format(TIME_FMT);
        JLabel subLbl = new JLabel(sub);
        subLbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        subLbl.setForeground(AppTheme.TEXT_MUTED);

        info.add(titleLbl);
        info.add(Box.createVerticalStrut(2));
        info.add(subLbl);

        // Status badge on right
        String badgeText;
        Color badgeColor;
        switch (status) {
            case OVERDUE: badgeText = "⚠ OVERDUE"; badgeColor = new Color(220, 60, 60); break;
            case URGENT:  badgeText = "* URGENT";   badgeColor = new Color(255, 160, 0); break;
            case SHIFTED: badgeText = "> SHIFTED";  badgeColor = new Color(255, 200, 0); break;
            case DUE:     badgeText = "⏰ DUE";      badgeColor = new Color(255, 120, 40); break;
            default:      badgeText = task.getPriority().name(); badgeColor = priColor; break;
        }
        JLabel badge = new JLabel(badgeText);
        // Use emoji-capable font (Segoe UI Emoji on Windows renders ⚠ ⏰ correctly)
        Font emojiFont = new Font("Segoe UI Emoji", Font.BOLD, 10);
        if (!emojiFont.getFamily().equals("Segoe UI Emoji")) {
            emojiFont = new Font("Apple Color Emoji", Font.BOLD, 10);
        }
        if (!emojiFont.getFamily().equals("Apple Color Emoji")) {
            emojiFont = new Font("Segoe UI", Font.BOLD, 10); // fallback
        }
        badge.setFont(emojiFont);
        badge.setForeground(badgeColor);
        badge.setHorizontalAlignment(SwingConstants.RIGHT);

        card.add(dotPanel, BorderLayout.WEST);
        card.add(info,     BorderLayout.CENTER);
        card.add(badge,    BorderLayout.EAST);
        return card;
    }

    /** Read-only info card shown for completed tasks in calendar side panel. */
    private JPanel createCompletedInfoCard(Task task) {
        JPanel card = new JPanel(new BorderLayout(10, 0));
        card.setBackground(new Color(40, 60, 40));
        card.setBorder(new CompoundBorder(
            new MatteBorder(0, 4, 0, 0, AppTheme.ACCENT_GREEN),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 64));

        JPanel info = new JPanel();
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.setBackground(new Color(40, 60, 40));

        JLabel titleLbl = new JLabel("<html><s>" + task.getTitle() + "</s></html>");
        titleLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        titleLbl.setForeground(AppTheme.TEXT_MUTED);

        String completedStr = "Completed";
        if (task.getCompletedAt() > 0) {
            java.time.Instant inst = java.time.Instant.ofEpochMilli(task.getCompletedAt());
            java.time.LocalDateTime ldt = java.time.LocalDateTime.ofInstant(inst, java.time.ZoneId.systemDefault());
            completedStr = "Done " + ldt.format(DateTimeFormatter.ofPattern("d MMM, HH:mm"));
        }
        JLabel subLbl = new JLabel(completedStr);
        subLbl.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        subLbl.setForeground(AppTheme.ACCENT_GREEN);

        info.add(titleLbl);
        info.add(Box.createVerticalStrut(2));
        info.add(subLbl);

        JLabel badge = new JLabel("✔ Done");
        badge.setFont(new Font("Segoe UI", Font.BOLD, 10));
        badge.setForeground(AppTheme.ACCENT_GREEN);

        card.add(info, BorderLayout.CENTER);
        card.add(badge, BorderLayout.EAST);
        return card;
    }

    private void refresh() {
        renderCalendar();
        updateTaskList();
    }

    // ─── Button helpers ───────────────────────────────────────────────────────
    private JButton createNavArrow(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 18));
        btn.setForeground(AppTheme.TEXT_PRIMARY);
        btn.setBackground(AppTheme.BG_CARD);
        btn.setBorder(BorderFactory.createEmptyBorder(4, 14, 4, 14));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.setBackground(AppTheme.BG_HOVER); }
            public void mouseExited(MouseEvent e)  { btn.setBackground(AppTheme.BG_CARD);  }
        });
        return btn;
    }

    private JButton createSmallButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setForeground(AppTheme.TEXT_PRIMARY);
        btn.setBackground(AppTheme.BG_CARD);
        btn.setBorder(new CompoundBorder(
            new LineBorder(AppTheme.BORDER, 1, true),
            BorderFactory.createEmptyBorder(5, 12, 5, 12)
        ));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.setBackground(AppTheme.BG_HOVER); }
            public void mouseExited(MouseEvent e)  { btn.setBackground(AppTheme.BG_CARD);  }
        });
        return btn;
    }
}
