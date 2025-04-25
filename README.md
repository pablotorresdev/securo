# Conitrack Project Documentation

## **Overview**

Conitrack is a Spring Boot application designed for user management with a PostgreSQL database. It includes Dockerized environments for both the application and the database, complete with automated backup capabilities and options for deployment in the cloud.

---

## **Project Structure**

```
/conitrack                   # Root directory of the project
   ├── src                   # Source code of the Spring Boot application
   ├── backups               # Directory for database backups
   ├── Dockerfile            # Dockerfile for the Spring Boot application
   ├── build.gradle          # Gradle build file
   ├── data-base             # Directory for database configurations
   │   ├── backup.sh         # Backup script for PostgreSQL cron job
   │   ├── custom_backup.sh  # Backup script for custom PostgreSQL copy
   │   └── Dockerfile.db     # Dockerfile for the PostgreSQL database
   ├── docker-compose.yml    # Docker Compose configuration file
```

---

## **Prerequisites**

Before setting up the project, ensure you have the following tools installed:

- Docker and Docker Compose
- Java 17 or higher
- Gradle
- PostgreSQL client (for manual database management)

---

## **Setup Instructions**

### **1. Build and Run the Project with Docker**

1. **Build Docker Images:**

   ```bash
   docker-compose build
   ```

2. **Run Docker Containers:**

   ```bash
   docker-compose up -d
   ```

    - This starts two containers:
        - `postgres-db`: PostgreSQL database with automated backups.
        - `spring-app`: Spring Boot application connected to the database.

3. **Access the Application:**

    - Open your browser and navigate to `http://localhost:8080`.

4. **Verify Database Connection:**

    - Run the following command to check the database:
      ```bash
      docker exec -it postgres-db psql -U postgres -d conitrack
      ```
5. **Stop the Containers:**

    ```bash
    docker-compose down
    ```

---

### **2. Backup and Restore**

#### **Automated Backups**

- The database container includes a cron job that creates backups every 10 minutes.
- Backups are stored in `/backups` inside the container.

#### **Manual Backups**

1. Trigger a manual backup:
   ```bash
   .\data-base\custom_backup.sh

   This will create a backup file in the `/backups` directory.
   ```

#### **Restore Backups**

1. Connect to the PostgreSQL container:
   ```bash
   docker exec -it postgres-db psql -U postgres
   ```
2. Drop the existing database if necessary:
   ```sql
   \c postgres
   SELECT pid, usename, datname, client_addr, state FROM pg_stat_activity WHERE datname = 'conitrack';
   SELECT pg_terminate_backend(pid) FROM pg_stat_activity WHERE datname = 'conitrack';
   DROP DATABASE conitrack;
   ```
3. Recreate the database:
   ```sql
   CREATE DATABASE conitrack;
   ```
   Exit the PostgreSQL shell once done.

4. Copy the backup file to the container:
   ```bash
   docker cp backups/backup_2024-12-28_18-00-01.sql postgres-db:/backup_2024-12-28_18-00-01.sql
   ```
5. Restore the backup:
   ```bash
   docker exec -it postgres-db psql -U postgres -d conitrack -f /backup_2024-12-28_18-00-01.sql
   ```

---

### **3. Application Configuration**

#### **Environment Variables (Docker)**

Ensure the following environment variables are set for the application:

```env
SPRING_DATASOURCE_URL=jdbc:postgresql://postgres-db:5432/conitrack
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=root
```

#### **Logging**

Logs for the database backups are stored at `/var/log/backup.log` within the database container.

---

## **Cloud Deployment**

### **1. Heroku Deployment**

1. **Install Heroku CLI:**

   ```bash
   heroku login
   ```

2. **Deploy the Application:**

   ```bash
   heroku container:push web -a conitrack
   heroku container:release web -a conitrack
   ```

3. **Add PostgreSQL Add-On:**

   ```bash
   heroku addons:create heroku-postgresql
   ```

4. **Configure Environment Variables:**
   Set `DATABASE_URL` using the value provided by Heroku's PostgreSQL add-on.

---

## **Monitoring and Troubleshooting**

### **1. Logs**

- View Spring Boot application logs:
  ```bash
  docker logs spring-app
  ```
- View database backup logs:
  ```bash
  docker exec -it postgres-db tail -f /var/log/backup.log
  ```

### **2. Common Errors**

- **Port Conflict:**
  Ensure no other services are using ports `5432` or `8080`.
- **Database Connection Issues:**
  Check the environment variables and ensure the database container is running.

---

## **Future Enhancements**

1. **Automated Backup Storage**
    - Integrate with AWS S3 or Google Cloud Storage for backup storage.
2. **Monitoring Tools**
    - Use Prometheus and Grafana for advanced monitoring.
3. **Load Balancing**
    - Add support for load balancers like NGINX.

---

