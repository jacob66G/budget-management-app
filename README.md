# Smart Budget - Budget management app with AI financial assistant

---

## Description

Smart Budget is a comprehensive personal finance management application designed
to help users take full control over their budget. By combining industry standards
with cutting-edge solutions - including artificial intelligence - it provides a unique
and modern approach to financial management that stands out from traditional tools.

---

## Main Features

- **AI Financial Assistant:**
an intelligent, chat-based assistant performing tasks such as: analyzing user spending, providing personalized financial
advice and helping to make reasonable financial decisions.
- **Digital Receipt Archiving:**
the ability to securely upload and store photos of receipts or invoices in the cloud with assignment to a given transaction.
- **Recurring Payment Automation:**
full support for recurring transactions (such as rent, utilities or subscriptions), eliminating the need for manual entries and ensuring
no bills are missed.
- **Budgeting and Limits:**
defining a budget on a selected account and notifying before exceeding the set spending amount.
- **Transaction Categorization:**
assigning a transaction to a given category in order to group them and for easier analysis.
- **Interactive Data Visualisations:**
dynamic and clear charts that provide instant insights into financial flow, incomes and expenses.
- **PDF Financial Reports:**
the ability to generate financial summaries and analysis for a given account in a PDF format.

---

## Architecture Overview
The application follows a Decoupled Client-Server Architecture, ensuring a clear separation of concerns:

- **Frontend (Client):** A dynamic Single Page Application (SPA) built with Angular that communicates with the backend via a RESTful API.

- **Backend (Server):** A modular Spring Boot service handling business logic, AI integrations, and data persistence.

- **Real-time Communication:** Uses WebSockets (SockJS) to push instant notifications from the server to the client without page refreshes.


---

## Tech Stack

### Frontend

- **Angular 20:**
core framework for the user interface.
- **Angular Material:**
UI component library for consistent design and UX.
- **TypeScript 5:**
primary language for type-safe client-side logic.
- **Vite:**
fast build tool and development server.
- **Chart.js:**
library used for interactive financial data visualisations with charts.
- **SockJs:**
enables real-time communication using WebSocket protocol for notification mechanism.
- **HTML5 and SCSS:**
layout and advanced styling

### Backend

- **Java 21:**
primary programming language for core business logic.
- **Spring Boot 3.5:**
core framework for the Java-based web application.
- **Spring Data JPA & Hibernate:**
persistent data management and ORM.
- **Spring Security + JWT:**
secure, stateless authentication and authorization system using JWT standard.
- **Spring AI & OpenAI API:**
integration of Large Language Model (GPT-4o) from OpenAI with app business logic.
- **JavaMailSender:**
  used for sending registration emails and generated PDF reports via the SMTP protocol.
- **WebSockets:**
enables real-time communication with client-side based on WebSocket protocol. Used for notifications.
- **PostgreSQL:**
relational database served as primary data storage.
- **H2 Database:**
lightweight in-memory database used for testing.
- **REDIS:**
high-performance data store used for efficient user session management.
- **AWS S3:**
scalable cloud storage for secure digital receipt and invoice archiving.
- **JUnit/Mockito:**
frameworks used for unit and integration testing of the application's business logic.

### Infrastructure
- **Docker**
containerization for developing and running the application
in a consistent and isolated environment.

---

## Run Instructions
The easiest way to run the application is using Docker Compose, which sets up the frontend, backend
and all necessary components (PostgreSQL, REDIS) in a single command.

### Prerequisites
Ensure you meet the following requirements to run the application:
- Docker installed.
- OpenAI API key (for AI-powered financial assistant).
- AWS S3 account and credentials (for uploading and storing transaction attachments).
- SMTP Server Credentials - required for sending registration emails and PDF reports.

### Setup & Configuration
1. **Clone the repository:**
    ```bash
        git clone https://github.com/jacob66G/budget-management-app.git
        cd budget-management-app
    ```
   
2. **Configure Environment Variables:**
    The application leverages an external `.env` file with environment variables to manage sensitive credentials securely. To set it up
    copy the example file from root directory (.env.example). Create new `.env` file (based on the example file) and provide your
    actual credentials and other start up data.
    <br><br>
    **Note:** All variables listed in `.env.example` are required. The application may not start correctly if data in `.env` file are missing.

### Run the Application
Launch the entire stack using the following command:
```bash
   docker-compose up --build
```

Docker will download the necessary images, build the backend, frontend and other containers, and link them via a private network.
Once the process is finished, the application will be accessible at:

- Frontend: http://localhost:80
- Backend API: http://localhost:8080
- Database (Postgres): localhost:5432

### Stopping the Application
To stop and remove the containers, use:
```bash
   docker-compose down
```

---

## App Preview
The following preview showcases a selection of key modules and core functionalities, highlighting the primary user interface and essential features of the application.

### Figure 1: Dashboard
![Dashboard](https://res.cloudinary.com/dymfiz2z4/image/upload/w_1200,c_limit/v1771247753/dashboard_1_u6b0en.png)
<br>
*The application's dashboard provides a comprehensive financial overview for the user featuring interactive 
data visualisation.*

### Figure 2: AI Financial Assistant
![AI Financial Assistant](https://res.cloudinary.com/dymfiz2z4/image/upload/w_1200,c_limit/v1771248746/asystent-ai-analiza-wydatk%C3%B3w_lp4voe.png)
<br>
*An AI-based Financial Assistant in the form of conversational chat that automates the analysis of user expenses and provides s financial summary with suggestions for improvement.*

### Figure 3: Transaction Manager
![Transaction Manager Page](https://res.cloudinary.com/dymfiz2z4/image/upload/w_1200,c_limit/v1771249305/strona-transakcji_dhnysj.png)
<br>
*A dedicated module for managing and tracking user expenses and incomes, featuring advanced transaction filtering with efficient pagination.
It provides insights into recent and incoming transactions.*

### Figure 4: Transaction Attachment Upload
![Transaction Attachment Upload](https://res.cloudinary.com/dymfiz2z4/image/upload/w_1200,c_limit/v1771249953/dodawanie-zdj%C4%99cia-transakcji_zcl994.png)
<br>
*This view illustrates the secure attachment upload functionality and assigning them to given transactions. Attachments are stored in AWS S3 Storage and
are being uploaded using Presigned URLs mechanism.*

### Figure 5: Recurring Transaction Manager
![Recurring Transaction Manager](https://res.cloudinary.com/dymfiz2z4/image/upload/w_1200,c_limit/v1771250892/strona-do-zarz%C4%85dzania-szablonami-cyklicznymi_axjfep.png)
<br>
*A dedicated module responsible for managing recurring templates that the system leverages to automate transaction creation at specified intervals.*

### Figure 6: Account View with Defined Budget
![Account View with Defined Budget](https://res.cloudinary.com/dymfiz2z4/image/upload/w_1200,c_limit/v1771251227/widok-konta_njtigy.png)
<br>
*A detailed account view featuring specified monthly budget with an alert threshold. This module combines advanced financial data visualisation with real-time budget monitoring.*
