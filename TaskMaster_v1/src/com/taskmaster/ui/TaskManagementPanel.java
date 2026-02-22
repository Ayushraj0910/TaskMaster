package com.taskmaster.ui;

import com.taskmaster.model.Task;
import com.taskmaster.service.TaskService;
import com.taskmaster.util.AppTheme;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Month;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

public class TaskManagementPanel extends JPanel {
    private final TaskService taskService;
    private JPanel taskListContainer;
    private JTextField searchField;
    private Task selectedTask = null;

    // Form fields
    private JTextField titleField, timeField;
    private JTextArea descField;
    private JComboBox<Task.Priority> priorityBox;
    private JButton saveButton, clearButton, deleteButton;
    private JLabel formTitle;

    // Date picker components — Planned Date
    private JComboBox<String> plannedDayBox, plannedMonthBox, plannedYearBox;
    // Date picker components — Due Date (deadline)
    private JComboBox<String> dueDayBox, dueMonthBox, dueYearBox;

    private static final String[] MONTHS = {
        "Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"
    };

    private static final DateTimeFormatter DISPLAY_FMT = DateTimeFormatter.ofPattern("dd MMM yyyy");

    public TaskManagementPanel(TaskService taskService) {
        this.taskService = taskService;
        setBackground(AppTheme.BG_PRIMARY);
        setLayout(new BorderLayout());
        buildUI();
        taskService.addListener(this::refreshList);
    }

    private void buildUI() {
        JPanel header = buildHeader();
        add(header, BorderLayout.NORTH);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        split.setBorder(null);
        split.setDividerSize(1);
        split.setDividerLocation(420);
        split.setResizeWeight(0.38);
        split.setBackground(AppTheme.BG_PRIMARY);

        split.setLeftComponent(buildFormPanel());
        split.setRightComponent(buildListPanel());

        add(split, BorderLayout.CENTER);
        refreshList();
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(AppTheme.BG_SECONDARY);
        header.setBorder(new CompoundBorder(
            new MatteBorder(0, 0, 1, 0, AppTheme.BORDER),
            BorderFactory.createEmptyBorder(14, 20, 14, 20)
        ));
        JLabel title = new JLabel("TASK MANAGEMENT");
        title.setFont(AppTheme.FONT_HEADER);
        title.setForeground(AppTheme.TEXT_PRIMARY);
        header.add(title, BorderLayout.WEST);
        return header;
    }

    // ─── Form Panel ──────────────────────────────────────────────────────────
    private JPanel buildFormPanel() {
        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBackground(AppTheme.BG_SECONDARY);
        form.setBorder(new CompoundBorder(
            new MatteBorder(0, 0, 0, 1, AppTheme.BORDER),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));

        formTitle = new JLabel("Add New Task");
        formTitle.setFont(AppTheme.FONT_TITLE);
        formTitle.setForeground(AppTheme.TEXT_PRIMARY);
        formTitle.setAlignmentX(LEFT_ALIGNMENT);
        form.add(formTitle);
        form.add(Box.createVerticalStrut(18));

        // Task Title
        form.add(buildFieldLabel("Task Title *"));
        titleField = createTextField("Enter task title...");
        form.add(titleField);
        form.add(Box.createVerticalStrut(12));

        // Description
        form.add(buildFieldLabel("Description"));
        descField = new JTextArea(4, 20);
        descField.setFont(AppTheme.FONT_BODY);
        descField.setForeground(AppTheme.TEXT_PRIMARY);
        descField.setBackground(AppTheme.BG_CARD);
        descField.setCaretColor(AppTheme.TEXT_PRIMARY);
        descField.setLineWrap(true);
        descField.setWrapStyleWord(true);
        descField.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));

        JScrollPane descScroll = new JScrollPane(descField);
        descScroll.setBorder(new LineBorder(AppTheme.BORDER, 1, false));
        descScroll.setBackground(AppTheme.BG_CARD);
        descScroll.getViewport().setBackground(AppTheme.BG_CARD);
        descScroll.getVerticalScrollBar().setPreferredSize(new Dimension(6, 0));
        descScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        descScroll.setAlignmentX(LEFT_ALIGNMENT);

        descField.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                descScroll.setBorder(new LineBorder(AppTheme.ACCENT_BLUE, 1, false));
            }
            public void focusLost(FocusEvent e) {
                descScroll.setBorder(new LineBorder(AppTheme.BORDER, 1, false));
            }
        });

        form.add(descScroll);
        form.add(Box.createVerticalStrut(12));

        // Priority
        form.add(buildFieldLabel("Priority Level *"));
        priorityBox = new JComboBox<>(Task.Priority.values());
        styleComboBox(priorityBox);
        priorityBox.setSelectedItem(Task.Priority.MEDIUM);
        form.add(priorityBox);
        form.add(Box.createVerticalStrut(12));

        // ── Planned Date picker ──────────────────────────────────────────
        form.add(buildFieldLabel("Planned Date  (when you intend to do it)"));
        JPanel plannedDateRow = buildDatePickerRow(true);
        form.add(plannedDateRow);
        form.add(Box.createVerticalStrut(4));
        // small hint
        JLabel plannedHint = new JLabel("The day you plan to work on this task");
        plannedHint.setFont(AppTheme.FONT_SMALL);
        plannedHint.setForeground(AppTheme.TEXT_MUTED);
        plannedHint.setAlignmentX(LEFT_ALIGNMENT);
        form.add(plannedHint);
        form.add(Box.createVerticalStrut(12));

        // ── Due Date (deadline) picker ───────────────────────────────────
        form.add(buildFieldLabel("Due Date  (latest deadline)"));
        JPanel dueDateRow = buildDatePickerRow(false);
        form.add(dueDateRow);
        form.add(Box.createVerticalStrut(4));
        JLabel dueHint = new JLabel("Maximum delay — task must be done by this date");
        dueHint.setFont(AppTheme.FONT_SMALL);
        dueHint.setForeground(new Color(255, 170, 60, 180));
        dueHint.setAlignmentX(LEFT_ALIGNMENT);
        form.add(dueHint);
        form.add(Box.createVerticalStrut(12));

        // Due Time
        form.add(buildFieldLabel("Due Time (HH:mm, optional)"));
        timeField = createTextField("e.g. 17:30");
        form.add(timeField);
        form.add(Box.createVerticalStrut(20));

        // Buttons
        JPanel buttons = new JPanel(new GridLayout(1, 3, 8, 0));
        buttons.setBackground(AppTheme.BG_SECONDARY);
        buttons.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        buttons.setAlignmentX(LEFT_ALIGNMENT);

        saveButton   = createPrimaryButton("+ Save Task", AppTheme.ACCENT_BLUE);
        clearButton  = createSecondaryButton("Clear");
        deleteButton = createPrimaryButton("Delete", AppTheme.PRIORITY_CRITICAL);
        deleteButton.setVisible(false);

        saveButton.addActionListener(e -> saveTask());
        clearButton.addActionListener(e -> clearForm());
        deleteButton.addActionListener(e -> deleteTask());

        buttons.add(saveButton);
        buttons.add(clearButton);
        buttons.add(deleteButton);
        form.add(buttons);

        form.add(Box.createVerticalStrut(10));
        JLabel hint = new JLabel("* Required fields");
        hint.setFont(AppTheme.FONT_SMALL);
        hint.setForeground(AppTheme.TEXT_MUTED);
        hint.setAlignmentX(LEFT_ALIGNMENT);
        form.add(hint);
        form.add(Box.createVerticalGlue());

        JScrollPane scroll = new JScrollPane(form);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(AppTheme.BG_SECONDARY);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(AppTheme.BG_SECONDARY);
        wrapper.add(form, BorderLayout.CENTER);
        return wrapper;
    }

    // ─── Date Picker Row ─────────────────────────────────────────────────────
    /**
     * Builds a DD / MMM / YYYY dropdown row.
     * isPlanned=true  → fills plannedDayBox / plannedMonthBox / plannedYearBox
     * isPlanned=false → fills dueDayBox / dueMonthBox / dueYearBox
     */
    private JPanel buildDatePickerRow(boolean isPlanned) {
        // Day dropdown 1–31
        String[] days = new String[32];
        days[0] = "--";
        for (int i = 1; i <= 31; i++) days[i] = String.format("%02d", i);

        // Month dropdown
        String[] months = new String[13];
        months[0] = "---";
        System.arraycopy(MONTHS, 0, months, 1, 12);

        // Year dropdown: current year - 1 to current + 5
        int curYear = LocalDate.now().getYear();
        String[] years = new String[8];
        years[0] = "----";
        for (int i = 0; i < 7; i++) years[i + 1] = String.valueOf(curYear - 1 + i);

        JComboBox<String> dayBox   = createDateCombo(days);
        JComboBox<String> monthBox = createDateCombo(months);
        JComboBox<String> yearBox  = createDateCombo(years);

        // Wire to class fields
        if (isPlanned) {
            plannedDayBox   = dayBox;
            plannedMonthBox = monthBox;
            plannedYearBox  = yearBox;
        } else {
            dueDayBox   = dayBox;
            dueMonthBox = monthBox;
            dueYearBox  = yearBox;
        }

        // Auto-adjust days when month/year changes
        ItemListener adjuster = e -> adjustDays(dayBox, monthBox, yearBox);
        monthBox.addItemListener(adjuster);
        yearBox.addItemListener(adjuster);

        // Today shortcut button
        JButton todayBtn = new JButton("Today");
        todayBtn.setFont(new Font("Segoe UI", Font.BOLD, 11));
        todayBtn.setForeground(AppTheme.TEXT_PRIMARY);
        todayBtn.setBackground(AppTheme.BG_CARD);
        todayBtn.setBorder(new CompoundBorder(
            new LineBorder(AppTheme.BORDER, 1, true),
            BorderFactory.createEmptyBorder(3, 8, 3, 8)
        ));
        todayBtn.setFocusPainted(false);
        todayBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        todayBtn.addActionListener(e -> setDateDropdowns(dayBox, monthBox, yearBox, LocalDate.now()));

        // Clear date button
        JButton clearBtn = new JButton("x");
        clearBtn.setFont(new Font("Segoe UI", Font.BOLD, 11));
        clearBtn.setForeground(AppTheme.TEXT_PRIMARY);
        clearBtn.setBackground(AppTheme.BG_CARD);
        clearBtn.setBorder(new CompoundBorder(
            new LineBorder(AppTheme.BORDER, 1, true),
            BorderFactory.createEmptyBorder(3, 6, 3, 6)
        ));
        clearBtn.setFocusPainted(false);
        clearBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        clearBtn.addActionListener(e -> {
            dayBox.setSelectedIndex(0);
            monthBox.setSelectedIndex(0);
            yearBox.setSelectedIndex(0);
        });

        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        row.setBackground(AppTheme.BG_SECONDARY);
        row.setAlignmentX(LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));

        // Labels between dropdowns
        JLabel sep1 = new JLabel("/");
        sep1.setForeground(AppTheme.TEXT_MUTED);
        JLabel sep2 = new JLabel("/");
        sep2.setForeground(AppTheme.TEXT_MUTED);

        row.add(dayBox);
        row.add(sep1);
        row.add(monthBox);
        row.add(sep2);
        row.add(yearBox);
        row.add(Box.createHorizontalStrut(6));
        row.add(todayBtn);
        row.add(clearBtn);

        return row;
    }

    private JComboBox<String> createDateCombo(String[] items) {
        JComboBox<String> box = new JComboBox<>(items);
        box.setFont(AppTheme.FONT_BODY);
        box.setForeground(AppTheme.TEXT_PRIMARY);
        box.setBackground(AppTheme.BG_CARD);
        box.setFocusable(false);
        box.setRenderer(new DefaultListCellRenderer() {
            public Component getListCellRendererComponent(JList<?> list, Object value,
                    int idx, boolean sel, boolean foc) {
                super.getListCellRendererComponent(list, value, idx, sel, foc);
                setBackground(sel ? AppTheme.BG_HOVER : AppTheme.BG_CARD);
                setForeground(AppTheme.TEXT_PRIMARY);
                setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
                return this;
            }
        });
        return box;
    }

    /** Clamp the day dropdown to the actual number of days in the selected month/year */
    private void adjustDays(JComboBox<String> dayBox, JComboBox<String> monthBox,
                            JComboBox<String> yearBox) {
        int maxDay = 31;
        int monthIdx = monthBox.getSelectedIndex(); // 0 = placeholder
        int yearIdx  = yearBox.getSelectedIndex();

        if (monthIdx > 0 && yearIdx > 0) {
            try {
                int year = Integer.parseInt((String) yearBox.getSelectedItem());
                maxDay = YearMonth.of(year, monthIdx).lengthOfMonth();
            } catch (NumberFormatException ignored) {}
        } else if (monthIdx > 0) {
            // Approximate without year
            maxDay = Month.of(monthIdx).maxLength();
        }

        int currentSel = dayBox.getSelectedIndex();
        dayBox.removeAllItems();
        dayBox.addItem("--");
        for (int i = 1; i <= maxDay; i++) dayBox.addItem(String.format("%02d", i));

        // Restore selection if still valid
        if (currentSel > 0 && currentSel <= maxDay) {
            dayBox.setSelectedIndex(currentSel);
        }
    }

    /** Populate day/month/year dropdowns from a LocalDate */
    private void setDateDropdowns(JComboBox<String> dayBox, JComboBox<String> monthBox,
                                  JComboBox<String> yearBox, LocalDate date) {
        if (date == null) {
            dayBox.setSelectedIndex(0);
            monthBox.setSelectedIndex(0);
            yearBox.setSelectedIndex(0);
            return;
        }
        dayBox.setSelectedItem(String.format("%02d", date.getDayOfMonth()));
        monthBox.setSelectedIndex(date.getMonthValue()); // 1-based month = index in our array
        String yearStr = String.valueOf(date.getYear());
        // Find year in combo
        for (int i = 0; i < yearBox.getItemCount(); i++) {
            if (yearStr.equals(yearBox.getItemAt(i))) {
                yearBox.setSelectedIndex(i);
                break;
            }
        }
    }

    /** Read day/month/year dropdowns → LocalDate, or null if incomplete */
    private LocalDate readDateFromDropdowns(JComboBox<String> dayBox, JComboBox<String> monthBox,
                                           JComboBox<String> yearBox, String fieldLabel) {
        int dayIdx   = dayBox.getSelectedIndex();
        int monthIdx = monthBox.getSelectedIndex();
        int yearIdx  = yearBox.getSelectedIndex();

        // All unset = no date (allowed)
        if (dayIdx == 0 && monthIdx == 0 && yearIdx == 0) return null;

        // Partially set = error
        if (dayIdx == 0 || monthIdx == 0 || yearIdx == 0) {
            showError(fieldLabel + ": Please select a complete date (DD / Month / Year).");
            return null;
        }

        try {
            int day   = Integer.parseInt((String) dayBox.getSelectedItem());
            int month = monthIdx; // monthIdx 1–12 matches Month values
            int year  = Integer.parseInt((String) yearBox.getSelectedItem());
            return LocalDate.of(year, month, day);
        } catch (Exception ex) {
            showError(fieldLabel + ": Invalid date combination.");
            return null;
        }
    }

    // ─── List Panel ───────────────────────────────────────────────────────────
    private JPanel buildListPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(AppTheme.BG_PRIMARY);

        JPanel searchBar = new JPanel(new BorderLayout(8, 0));
        searchBar.setBackground(AppTheme.BG_PRIMARY);
        searchBar.setBorder(BorderFactory.createEmptyBorder(14, 16, 10, 16));

        searchField = new JTextField();
        searchField.setFont(AppTheme.FONT_BODY);
        searchField.setForeground(AppTheme.TEXT_PRIMARY);
        searchField.setBackground(AppTheme.BG_CARD);
        searchField.setCaretColor(AppTheme.TEXT_PRIMARY);
        searchField.setBorder(new CompoundBorder(
            new LineBorder(AppTheme.BORDER, 1, true),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        searchField.putClientProperty("JTextField.placeholderText", "🔍  Search tasks...");

        JButton searchBtn  = createSmallPrimaryButton("Search");
        JButton clearSearch = createSmallSecondaryButton("Clear");

        searchBtn.addActionListener(e -> filterList(searchField.getText()));
        clearSearch.addActionListener(e -> { searchField.setText(""); refreshList(); });
        searchField.addActionListener(e -> filterList(searchField.getText()));

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        btnRow.setBackground(AppTheme.BG_PRIMARY);
        btnRow.add(searchBtn);
        btnRow.add(clearSearch);

        searchBar.add(searchField, BorderLayout.CENTER);
        searchBar.add(btnRow, BorderLayout.EAST);
        panel.add(searchBar, BorderLayout.NORTH);

        taskListContainer = new JPanel();
        taskListContainer.setLayout(new BoxLayout(taskListContainer, BoxLayout.Y_AXIS));
        taskListContainer.setBackground(AppTheme.BG_PRIMARY);
        taskListContainer.setBorder(BorderFactory.createEmptyBorder(0, 12, 12, 12));

        JScrollPane scroll = new JScrollPane(taskListContainer);
        scroll.setBorder(null);
        scroll.setBackground(AppTheme.BG_PRIMARY);
        scroll.getViewport().setBackground(AppTheme.BG_PRIMARY);
        scroll.getVerticalScrollBar().setPreferredSize(new Dimension(6, 0));
        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    private void refreshList() {
        java.util.List<Task> active = taskService.getAllTasks().stream()
            .filter(t -> !t.isCompleted() && !t.isArchived())
            .collect(java.util.stream.Collectors.toList());
        showTasks(active);
    }

    private void filterList(String query) {
        if (query == null || query.isBlank()) { refreshList(); return; }
        java.util.List<Task> results = taskService.searchTasks(query).stream()
            .filter(t -> !t.isCompleted() && !t.isArchived())
            .collect(java.util.stream.Collectors.toList());
        showTasks(results);
    }

    private void showTasks(List<Task> tasks) {
        taskListContainer.removeAll();
        if (tasks.isEmpty()) {
            JLabel empty = new JLabel("No tasks found", SwingConstants.CENTER);
            empty.setFont(AppTheme.FONT_BODY);
            empty.setForeground(AppTheme.TEXT_MUTED);
            empty.setAlignmentX(CENTER_ALIGNMENT);
            empty.setBorder(BorderFactory.createEmptyBorder(40, 0, 0, 0));
            taskListContainer.add(empty);
        } else {
            for (Task task : tasks) {
                taskListContainer.add(createTaskRow(task));
                taskListContainer.add(Box.createVerticalStrut(6));
            }
        }
        taskListContainer.add(Box.createVerticalGlue());
        taskListContainer.revalidate();
        taskListContainer.repaint();
    }

    private JPanel createTaskRow(Task task) {
        boolean isSelected = selectedTask != null && selectedTask.getId().equals(task.getId());

        JPanel row = new JPanel(new BorderLayout(10, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(isSelected ? AppTheme.BG_SELECTED : AppTheme.BG_CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                if (isSelected) {
                    g2.setColor(AppTheme.ACCENT_BLUE);
                    g2.setStroke(new BasicStroke(1.5f));
                    g2.drawRoundRect(1, 1, getWidth()-2, getHeight()-2, 8, 8);
                }
                g2.dispose();
            }
        };
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 85));
        row.setAlignmentX(LEFT_ALIGNMENT);
        row.setBorder(new CompoundBorder(
            new MatteBorder(0, 4, 0, 0, AppTheme.getPriorityColor(task.getPriority())),
            BorderFactory.createEmptyBorder(10, 14, 10, 14)
        ));

        JPanel left = new JPanel(new BorderLayout(10, 0));
        left.setOpaque(false);

        JCheckBox check = new JCheckBox();
        check.setSelected(task.isCompleted());
        check.setOpaque(false);
        check.setFocusPainted(false);
        check.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        check.addActionListener(e -> taskService.toggleComplete(task.getId()));

        JPanel info = new JPanel();
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.setOpaque(false);

        JLabel titleLbl = new JLabel(task.isCompleted()
            ? "<html><s>" + task.getTitle() + "</s></html>" : task.getTitle());
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        titleLbl.setForeground(task.isCompleted() ? AppTheme.TEXT_MUTED : AppTheme.TEXT_PRIMARY);

        // Build two-line date meta
        JPanel metaPanel = new JPanel();
        metaPanel.setLayout(new BoxLayout(metaPanel, BoxLayout.Y_AXIS));
        metaPanel.setOpaque(false);

        if (task.getPlannedDate() != null) {
            JLabel planLbl = new JLabel("[Plan] " + task.getPlannedDate().format(DISPLAY_FMT));
            planLbl.setFont(AppTheme.FONT_SMALL);
            planLbl.setForeground(AppTheme.ACCENT_CYAN);
            metaPanel.add(planLbl);
        }
        if (task.getDueDate() != null) {
            String dueStr = "[Due] " + task.getDueDate().format(DISPLAY_FMT);
            if (task.getDueTime() != null)
                dueStr += " " + task.getDueTime().format(DateTimeFormatter.ofPattern("HH:mm"));
            JLabel dueLbl = new JLabel(dueStr);
            dueLbl.setFont(AppTheme.FONT_SMALL);
            dueLbl.setForeground(AppTheme.ACCENT_ORANGE);
            metaPanel.add(dueLbl);
        }
        if (task.getPlannedDate() == null && task.getDueDate() == null) {
            JLabel noDate = new JLabel(task.getPriority().name());
            noDate.setFont(AppTheme.FONT_SMALL);
            noDate.setForeground(AppTheme.TEXT_MUTED);
            metaPanel.add(noDate);
        }

        info.add(titleLbl);
        info.add(Box.createVerticalStrut(3));
        info.add(metaPanel);

        left.add(check, BorderLayout.WEST);
        left.add(info, BorderLayout.CENTER);

        JButton editBtn = new JButton("Edit");
        editBtn.setFont(AppTheme.FONT_SMALL);
        editBtn.setForeground(AppTheme.ACCENT_BLUE);
        editBtn.setBackground(new Color(88, 166, 255, 15));
        editBtn.setBorder(new CompoundBorder(
            new LineBorder(new Color(88, 166, 255, 50), 1, true),
            BorderFactory.createEmptyBorder(4, 10, 4, 10)
        ));
        editBtn.setFocusPainted(false);
        editBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        editBtn.addActionListener(e -> loadTaskIntoForm(task));

        row.add(left, BorderLayout.CENTER);
        row.add(editBtn, BorderLayout.EAST);

        row.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        row.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getSource() == row) loadTaskIntoForm(task);
            }
        });

        return row;
    }

    private void loadTaskIntoForm(Task task) {
        selectedTask = task;
        formTitle.setText("Edit Task");
        titleField.setText(task.getTitle());
        descField.setText(task.getDescription() != null ? task.getDescription() : "");
        priorityBox.setSelectedItem(task.getPriority());

        setDateDropdowns(plannedDayBox, plannedMonthBox, plannedYearBox, task.getPlannedDate());
        setDateDropdowns(dueDayBox, dueMonthBox, dueYearBox, task.getDueDate());
        timeField.setText(task.getDueTime() != null ? task.getDueTime().toString() : "");

        saveButton.setText("Save Changes");
        deleteButton.setVisible(true);
        refreshList();
    }

    private void clearForm() {
        selectedTask = null;
        formTitle.setText("Add New Task");
        titleField.setText("");
        descField.setText("");
        priorityBox.setSelectedItem(Task.Priority.MEDIUM);
        // Default planned = today
        setDateDropdowns(plannedDayBox, plannedMonthBox, plannedYearBox, LocalDate.now());
        setDateDropdowns(dueDayBox, dueMonthBox, dueYearBox, null);
        timeField.setText("");
        saveButton.setText("+ Save Task");
        deleteButton.setVisible(false);
        refreshList();
    }

    private void saveTask() {
        String title = titleField.getText().trim();
        if (title.isEmpty()) { showError("Task title is required."); return; }

        LocalDate plannedDate = readDateFromDropdowns(
            plannedDayBox, plannedMonthBox, plannedYearBox, "Planned Date");
        // null is allowed — but if partial, readDate shows error and returns null with a sentinel
        if (isPartialDate(plannedDayBox, plannedMonthBox, plannedYearBox)) return;

        LocalDate dueDate = readDateFromDropdowns(
            dueDayBox, dueMonthBox, dueYearBox, "Due Date");
        if (isPartialDate(dueDayBox, dueMonthBox, dueYearBox)) return;

        // Validate: if both set, due must be >= planned
        if (plannedDate != null && dueDate != null && dueDate.isBefore(plannedDate)) {
            showError("Due Date (deadline) cannot be before the Planned Date.");
            return;
        }

        LocalTime dueTime = null;
        String timeStr = timeField.getText().trim();
        if (!timeStr.isEmpty()) {
            try { dueTime = LocalTime.parse(timeStr); }
            catch (DateTimeParseException e) { showError("Invalid time format. Use HH:mm (e.g. 17:30)"); return; }
        }

        String desc = descField.getText().trim();
        Task.Priority priority = (Task.Priority) priorityBox.getSelectedItem();

        if (selectedTask == null) {
            Task newTask = new Task(title, desc.isEmpty() ? null : desc, priority,
                                   plannedDate, dueDate, dueTime);
            taskService.addTask(newTask);
        } else {
            selectedTask.setTitle(title);
            selectedTask.setDescription(desc.isEmpty() ? null : desc);
            selectedTask.setPriority(priority);
            selectedTask.setPlannedDate(plannedDate);
            selectedTask.setDueDate(dueDate);
            selectedTask.setDueTime(dueTime);
            taskService.updateTask(selectedTask);
        }
        clearForm();
    }

    /** Returns true if exactly 1 or 2 of the 3 dropdowns are set (partial = invalid) */
    private boolean isPartialDate(JComboBox<String> d, JComboBox<String> m, JComboBox<String> y) {
        int set = (d.getSelectedIndex() > 0 ? 1 : 0)
                + (m.getSelectedIndex() > 0 ? 1 : 0)
                + (y.getSelectedIndex() > 0 ? 1 : 0);
        return set > 0 && set < 3;
    }

    private void deleteTask() {
        if (selectedTask == null) return;
        int confirm = JOptionPane.showConfirmDialog(this,
            "Delete task \"" + selectedTask.getTitle() + "\"?",
            "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            taskService.deleteTask(selectedTask.getId());
            clearForm();
        }
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Validation Error", JOptionPane.ERROR_MESSAGE);
    }

    // ── Styled Components ────────────────────────────────────────────────────

    private JLabel buildFieldLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(AppTheme.FONT_SMALL);
        lbl.setForeground(AppTheme.TEXT_SECONDARY);
        lbl.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));
        lbl.setAlignmentX(LEFT_ALIGNMENT);
        return lbl;
    }

    private JTextField createTextField(String placeholder) {
        JTextField field = new JTextField();
        field.setFont(AppTheme.FONT_BODY);
        field.setForeground(AppTheme.TEXT_PRIMARY);
        field.setBackground(AppTheme.BG_CARD);
        field.setCaretColor(AppTheme.TEXT_PRIMARY);
        field.setBorder(new CompoundBorder(
            new LineBorder(AppTheme.BORDER, 1, false),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        field.setAlignmentX(LEFT_ALIGNMENT);
        field.putClientProperty("JTextField.placeholderText", placeholder);
        field.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                field.setBorder(new CompoundBorder(
                    new LineBorder(AppTheme.ACCENT_BLUE, 1, false),
                    BorderFactory.createEmptyBorder(8, 12, 8, 12)));
            }
            public void focusLost(FocusEvent e) {
                field.setBorder(new CompoundBorder(
                    new LineBorder(AppTheme.BORDER, 1, false),
                    BorderFactory.createEmptyBorder(8, 12, 8, 12)));
            }
        });
        return field;
    }

    private void styleComboBox(JComboBox<Task.Priority> box) {
        box.setFont(AppTheme.FONT_BODY);
        box.setForeground(AppTheme.TEXT_PRIMARY);
        box.setBackground(AppTheme.BG_CARD);
        box.setBorder(new LineBorder(AppTheme.BORDER, 1));
        box.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        box.setAlignmentX(LEFT_ALIGNMENT);
        box.setRenderer(new DefaultListCellRenderer() {
            public Component getListCellRendererComponent(JList<?> list, Object value,
                    int idx, boolean sel, boolean foc) {
                super.getListCellRendererComponent(list, value, idx, sel, foc);
                if (value instanceof Task.Priority p) {
                    setText(AppTheme.getPriorityIcon(p) + "  " + p.name());
                    setForeground(AppTheme.getPriorityColor(p));
                }
                setBackground(sel ? AppTheme.BG_HOVER : AppTheme.BG_CARD);
                setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
                return this;
            }
        });
    }

    private JButton createPrimaryButton(String text, Color color) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setForeground(color);
        btn.setBackground(AppTheme.BG_CARD);
        btn.setOpaque(true);
        btn.setBorder(new CompoundBorder(
            new LineBorder(color, 1, true),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(new Color(
                    Math.min(color.getRed()/4 + AppTheme.BG_CARD.getRed()*3/4, 255),
                    Math.min(color.getGreen()/4 + AppTheme.BG_CARD.getGreen()*3/4, 255),
                    Math.min(color.getBlue()/4 + AppTheme.BG_CARD.getBlue()*3/4, 255)
                ));
            }
            public void mouseExited(MouseEvent e) { btn.setBackground(AppTheme.BG_CARD); }
        });
        return btn;
    }

    private JButton createSecondaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setForeground(AppTheme.TEXT_PRIMARY);
        btn.setBackground(AppTheme.BG_CARD);
        btn.setOpaque(true);
        btn.setBorder(new CompoundBorder(
            new LineBorder(AppTheme.BORDER, 1, true),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.setBackground(AppTheme.BG_HOVER); }
            public void mouseExited(MouseEvent e)  { btn.setBackground(AppTheme.BG_CARD);  }
        });
        return btn;
    }

    private JButton createSmallPrimaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(AppTheme.FONT_SMALL);
        btn.setForeground(AppTheme.ACCENT_BLUE);
        btn.setBackground(new Color(88, 166, 255, 20));
        btn.setBorder(new CompoundBorder(
            new LineBorder(new Color(88, 166, 255, 60), 1, true),
            BorderFactory.createEmptyBorder(6, 14, 6, 14)
        ));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JButton createSmallSecondaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(AppTheme.FONT_SMALL);
        btn.setForeground(AppTheme.TEXT_MUTED);
        btn.setBackground(AppTheme.BG_CARD);
        btn.setBorder(new CompoundBorder(
            new LineBorder(AppTheme.BORDER, 1, true),
            BorderFactory.createEmptyBorder(6, 14, 6, 14)
        ));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }
}
