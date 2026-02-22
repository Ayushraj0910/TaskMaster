# ✦ TaskMaster – Java Swing Task Management Application

## 📌 Description

TaskMaster is a desktop-based task management application developed using
Java Swing. It allows users to efficiently create, organize, schedule, and
manage daily tasks through an interactive graphical interface.

The application provides multiple panels for task viewing, scheduling, and
settings — making it easy to handle productivity workflows in a structured way.

This project demonstrates strong understanding of:

- Object-Oriented Programming
- Layered Architecture Design (MVC)
- Java Swing (GUI Development)
- Event-Driven Programming
- Data Management using Java Collections
- Modular Code Organization

---

## 🚀 Features

- 📋 Create, update, and delete tasks
- 🎯 Assign priority levels to tasks
- 📅 Calendar-based task scheduling
- 🗂️ Organized task list display
- 🔄 Real-time task updates in GUI
- 🎨 Consistent dark application theme
- ⚙️ Settings panel for app preferences
- 🖥️ Multi-panel interactive interface

---

## 🛠️ Tech Stack

- Java 17
- Java Swing (GUI)
- Object-Oriented Programming (OOP)
- Layered Architecture (MVC Pattern)
- Event-Driven Programming
- Java Collections Framework

---

## 📁 Project Structure
```
TaskMaster/
│
├── src/
│   ├── resources/
│   │   └── taskmaster_icon.png
│   │
│   └── com/taskmaster/
│       ├── Main.java
│       ├── model/
│       │   └── Task.java
│       ├── service/
│       │   └── TaskService.java
│       ├── ui/
│       │   ├── MainWindow.java
│       │   ├── CalendarPanel.java
│       │   ├── TaskListPanel.java
│       │   ├── TaskManagementPanel.java
│       │   └── SettingsPanel.java
│       └── util/
│           └── AppTheme.java
│
└── README.md
```

---

## 🧠 How It Works

### 🔹 Application Architecture

The project follows a layered architecture to maintain separation of concerns:

- **Model Layer** → Defines data structures for task representation
- **Service Layer** → Processes task operations and business logic
- **UI Layer** → Handles user interaction and displays GUI components
- **Utility Layer** → Provides theme management and helper functions

### 🔹 Execution Flow

1. Application starts from `Main.java`
2. Main window initializes all GUI panels
3. User interacts with UI components to manage tasks
4. UI panels communicate with `TaskService`
5. `TaskService` processes task data using model classes
6. Updates are reflected instantly across the interface

---

## ▶️ How to Run

### Option 1: Using an IDE

1. Open the project in IntelliJ / Eclipse / VS Code
2. Navigate to `src/com/taskmaster/Main.java`
3. Run the main class

### Option 2: Using Terminal
```bash
cd src
javac com/taskmaster/**/*.java
java com.taskmaster.Main
```

---

## 🎯 Learning Outcomes

Through this project, I gained hands-on experience in:

- Designing GUI applications using Java Swing
- Implementing layered software architecture (MVC)
- Managing application state and task data
- Writing modular and maintainable Java code
- Handling event-driven programming
- Organizing large-scale Java projects

---

## 👨‍💻 Author

**Ayush Raj**

B.Tech CSE (3rd Year) | DIT University, Dehradun
