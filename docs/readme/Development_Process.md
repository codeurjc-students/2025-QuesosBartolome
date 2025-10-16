### Development Process

The development of the Quesos Bartolomé application has been carried out using an iterative and incremental process following the principles of the Agile Manifesto, incorporating practices from Extreme Programming (XP) and a visual management system based on Kanban.
Throughout the process, functional versions of the system are delivered at regular intervals, focused on specific features and with continuous feedback from the project tutor.

#### Task Management

GitHub Issues and GitHub Projects have been used for task management:

- Each new feature or issue is registered as an Issue and added to the project board. The board is divided into different sections (Todo, In Progress, Done) that show the status of each task or issue, facilitating workflow tracking.

#### Git

The source code is managed using Git, hosted in a remote repository on GitHub.

A lightweight branch strategy inspired by Git Flow was applied, adapted to the project’s needs:
- main → main branch.
- feature/ →* branches for the development of new features.
- fix/ →* branches for specific bug fixes.

For each feature, a feature/ branch is created where the implementation is developed. Once complete and passing CI, it is merged into the main branch via a Pull Request.

**Repository Metrics:**

| Metric | Value |
|---------|-------|
| Commits | -  | 
| Branches | -  | 
| Issues | -  | 
| Pull Requests | -  | 

#### Continuous Integration

The Quesos Bartolomé application has a Continuous Integration (CI) system implemented with GitHub Actions, with two levels of quality control:

| Quality Control | Description |
|---------|-------|
| CI-Feature | - Runs automatically on every commit or push to a feature branch. Executes unit tests.  | 
| CI-Full | - Runs automatically when a Pull Request is opened toward the main branch. Executes all tests on both client and server and performs static code analysis with SonarQube. If this control is not passed, the Pull Request cannot be completed.  | 
