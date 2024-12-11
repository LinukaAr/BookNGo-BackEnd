# OnlineTicketing Backend 

---

## Introduction
The **OnlineTicketing Backend** is a Spring Boot-based application providing RESTful APIs and WebSocket support for managing and viewing ticketing information in real-time.

Link to Frontend: [https://github.com/LinukaAr/BookNGo-FrontEnd](https://github.com/LinukaAr/BookNGo-FrontEnd)

---

## Setup Instructions

### Prerequisites
- **Java**: 17 or higher
- **Maven**: 3.9.9 or higher
- **MySQL database**

### How to Build and Run the Application
1. **Clone the repository:**
   ```bash
   git clone https://github.com/your-repo/online-ticketing-backend.git
   cd online-ticketing-backend

2. **Configure the database**: Update the database connection properties in the application.properties file:

    ```bash
    spring.datasource.url=jdbc:mysql://localhost:3306/ticketing_db
    spring.datasource.username=root
    spring.datasource.password=yourpassword

3. **Build the project**:

    ```bash
    ./mvnw clean install

4. **Run the application:**

    ```bash
    ./mvnw spring-boot:run

### CLI Usage Guidelines
The CLI for the OnlineTicketing system is implemented in the TicketingCLI class. It allows interaction with the system via the command line.

### Commands
start: Start the ticket handling operations.
stop: Stop the ticket handling operations.
add: Add a specified number of tickets.
exit: Exit the CLI.

### Example Usage
Run the CLI:
    
    
    java -cp target/online-ticketing-0.0.1-SNAPSHOT.jar com.linuka.OnlineTicketing.TicketingCLI

    

## GUI Usage Guidelines
The GUI for the OnlineTicketing system is implemented in the frontend part of the project, which is a separate Angular application. Please refer to the frontend repository for setup and usage instructions.

## Troubleshooting
### Common Issues

**Database Connection Issues**
* Ensure the MySQL server is running and accessible.

* Verify the database connection properties in the application.properties file.

**Port Conflicts**

* Ensure port 8080 is not being used by another application.

* Change the port in the application.properties file if needed:

    ```bash
    server.port=8081

**Dependency Issues**
* Ensure all dependencies are correctly specified in the pom.xml file.

* Run the following command to resolve dependency issues:

    ```bash
    ./mvnw clean install

### Logs
Logs are stored in the logs directory. Access logs by hitting the /api/logs endpoint.








