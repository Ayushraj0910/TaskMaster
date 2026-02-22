package com.taskmaster.model;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public class Task implements Serializable {
    // Keep at 1L forever — new fields default safely on old save files
    private static final long serialVersionUID = 1L;

    public enum Priority { LOW, MEDIUM, HIGH, CRITICAL }

    private String id;
    private String title;
    private String description;
    private Priority priority;

    private LocalDate plannedDate;
    private LocalDate dueDate;
    private LocalTime dueTime;
    private boolean completed;
    private boolean archived;   // cleared from Task Chart, still shown in Calendar history
    private long createdAt;
    private long completedAt;   // timestamp when marked done (0 = not done)

    public Task(String title, String description, Priority priority,
                LocalDate plannedDate, LocalDate dueDate, LocalTime dueTime) {
        this.id = UUID.randomUUID().toString();
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.plannedDate = plannedDate;
        this.dueDate = dueDate;
        this.dueTime = dueTime;
        this.completed = false;
        this.archived = false;
        this.createdAt = System.currentTimeMillis();
        this.completedAt = 0;
    }

    // Ensures new fields (archived, completedAt) safely default to false/0 on old saves
    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        ois.defaultReadObject();
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Priority getPriority() { return priority; }
    public void setPriority(Priority priority) { this.priority = priority; }

    public LocalDate getPlannedDate() { return plannedDate; }
    public void setPlannedDate(LocalDate plannedDate) { this.plannedDate = plannedDate; }

    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }

    public LocalTime getDueTime() { return dueTime; }
    public void setDueTime(LocalTime dueTime) { this.dueTime = dueTime; }

    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }

    public boolean isArchived() { return archived; }
    public void setArchived(boolean archived) { this.archived = archived; }

    public long getCreatedAt() { return createdAt; }
    public long getCompletedAt() { return completedAt; }
    public void setCompletedAt(long completedAt) { this.completedAt = completedAt; }

    public LocalDate getPrimaryDate() {
        return plannedDate != null ? plannedDate : dueDate;
    }

    /** The calendar date on which this task was completed, or null if not completed. */
    public LocalDate getCompletionDate() {
        if (!completed || completedAt == 0) return null;
        return java.time.Instant.ofEpochMilli(completedAt)
            .atZone(java.time.ZoneId.systemDefault())
            .toLocalDate();
    }

    @Override
    public String toString() {
        return title + " [" + priority + "] " + (plannedDate != null ? plannedDate : "No date");
    }
}
