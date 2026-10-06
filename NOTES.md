# Git and Spring Boot Basics

## Spring Boot Concepts

### What is a starter?
A **starter** is a single dependency (one line in your build file) that automatically pulls in a complete set of required libraries for a specific task.

### What does @SpringBootApplication do?
The `@SpringBootApplication` annotation explicitly tells the framework that this class is the **starting point and configuration hub** of a Spring Boot application.

---

## Git Concepts

### What is a branch for?
We create a **branch** in Git so that experimental code changes or new features do not affect the `main` branch. Once we are satisfied with the changes, we merge them back into the `main` branch.

### What is a pull request for?
A **pull request (PR)** is a proposal to **merge changes** from one branch into another (usually your feature branch into the `main` branch). It allows team members to review the code, discuss changes, and approve them before they are officially integrated into the codebase.

*(Note: To download changes from a Git server to your local machine, you use `git pull`, whereas a pull request is a collaboration tool on platforms like GitHub or GitLab).*
