# Wikipedia Article Manager

**Wikipedia Article Manager** is a web application that allows users to explore, organize, and annotate Wikipedia articles.  
Users can view articles with their basic metadata (title, snippet, page ID), add personal comments, assign grades, and organize articles into custom categories. The platform provides a clean interface to manage articles efficiently while keeping all data structured and searchable.

---

## High-Level Overview

- **Backend:** Built using the Java **Spring Framework**, providing a robust, scalable **REST API** running on an embedded **Tomcat** web server.

- **Persistence:** Uses **Hibernate** and **JPA** for object-relational mapping. The project also follows the **Repository Pattern**, which abstracts database operations behind repository interfaces, making the code cleaner, easier to maintain, and testable.

- **Database:** Powered by **Apache Derby Network Server**. The network version must be installed and running before launching the application.

- **Frontend:** Lightweight, responsive interface built with **vanilla HTML5, CSS, and JavaScript**, offering intuitive interaction without the overhead of additional frameworks.

- **Build System:** Uses **Maven** for dependency management and building. Ensure Maven support is available (via IDE plugin or global installation) to compile and run the backend packages.

---

## Prerequisites

Before running the application, make sure you have the following installed and configured:

1. **Maven Plugin** – required to build and manage project dependencies. Most IDEs provide Maven support via a plugin; global Maven installation is optional.

2. **Apache Derby (Network Version)** – must be installed and running. The application connects to Derby in network mode.

3. **Java JDK** – ensure a compatible JDK is installed (**Java 17 or later** recommended).

---
