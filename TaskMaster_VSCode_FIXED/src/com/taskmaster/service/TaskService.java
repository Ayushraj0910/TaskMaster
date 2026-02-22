package com.taskmaster.service;

import com.taskmaster.model.Task;
import java.io.*;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class TaskService {
    private static final String DATA_FILE = System.getProperty("user.home") + "/taskmaster_tasks.dat";
    private List<Task> tasks;
    private List<TaskChangeListener> listeners = new ArrayList<>();

    public interface TaskChangeListener {
        void onTasksChanged();
    }

    // ─── Status enum for a task on a specific calendar date ──────────────────
    public enum DateStatus {
        PLANNED,      // planned dot — task not yet past planned date
        SHIFTED,      // planned date passed, dot moved to today (task still pending)
        DUE,          // due date dot
        URGENT,       // planned + due collide on same day (or shifted onto due date)
        OVERDUE,      // past due date, not completed → red/failed
        COMPLETED,    // completed on this date → green
        NONE          // task not relevant to this date
    }

    public TaskService() {
        tasks = loadTasks();
    }

    public void addListener(TaskChangeListener l) { listeners.add(l); }
    private void notifyListeners() { listeners.forEach(TaskChangeListener::onTasksChanged); }

    public void addTask(Task task) { tasks.add(task); saveTasks(); notifyListeners(); }

    public void updateTask(Task task) {
        for (int i = 0; i < tasks.size(); i++) {
            if (tasks.get(i).getId().equals(task.getId())) { tasks.set(i, task); break; }
        }
        saveTasks(); notifyListeners();
    }

    public void deleteTask(String id) { tasks.removeIf(t -> t.getId().equals(id)); saveTasks(); notifyListeners(); }

    public void toggleComplete(String id) {
        tasks.stream().filter(t -> t.getId().equals(id)).findFirst()
            .ifPresent(t -> {
                t.setCompleted(!t.isCompleted());
                t.setCompletedAt(t.isCompleted() ? System.currentTimeMillis() : 0);
                saveTasks(); notifyListeners();
            });
    }

    public void resetAll() { tasks.clear(); saveTasks(); notifyListeners(); }

    public void clearCompletedHistory() { tasks.removeIf(Task::isCompleted); saveTasks(); notifyListeners(); }

    public void archiveCompleted() {
        tasks.stream().filter(Task::isCompleted).forEach(t -> t.setArchived(true));
        saveTasks(); notifyListeners();
    }

    public List<Task> getAllTasks() { return Collections.unmodifiableList(tasks); }

    // ─── Core date helper: what is the "effective planned date" for a task? ──
    // If planned date has passed and task is not done, the dot shifts to today.
    private LocalDate effectivePlannedDate(Task t) {
        if (t.getPlannedDate() == null) return null;
        LocalDate today = LocalDate.now();
        // If the planned date is in the past, the dot shifts to today (but never past due date)
        if (t.getPlannedDate().isBefore(today)) {
            if (t.getDueDate() != null && today.isAfter(t.getDueDate())) {
                return t.getDueDate(); // cap at due date (will be shown as overdue)
            }
            return today;
        }
        return t.getPlannedDate();
    }

    /**
     * Returns the DateStatus of a specific task on a specific date.
     * This drives both dot color and cell border in CalendarPanel.
     */
    public DateStatus getStatusForTaskOnDate(Task t, LocalDate date) {
        LocalDate today = LocalDate.now();

        // ── Completed task ──────────────────────────────────────────────────
        if (t.isCompleted()) {
            LocalDate cd = t.getCompletionDate();
            return date.equals(cd) ? DateStatus.COMPLETED : DateStatus.NONE;
        }

        // ── Overdue: past due date, not done ────────────────────────────────
        if (t.getDueDate() != null && today.isAfter(t.getDueDate())) {
            // Show overdue dot on due date AND on today (shifted)
            if (date.equals(t.getDueDate()) || date.equals(today)) {
                return DateStatus.OVERDUE;
            }
            return DateStatus.NONE;
        }

        LocalDate effPlanned = effectivePlannedDate(t);
        LocalDate due = t.getDueDate();

        // ── URGENT: effective planned and due land on same date ─────────────
        boolean plannedHere = date.equals(effPlanned);
        boolean dueHere     = due != null && date.equals(due);

        if (plannedHere && dueHere) return DateStatus.URGENT;

        // ── Shifted: original planned date passed, dot moved to today ───────
        if (plannedHere) {
            if (t.getPlannedDate() != null && t.getPlannedDate().isBefore(today)) {
                return DateStatus.SHIFTED;
            }
            return DateStatus.PLANNED;
        }

        if (dueHere) return DateStatus.DUE;

        return DateStatus.NONE;
    }

    /**
     * Returns all tasks relevant to a given calendar date (with any non-NONE status).
     */
    public List<Task> getTasksByDate(LocalDate date) {
        return tasks.stream()
            .filter(t -> getStatusForTaskOnDate(t, date) != DateStatus.NONE)
            .collect(Collectors.toList());
    }

    public List<Task> getTasksByPlannedDate(LocalDate date) {
        return tasks.stream().filter(t -> date.equals(t.getPlannedDate())).collect(Collectors.toList());
    }

    public List<Task> searchTasks(String query) {
        String q = query.toLowerCase();
        return tasks.stream()
            .filter(t -> t.getTitle().toLowerCase().contains(q) ||
                (t.getDescription() != null && t.getDescription().toLowerCase().contains(q)))
            .collect(Collectors.toList());
    }

    public List<Task> getTasksSortedByPriority() {
        return tasks.stream()
            .sorted(Comparator.comparing(Task::getPriority).reversed()
                .thenComparing(t -> t.getPrimaryDate() != null ? t.getPrimaryDate() : LocalDate.MAX))
            .collect(Collectors.toList());
    }

    public List<Task> getTasksSortedByDate() {
        return tasks.stream()
            .sorted(Comparator.comparing((Task t) -> t.getPrimaryDate() != null ? t.getPrimaryDate() : LocalDate.MAX)
                .thenComparing(t -> t.getDueTime() != null ? t.getDueTime() : java.time.LocalTime.MAX))
            .collect(Collectors.toList());
    }

    public Set<LocalDate> getDatesWithTasks() {
        Set<LocalDate> dates = new HashSet<>();
        LocalDate today = LocalDate.now();
        for (Task t : tasks) {
            if (t.isCompleted()) {
                LocalDate cd = t.getCompletionDate();
                if (cd != null) dates.add(cd);
            } else {
                LocalDate effPlanned = effectivePlannedDate(t);
                if (effPlanned != null) dates.add(effPlanned);
                if (t.getDueDate() != null) dates.add(t.getDueDate());
                // Also add today if overdue
                if (t.getDueDate() != null && today.isAfter(t.getDueDate())) dates.add(today);
            }
        }
        return dates;
    }

    public boolean hasHighPriorityTaskOn(LocalDate date) {
        return tasks.stream()
            .filter(t -> !t.isCompleted())
            .filter(t -> getStatusForTaskOnDate(t, date) != DateStatus.NONE)
            .anyMatch(t -> t.getPriority() == Task.Priority.HIGH || t.getPriority() == Task.Priority.CRITICAL);
    }

    @SuppressWarnings("unchecked")
    private List<Task> loadTasks() {
        File file = new File(DATA_FILE);
        if (!file.exists()) return new ArrayList<>();
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            return (List<Task>) ois.readObject();
        } catch (Exception e) {
            System.err.println("TaskMaster: Could not load save file (" + e.getMessage() + "). Starting fresh.");
            file.renameTo(new File(DATA_FILE + ".bak"));
            return new ArrayList<>();
        }
    }

    private void saveTasks() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(DATA_FILE))) {
            oos.writeObject(tasks);
        } catch (IOException e) { e.printStackTrace(); }
    }
}
